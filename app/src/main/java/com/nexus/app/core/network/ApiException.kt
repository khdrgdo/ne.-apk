package com.nexus.app.core.network

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InterruptedIOException

/** Coarse classification of every failure the data layer can produce. */
enum class ApiErrorKind { Network, Unauthorized, Forbidden, NotFound, Validation, Server, Unknown }

/**
 * The only exception type repositories throw.
 * [userMessage] is always safe to show in the UI (Arabic). [serverMessage] keeps the raw server text for logic
 * (for example checking whether a message mentions a column) and must not be shown directly.
 * `message` equals [userMessage].
 */
class ApiException(
    val kind: ApiErrorKind,
    val userMessage: String,
    val statusCode: Int = 0,
    val serverMessage: String? = null,
    val errorCode: String? = null,
    cause: Throwable? = null
) : Exception(userMessage, cause) {
    /** True for failures that are worth retrying later (offline, timeout, 5xx). */
    val isTransient: Boolean get() = kind == ApiErrorKind.Network || kind == ApiErrorKind.Server
}

/** Thrown from a streaming request body when the payload exceeds its limit; message is user-presentable. */
class UploadLimitException(message: String) : IOException(message)

/** Maps HTTP responses and throwables to [ApiException] with Arabic messages. */
object ApiErrorMapper {
    const val MSG_NETWORK = "تعذّر الاتصال بالخادم. تحقق من اتصال الإنترنت وحاول مرة أخرى"
    const val MSG_TIMEOUT = "انتهت مهلة الاتصال. حاول مرة أخرى"
    const val MSG_SESSION_EXPIRED = "انتهت جلسة الدخول. سجّل الدخول مرة أخرى."
    const val MSG_INVALID_SESSION = "جلسة غير صالحة"
    const val MSG_FORBIDDEN = "ليست لديك صلاحية لتنفيذ هذا الإجراء"
    const val MSG_NOT_FOUND = "العنصر المطلوب غير موجود"
    const val MSG_VALIDATION = "البيانات المدخلة غير صالحة"
    const val MSG_SERVER = "حدث خطأ في الخادم. حاول لاحقاً"
    const val MSG_RATE_LIMIT = "عدد المحاولات كبير. انتظر قليلاً ثم حاول مرة أخرى"
    const val MSG_UNKNOWN = "حدث خطأ غير متوقع"
    const val MSG_UNEXPECTED_RESPONSE = "استجابة غير متوقعة من الخادم"
    const val MSG_INVALID_CREDENTIALS = "بيانات الدخول غير صحيحة. تحقق من الرقم الجامعي وكلمة المرور"

    private val TRANSLATIONS: List<Pair<String, String>> = listOf(
        "invalid login credentials" to MSG_INVALID_CREDENTIALS,
        "email not confirmed" to "لم يتم تأكيد البريد الإلكتروني بعد",
        "user already registered" to "هذا الحساب مسجّل مسبقاً",
        "already been registered" to "هذا الحساب مسجّل مسبقاً",
        "new password should be different" to "يجب أن تختلف كلمة المرور الجديدة عن الحالية",
        "password should be at least" to "كلمة المرور قصيرة جداً",
        "unable to validate email address" to "البريد الإلكتروني غير صالح",
        "user not found" to "المستخدم غير موجود",
        "invalid refresh token" to MSG_SESSION_EXPIRED,
        "refresh_token_not_found" to MSG_SESSION_EXPIRED,
        "refresh token not found" to MSG_SESSION_EXPIRED,
        "refresh_token_already_used" to MSG_SESSION_EXPIRED,
        "jwt expired" to MSG_SESSION_EXPIRED,
        "invalid jwt" to MSG_SESSION_EXPIRED,
        "over_email_send_rate_limit" to MSG_RATE_LIMIT,
        "rate limit" to MSG_RATE_LIMIT,
        "too many requests" to MSG_RATE_LIMIT,
        "row-level security" to MSG_FORBIDDEN,
        "permission denied" to MSG_FORBIDDEN,
        "duplicate key" to "هذا العنصر موجود مسبقاً",
        "violates foreign key" to "لا يمكن إكمال العملية لارتباط العنصر ببيانات أخرى"
    )

