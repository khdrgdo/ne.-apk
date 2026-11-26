package com.nexus.app.core.session

import android.net.Uri
import com.nexus.app.core.network.ApiClient
import com.nexus.app.core.network.ApiErrorKind
import com.nexus.app.core.network.ApiErrorMapper
import com.nexus.app.core.network.ApiException
import com.nexus.app.core.network.AuthTokenProvider
import com.nexus.app.core.network.longOrNull
import com.nexus.app.core.network.objectOrNull
import com.nexus.app.core.network.parseJsonObject
import com.nexus.app.core.network.stringOrNull
import com.nexus.app.core.network.toApiException
import com.nexus.app.core.util.Clock
import com.nexus.app.core.util.DefaultClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Single owner of the signed-in session. Plugs into [ApiClient] as its [AuthTokenProvider].
 *
 * - [state] is Initializing until [restore] ran, then SignedOut or Active.
 * - Tokens are refreshed under a [Mutex] when they expire within [REFRESH_WINDOW_SECONDS] (double-checked),
 *   and after a 401 ([refreshAfterUnauthorized]).
 * - A refresh that the server definitively rejects signs the user out locally; offline/server errors keep the session.
 * - Web-auth deep links are accepted only with the persisted one-time state (and nonce when returned) and after the
 *   access token was verified with `GET /auth/v1/user`.
 */
