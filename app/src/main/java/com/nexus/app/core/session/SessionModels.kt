package com.nexus.app.core.session

import com.nexus.app.core.network.long
import com.nexus.app.core.network.string
import org.json.JSONException
import org.json.JSONObject

/** Tokens of a signed-in user. [expiresAt] is epoch seconds. [userId] may be blank right after a deep link. */
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val userId: String
) {
    /** Payload stored encrypted on disk; keys are identical to the 1.x app so existing logins keep working. */
    fun toJson(): String = JSONObject()
        .put("access_token", accessToken)
        .put("refresh_token", refreshToken)
        .put("expires_at", expiresAt)
        .put("user_id", userId)
        .toString()

    companion object {
        /** Decodes the stored payload; null when malformed or when a token is blank. */
        fun fromJson(raw: String): UserSession? = try {
            val obj = JSONObject(raw)
            val access = obj.string("access_token")
            val refresh = obj.string("refresh_token")
            if (access.isBlank() || refresh.isBlank()) {
                null
            } else {
                UserSession(access, refresh, obj.long("expires_at"), obj.string("user_id"))
            }
        } catch (e: JSONException) {
            null
        }
    }
}

/** Observable authentication state. */
sealed interface SessionState {
    /** Stored session not read yet (splash). */
    data object Initializing : SessionState

    data object SignedOut : SessionState

    data class Active(val userId: String) : SessionState
}

/** Outcome of handling a `com.nexus.app://login-callback` link. */
sealed interface DeepLinkResult {
    /** The link is not a login callback; ignore it. */
    data object NotHandled : DeepLinkResult

    /** Tokens were validated and the session is active. [recovery] is true for password-recovery links. */
    data class Accepted(val recovery: Boolean) : DeepLinkResult

    /** The link was refused; [message] is Arabic and safe to show. */
    data class Rejected(val message: String) : DeepLinkResult
}

/** Pending web-auth round trip persisted before the browser is opened. */
data class AuthFlow(val state: String, val nonce: String, val createdAtMillis: Long)

/** Encrypted persistence of the session JSON. */
interface SessionStorage {
    fun save(value: String)
    fun read(): String?
    fun clear()
}

/** Persistence of the pending [AuthFlow]. */
interface AuthFlowStore {
    fun save(flow: AuthFlow)
    fun read(): AuthFlow?
    fun clear()
}