    fun kindForStatus(status: Int): ApiErrorKind = when {
        status == 401 -> ApiErrorKind.Unauthorized
        status == 403 -> ApiErrorKind.Forbidden
        status == 404 -> ApiErrorKind.NotFound
        status == 400 || status == 409 || status == 422 -> ApiErrorKind.Validation
        status == 429 || status >= 500 -> ApiErrorKind.Server
        else -> ApiErrorKind.Unknown
    }

    /** True when [text] contains Arabic letters (server functions sometimes raise Arabic messages already). */
    fun containsArabic(text: String): Boolean = text.any { it.code in 0x0600..0x06FF }

    /** Arabic text for a known server message or code, or null when nothing matches. */
    fun translate(serverMessage: String?, code: String?): String? {
        if (!serverMessage.isNullOrBlank() && containsArabic(serverMessage)) return serverMessage
        val haystack = ((serverMessage ?: "") + " " + (code ?: "")).lowercase()
        for ((needle, arabic) in TRANSLATIONS) {
            if (haystack.contains(needle)) return arabic
        }
        return when (code) {
            "42501" -> MSG_FORBIDDEN
            "23505" -> "هذا العنصر موجود مسبقاً"
            "23503" -> "لا يمكن إكمال العملية لارتباط العنصر ببيانات أخرى"
            "PGRST301" -> MSG_SESSION_EXPIRED
            "PGRST116" -> MSG_NOT_FOUND
            else -> null
        }
    }

    /** Builds the exception for a non-2xx response. [raw] is the response body (JSON or anything else). */
    fun fromResponse(status: Int, raw: String): ApiException {
        var serverMessage: String? = null
        var code: String? = null
        var isJson = false
        try {
            val obj = JSONObject(raw.trim())
            isJson = true
            serverMessage = firstText(obj, "msg", "message", "error_description")
            if (serverMessage == null) serverMessage = firstText(obj, "error")
            code = firstText(obj, "error_code", "code")
        } catch (e: JSONException) {
            isJson = false
        }
        var kind = kindForStatus(status)
        if (code == "42501") kind = ApiErrorKind.Forbidden
        if (code == "PGRST301") kind = ApiErrorKind.Unauthorized
        val message = translate(serverMessage, code) ?: when (kind) {
            ApiErrorKind.Unauthorized -> MSG_SESSION_EXPIRED
            ApiErrorKind.Forbidden -> MSG_FORBIDDEN
            ApiErrorKind.NotFound -> MSG_NOT_FOUND
            ApiErrorKind.Server -> if (status == 429) MSG_RATE_LIMIT else MSG_SERVER
            ApiErrorKind.Validation, ApiErrorKind.Unknown, ApiErrorKind.Network ->
                if (isJson) "تعذّر إكمال الطلب ($status)" else "تعذّر الاتصال بالخادم ($status)"
        }
        return ApiException(kind, message, status, serverMessage, code)
    }

    /** Converts any throwable (never CancellationException; callers rethrow that first) to an [ApiException]. */
    fun fromThrowable(t: Throwable): ApiException {
        if (t is ApiException) return t
        return when (t) {
            is UploadLimitException -> ApiException(ApiErrorKind.Validation, t.message ?: MSG_VALIDATION, cause = t)
            is InterruptedIOException -> ApiException(ApiErrorKind.Network, MSG_TIMEOUT, cause = t)
            is IOException -> ApiException(ApiErrorKind.Network, MSG_NETWORK, cause = t)
            is JSONException -> ApiException(ApiErrorKind.Unknown, MSG_UNEXPECTED_RESPONSE, cause = t)
            is IllegalArgumentException -> ApiException(ApiErrorKind.Validation, arabicOr(t.message, MSG_VALIDATION), cause = t)
            else -> ApiException(ApiErrorKind.Unknown, arabicOr(t.message, MSG_UNKNOWN), cause = t)
        }
    }

    private fun arabicOr(text: String?, fallback: String): String =
        if (!text.isNullOrBlank() && containsArabic(text)) text else fallback

    private fun firstText(obj: JSONObject, vararg keys: String): String? {
        for (key in keys) {
            if (!obj.has(key) || obj.isNull(key)) continue
            val value = obj.opt(key)
            val text = when (value) {
                is String -> value
                is Number -> value.toString()
                else -> null
            }
            if (!text.isNullOrBlank()) return text
        }
        return null
    }
}

fun Throwable.toApiException(): ApiException = ApiErrorMapper.fromThrowable(this)