class SessionManager(
    private val api: ApiClient,
    private val storage: SessionStorage,
    private val flowStore: AuthFlowStore,
    private val clock: Clock = DefaultClock
) : AuthTokenProvider {

    private val _state = MutableStateFlow<SessionState>(SessionState.Initializing)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val refreshMutex = Mutex()
    private val random = SecureRandom()

    @Volatile
    private var session: UserSession? = null

    /** Incremented on every replacement or removal of the session; lets a slow refresh detect a sign-out. */
    @Volatile
    private var generation: Int = 0

    init {
        api.tokenProvider = this
    }

    /** Id of the signed-in user, or null (also null while the id is still unknown after a deep link). */
    val currentUserId: String?
        get() = session?.userId?.takeIf { it.isNotBlank() }

    val isSignedIn: Boolean get() = session != null

    private fun nowSeconds(): Long = clock.nowMillis() / 1000L

    // ---------------------------------------------------------------- sign in / restore

    /** Password grant. Returns the user id. Throws [ApiException]; nothing is stored on failure. */
    suspend fun signIn(identifier: String, password: String): String {
        val email = normalizeLoginIdentifier(identifier)
        if (password.isEmpty()) {
            throw ApiException(ApiErrorKind.Validation, "أدخل كلمة المرور")
        }
        val raw = api.authPost(
            "/auth/v1/token?grant_type=password",
            JSONObject().put("email", email).put("password", password)
        )
        val obj = raw.parseJsonObject() ?: throw unexpected()
        val fresh = sessionFrom(obj, null)
        install(fresh, persistStrict = true)
        return fresh.userId
    }

    /** Reads the stored session and publishes SignedOut or Active. Never throws (except cancellation). */
    suspend fun restore() {
        val stored = try {
            storage.read()?.let { UserSession.fromJson(it) }
        } catch (e: Exception) {
            null
        }
        if (stored == null) {
            _state.value = SessionState.SignedOut
            return
        }
        session = stored
        generation++
        try {
            // Refreshes when the token is about to expire; terminal rejection signs out inside.
            accessToken()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Transient failure: stay signed in offline.
        }
        val current = session
        if (current == null) {
            _state.value = SessionState.SignedOut
            return
        }
        if (current.userId.isBlank()) {
            try {
                val id = fetchUserId(current.accessToken)
                if (session?.refreshToken == current.refreshToken) {
                    install(current.copy(userId = id), persistStrict = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Offline: userId stays blank until the next successful call.
            }
        }
        val finalSession = session
        _state.value = if (finalSession == null) SessionState.SignedOut else SessionState.Active(finalSession.userId)
    }

    // ---------------------------------------------------------------- token provider

    override suspend fun accessToken(): String? {
        val current = session ?: return null
        if (current.expiresAt > nowSeconds() + REFRESH_WINDOW_SECONDS) return current.accessToken
        return refreshLocked(staleToken = null)
    }

    override suspend fun refreshAfterUnauthorized(staleToken: String): String? = refreshLocked(staleToken)

    /**
     * Double-checked refresh. With [staleToken] == null refreshes only when still inside the window;
     * with a stale token refreshes unless another caller already replaced that token.
     */
    private suspend fun refreshLocked(staleToken: String?): String? = refreshMutex.withLock {
        val current = session ?: return@withLock null
        if (staleToken == null) {
            if (current.expiresAt > nowSeconds() + REFRESH_WINDOW_SECONDS) return@withLock current.accessToken
        } else if (current.accessToken != staleToken) {
            return@withLock current.accessToken
        }
        val startedGeneration = generation
        val raw = try {
            api.authPost(
                "/auth/v1/token?grant_type=refresh_token",
                JSONObject().put("refresh_token", current.refreshToken)
            )
        } catch (e: ApiException) {
            if (isTerminalRefreshFailure(e) && generation == startedGeneration) {
                clearLocal()
                throw ApiException(
                    ApiErrorKind.Unauthorized,
                    ApiErrorMapper.MSG_SESSION_EXPIRED,
                    e.statusCode,
                    e.serverMessage,
                    e.errorCode,
                    e
                )
            }
            throw e
        }
        if (generation != startedGeneration) {
            // Signed out (or signed in again) while refreshing: drop this result.
            return@withLock session?.accessToken
        }
        val obj = raw.parseJsonObject() ?: throw unexpected()
        val fresh = sessionFrom(obj, current.userId)
        install(fresh, persistStrict = false)
        fresh.accessToken
    }

    private fun isTerminalRefreshFailure(e: ApiException): Boolean =
        e.kind == ApiErrorKind.Unauthorized || e.kind == ApiErrorKind.Forbidden ||
            e.statusCode == 400 || e.statusCode == 401 || e.statusCode == 403

    // ---------------------------------------------------------------- sign out

    /**
     * Clears the local session first, then revokes this device's refresh token with `?scope=local`
     * (best effort; other devices stay signed in). Never throws except for cancellation.
     */
    suspend fun signOut() {
        val token = session?.accessToken
        clearLocal()
        if (token == null) return
        try {
            api.requestWithToken("/auth/v1/logout?scope=local", "POST", JSONObject(), token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The server may be unreachable or the token already expired; local sign-out is complete.
        }
    }

    // ---------------------------------------------------------------- web-auth handoff

    /**
     * Generates and persists a one-time state and nonce. Put both into the web-auth URL (`state`, `nonce` query
     * parameters) before opening the browser.
     */
    fun beginWebAuth(): AuthFlow {
        val flow = AuthFlow(randomToken(), randomToken(), clock.nowMillis())
        flowStore.save(flow)
        return flow
    }

    /** Convenience wrapper for the real [Uri] delivered to the activity. */
    suspend fun acceptDeepLink(uri: Uri?): DeepLinkResult {
        if (uri == null) return DeepLinkResult.NotHandled
        return acceptDeepLink(uri.scheme, uri.host, uri.fragment)
    }

    /**
     * Validates `com.nexus.app://login-callback#access_token=..&refresh_token=..&state=..[&nonce=..][&type=recovery]`.
     * The pending flow is consumed (single use) whether the link is accepted or not.
     */
    suspend fun acceptDeepLink(scheme: String?, host: String?, fragment: String?): DeepLinkResult {
        if (scheme != CALLBACK_SCHEME || host != CALLBACK_HOST) return DeepLinkResult.NotHandled
        val params = parseFragment(fragment)
        val flow = try {
            flowStore.read()
        } catch (e: Exception) {
            null
        }
        try {
            flowStore.clear()
        } catch (e: Exception) {
            // Best effort.
        }
        if (params["error"] != null || params["error_code"] != null || params["error_description"] != null) {
            return DeepLinkResult.Rejected(MSG_LINK_ERROR)
        }
        if (flow == null || clock.nowMillis() - flow.createdAtMillis > FLOW_MAX_AGE_MILLIS) {
            return DeepLinkResult.Rejected(MSG_LINK_INVALID)
        }
        val returnedState = params["state"]
        if (returnedState.isNullOrEmpty() || !constantTimeEquals(returnedState, flow.state)) {
            return DeepLinkResult.Rejected(MSG_LINK_INVALID)
        }
        val returnedNonce = params["nonce"]
        if (returnedNonce != null && !constantTimeEquals(returnedNonce, flow.nonce)) {
            return DeepLinkResult.Rejected(MSG_LINK_INVALID)
        }
        val access = params["access_token"]
        val refresh = params["refresh_token"]
        if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
            return DeepLinkResult.Rejected(MSG_LINK_INVALID)
        }
        val userId = try {
            fetchUserId(access)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return DeepLinkResult.Rejected(MSG_LINK_INVALID)
        }
        val now = nowSeconds()
        val expiresAt = params["expires_at"]?.toLongOrNull()?.takeIf { it > now }
            ?: (now + (params["expires_in"]?.toLongOrNull()?.takeIf { it > 0L } ?: DEFAULT_EXPIRES_IN))
        try {
            install(UserSession(access, refresh, expiresAt, userId), persistStrict = true)
        } catch (e: ApiException) {
            return DeepLinkResult.Rejected(e.userMessage)
        }
        return DeepLinkResult.Accepted(recovery = params["type"] == "recovery")
    }

    // ---------------------------------------------------------------- internals

    private suspend fun fetchUserId(token: String): String {
        val raw = api.requestWithToken("/auth/v1/user", "GET", null, token)
        val id = raw.parseJsonObject()?.stringOrNull("id")
        if (id.isNullOrBlank()) {
            throw ApiException(ApiErrorKind.Unauthorized, ApiErrorMapper.MSG_INVALID_SESSION)
        }
        return id
    }

    /** Stores [fresh] in memory, persists it and publishes Active. */
    private fun install(fresh: UserSession, persistStrict: Boolean) {
        session = fresh
        generation++
        try {
            storage.save(fresh.toJson())
        } catch (e: Exception) {
            if (persistStrict) {
                session = null
                generation++
                throw e.toApiException()
            }
        }
        _state.value = SessionState.Active(fresh.userId)
    }

    private fun clearLocal() {
        session = null
        generation++
        try {
            storage.clear()
        } catch (e: Exception) {
            // Best effort; memory state is already cleared.
        }
        try {
            flowStore.clear()
        } catch (e: Exception) {
            // Best effort.
        }
        _state.value = SessionState.SignedOut
    }

    private fun sessionFrom(obj: JSONObject, fallbackUserId: String?): UserSession {
        val access = obj.stringOrNull("access_token") ?: throw unexpected()
        val refresh = obj.stringOrNull("refresh_token") ?: throw unexpected()
        val now = nowSeconds()
        val expiresAt = obj.longOrNull("expires_at")?.takeIf { it > 0L }
            ?: (now + (obj.longOrNull("expires_in")?.takeIf { it > 0L } ?: DEFAULT_EXPIRES_IN))
        val userId = obj.objectOrNull("user")?.stringOrNull("id") ?: fallbackUserId.orEmpty()
        return UserSession(access, refresh, expiresAt, userId)
    }

    private fun unexpected(): ApiException =
        ApiException(ApiErrorKind.Unknown, ApiErrorMapper.MSG_UNEXPECTED_RESPONSE)

    private fun randomToken(): String {
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val out = StringBuilder(32)
        for (b in bytes) {
            val v = b.toInt() and 0xff
            out.append(HEX[v shr 4]).append(HEX[v and 0x0f])
        }
        return out.toString()
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))

    companion object {
        const val REFRESH_WINDOW_SECONDS = 60L
        const val CALLBACK_SCHEME = "com.nexus.app"
        const val CALLBACK_HOST = "login-callback"
        const val FLOW_MAX_AGE_MILLIS = 10L * 60L * 1000L
        private const val DEFAULT_EXPIRES_IN = 3600L
        private const val HEX = "0123456789abcdef"
        const val MSG_LINK_INVALID = "رابط تسجيل الدخول غير صالح أو منتهي. حاول مرة أخرى"
        const val MSG_LINK_ERROR = "تعذّر تسجيل الدخول عبر الموقع. حاول مرة أخرى"

        /** Parses a URL fragment (`a=1&b=2`) into a map; values are percent-decoded. Malformed pairs are skipped. */
        fun parseFragment(fragment: String?): Map<String, String> {
            if (fragment.isNullOrEmpty()) return emptyMap()
            val result = LinkedHashMap<String, String>()
            for (pair in fragment.split('&')) {
                if (pair.isEmpty()) continue
                val idx = pair.indexOf('=')
                val rawKey = if (idx < 0) pair else pair.substring(0, idx)
                val rawValue = if (idx < 0) "" else pair.substring(idx + 1)
                try {
                    result[URLDecoder.decode(rawKey, "UTF-8")] = URLDecoder.decode(rawValue, "UTF-8")
                } catch (e: IllegalArgumentException) {
                    // Skip malformed escape sequences.
                }
            }
            return result
        }
    }
}
