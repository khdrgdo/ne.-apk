package com.nexus.app.domain.model

import java.io.InputStream

/**
 * One page of a cursor-paginated list. [nextCursor] is null when there is nothing more to load.
 * The cursor is an opaque string produced by the repository (the `created_at` of the last row).
 */
data class Page<T>(val items: List<T>, val nextCursor: String?) {
    val hasMore: Boolean get() = nextCursor != null

    companion object {
        fun <T> empty(): Page<T> = Page(emptyList(), null)
    }
}

/** A local file chosen by the user, abstracted away from android.net.Uri so the domain stays pure Kotlin. */
class UploadSource(
    val displayName: String,
    val mimeType: String,
    /** Size in bytes, or -1 when unknown. */
    val sizeBytes: Long,
    /** Opens a NEW stream on every call (so retries work). The caller closes it. */
    val open: () -> InputStream
)

/** App appearance (device storage only). */
enum class ThemeMode(val label: String) {
    SYSTEM("تلقائي"),
    LIGHT("فاتح"),
    DARK("داكن");

    companion object {
        fun from(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/** UI language. */
enum class AppLanguage(val tag: String) {
    AR("ar"),
    EN("en");

    companion object {
        fun from(value: String?): AppLanguage = entries.firstOrNull { it.tag == value || it.name == value } ?: AR
    }
}

/** Converts what the user typed in the login box into the auth e-mail (university no. / sub-admin name / e-mail). */
fun normalizeLoginIdentifier(value: String): String {
    val cleaned = value.trim()
    if (cleaned.isEmpty()) throw IllegalArgumentException("أدخل الرقم الجامعي أو البريد الإلكتروني")
    if (cleaned.contains("@")) return cleaned
    if (cleaned.all { it.isDigit() }) return cleaned + "@college.edu"
    val username = cleaned.lowercase().replace(Regex("\\s+"), "").removePrefix("sub_")
    if (username.isNotBlank()) return username + "@subadmin.edu"
    throw IllegalArgumentException("أدخل الرقم الجامعي أو اسم المشرف المساعد أو البريد الإلكتروني")
}

fun majorLabel(code: String?): String = when (code?.lowercase()) {
    "it" -> "تقنية المعلومات"
    "is" -> "نظم المعلومات"
    "se" -> "هندسة البرمجيات"
    else -> "التخصص غير محدد"
}
