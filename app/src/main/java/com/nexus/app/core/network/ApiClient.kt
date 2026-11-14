package com.nexus.app.core.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

/** Supplies bearer tokens to [ApiClient]; implemented by the session manager. */
interface AuthTokenProvider {
    /** A valid access token (refreshed when close to expiry), or null when nobody is signed in. */
    suspend fun accessToken(): String?

    /** Called after a 401 for [staleToken]; returns a new token or null. Throws [ApiException] when refreshing fails. */
    suspend fun refreshAfterUnauthorized(staleToken: String): String?
}

/**
 * HTTP access to Supabase (GoTrue, PostgREST, Storage).
 * Header rules: `apikey` always; `Authorization: Bearer` for REST, RPC, user, logout and storage calls;
 * never for the password grant, refresh, signup and recover calls ([authPost]).
 * Authenticated calls retry exactly once after a 401 with a refreshed token.
 * Coroutine cancellation cancels the underlying OkHttp call.
 */
class ApiClient(
    baseUrl: String,
    private val apiKey: String,
    httpClient: OkHttpClient = defaultHttpClient()
) {
    val baseUrl: String = baseUrl.trim().trimEnd('/')

    @Volatile
    var tokenProvider: AuthTokenProvider? = null

    private val http: OkHttpClient = httpClient

    /** Uploads (up to 100 MiB videos) must not be cut by the 45 s call timeout. */
    private val uploadHttp: OkHttpClient = httpClient.newBuilder()
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /** GoTrue endpoints that take the publishable key only (token, signup, recover). */
    suspend fun authPost(path: String, body: JSONObject): String =
        execute(http, buildRequest(path, "POST", body.toString().toRequestBody(JSON), emptyMap(), null))

    /** REST / auth call with Bearer token, 401 -> refresh -> retry once. Non-GET methods send `{}` when [body] is null. */
    suspend fun rest(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        headers: Map<String, String> = emptyMap()
    ): String = restRaw(path, method, body?.toString(), headers)

    /** Same as [rest] with a raw JSON body (allows arrays). */
    suspend fun restRaw(
        path: String,
        method: String = "GET",
        rawBody: String? = null,
        headers: Map<String, String> = emptyMap()
    ): String = authed(http) { token ->
        buildRequest(path, method, jsonBodyFor(method, rawBody), headers, token)
    }

    /** `POST /rest/v1/rpc/<name>`. */
    suspend fun rpc(name: String, body: JSONObject = JSONObject(), headers: Map<String, String> = emptyMap()): String =
        rest("/rest/v1/rpc/$name", "POST", body, headers)

    /** One request with an explicit token and no refresh or retry (logout, verifying a freshly received token). */
    suspend fun requestWithToken(path: String, method: String, body: JSONObject?, token: String): String =
        execute(http, buildRequest(path, method, jsonBodyFor(method, body?.toString()), emptyMap(), token))

    /** Streams [body] to `/storage/v1/object/<bucket>/<path>` using a token that is refreshed first. Returns the response body. */
    suspend fun upload(bucket: String, path: String, contentType: String, body: RequestBody): String =
        authed(uploadHttp) { token ->
            Request.Builder()
                .url(baseUrl + "/storage/v1/object/" + bucket + "/" + encodeStoragePath(path))
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", contentType)
                .header("x-upsert", "false")
                .post(body)
                .build()
        }

    /** Signs a storage object. Returns an absolute URL. */
    suspend fun signedUrl(bucket: String, path: String, expiresInSeconds: Int = 3600): String {
        val raw = rest(
            "/storage/v1/object/sign/" + bucket + "/" + encodeStoragePath(path),
            "POST",
            JSONObject().put("expiresIn", expiresInSeconds)
        )
        val obj = raw.parseJsonObject()
        val signed = obj?.stringOrNull("signedURL") ?: obj?.stringOrNull("signedUrl")
            ?: throw ApiException(ApiErrorKind.Unknown, "تعذّر تحميل الصورة")
        return absoluteStorageUrl(signed)
    }

    /** Deletes one storage object. */
    suspend fun deleteObject(bucket: String, path: String) {
        rest("/storage/v1/object/" + bucket + "/" + encodeStoragePath(path), "DELETE")
    }

    /** Turns a (possibly relative) signed URL returned by Storage into an absolute one. */
    fun absoluteStorageUrl(signed: String): String {
        if (signed.startsWith("http://") || signed.startsWith("https://")) return signed
        return baseUrl + "/storage/v1" + (if (signed.startsWith("/")) signed else "/$signed")
    }

    private fun jsonBodyFor(method: String, raw: String?): RequestBody? =
        if (method.equals("GET", ignoreCase = true) || method.equals("HEAD", ignoreCase = true)) null
        else (raw ?: "{}").toRequestBody(JSON)

    private fun buildRequest(
        path: String,
        method: String,
        body: RequestBody?,
        headers: Map<String, String>,
        token: String?
    ): Request {
        val builder = Request.Builder().url(baseUrl + path).header("apikey", apiKey)
        if (token != null) builder.header("Authorization", "Bearer $token")
        for ((name, value) in headers) builder.header(name, value)
        builder.method(method.uppercase(), body)
        return builder.build()
    }

    private suspend fun authed(client: OkHttpClient, build: (String) -> Request): String {
        val provider = tokenProvider ?: throw ApiException(ApiErrorKind.Unauthorized, ApiErrorMapper.MSG_SESSION_EXPIRED)
        val token = provider.accessToken()
            ?: throw ApiException(ApiErrorKind.Unauthorized, ApiErrorMapper.MSG_SESSION_EXPIRED)
        try {
            return execute(client, build(token))
        } catch (first: ApiException) {
            if (first.kind != ApiErrorKind.Unauthorized) throw first
            val fresh = provider.refreshAfterUnauthorized(token) ?: throw first
            return execute(client, build(fresh))
        }
    }

    private suspend fun execute(client: OkHttpClient, request: Request): String =
        suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(e.toApiException())
                }

                override fun onResponse(call: Call, response: Response) {
                    val outcome: Result<String> = try {
                        response.use { r ->
                            val raw = r.body?.string().orEmpty()
                            if (r.isSuccessful) {
                                Result.success(raw)
                            } else {
                                Result.failure<String>(ApiErrorMapper.fromResponse(r.code, raw))
                            }
                        }
                    } catch (e: IOException) {
                        Result.failure<String>(e.toApiException())
                    }
                    if (continuation.isActive) continuation.resumeWith(outcome)
                }
            })
        }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()

        fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .build()
    }
}
