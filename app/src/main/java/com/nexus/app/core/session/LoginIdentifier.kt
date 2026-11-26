package com.nexus.app.core.session

import com.nexus.app.core.network.ApiErrorKind
import com.nexus.app.core.network.ApiException

const val STUDENT_EMAIL_DOMAIN = "college.edu"
const val SUBADMIN_EMAIL_DOMAIN = "subadmin.edu"
const val SUBADMIN_NUMBER_PREFIX = "sub_"

private const val MSG_ENTER_ID = "أدخل الرقم الجامعي أو البريد الإلكتروني"
private const val MSG_ENTER_ID_OR_SUB = "أدخل الرقم الجامعي أو اسم المشرف المساعد أو البريد الإلكتروني"
private const val MSG_SUBADMIN_ID_REQUIRED = "أدخل اسم دخول للمشرف المساعد"
private const val MSG_SUBADMIN_ID_DIGITS = "اسم دخول المشرف المساعد يجب أن يحتوي على حرف واحد على الأقل"

private fun validation(message: String): ApiException = ApiException(ApiErrorKind.Validation, message)

/** Converts Arabic-Indic and other Unicode decimal digits to ASCII; other characters are unchanged. */
fun toAsciiDigits(text: String): String {
    val out = StringBuilder(text.length)
    for (ch in text) {
        val digit = Character.digit(ch, 10)
        if (Character.isDigit(ch) && digit >= 0) out.append(('0'.code + digit).toChar()) else out.append(ch)
    }
    return out.toString()
}

/** Lowercase, no whitespace, no leading `sub_`. Result may be blank. */
fun cleanSubAdminLoginId(loginId: String): String =
    loginId.trim().lowercase().replace(Regex("\\s+"), "").removePrefix(SUBADMIN_NUMBER_PREFIX)

/**
 * Maps what the user typed at sign-in to the GoTrue email.
 * email (contains @) -> unchanged; digits only -> `<digits>@college.edu`; anything else is a sub-admin
 * login name -> `<name>@subadmin.edu` (a leading `sub_` is ignored). Throws a Validation [ApiException].
 */
fun normalizeLoginIdentifier(value: String): String {
    val cleaned = value.trim()
    if (cleaned.isEmpty()) throw validation(MSG_ENTER_ID)
    if (cleaned.contains("@")) return cleaned
    if (cleaned.all { Character.isDigit(it) }) return toAsciiDigits(cleaned) + "@" + STUDENT_EMAIL_DOMAIN
    val username = cleanSubAdminLoginId(cleaned)
    if (username.isNotBlank()) return username + "@" + SUBADMIN_EMAIL_DOMAIN
    throw validation(MSG_ENTER_ID_OR_SUB)
}

/**
 * Email used when an admin creates a sub-admin account. Uses the same cleaning as sign-in (so the `sub_` prefix
 * is stripped and the account can sign in with the id that was typed) and rejects digit-only ids, which sign-in
 * would route to `@college.edu`.
 */
fun subAdminEmailFor(loginId: String): String {
    val clean = cleanSubAdminLoginId(loginId)
    if (clean.isBlank()) throw validation(MSG_SUBADMIN_ID_REQUIRED)
    if (clean.all { Character.isDigit(it) }) throw validation(MSG_SUBADMIN_ID_DIGITS)
    if (clean.contains("@")) throw validation(MSG_SUBADMIN_ID_REQUIRED)
    return clean + "@" + SUBADMIN_EMAIL_DOMAIN
}

/** University-number value stored in the profile of a sub-admin (`sub_<id>`). */
fun subAdminUniversityNumberFor(loginId: String): String = SUBADMIN_NUMBER_PREFIX + cleanSubAdminLoginId(loginId)
