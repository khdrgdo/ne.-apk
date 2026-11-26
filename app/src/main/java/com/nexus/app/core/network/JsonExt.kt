package com.nexus.app.core.network

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/*
 * Null-safe readers for org.json.
 * Android's JSONObject.optString(key, default) returns the literal text "null" for a JSON null value.
 * Every reader here treats missing keys and JSON null identically and returns the caller's default instead.
 */

/** Text value, or null when the key is missing, JSON null or blank. */
fun JSONObject.stringOrNull(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val value = opt(key) ?: return null
    val text = when (value) {
        is String -> value
        is Number -> value.toString()
        is Boolean -> value.toString()
        else -> return null
    }
    return if (text.isBlank()) null else text
}

fun JSONObject.string(key: String, default: String = ""): String = stringOrNull(key) ?: default

fun JSONObject.intOrNull(key: String): Int? = when (val value = opt(key)) {
    is Number -> value.toInt()
    is String -> value.trim().toIntOrNull()
    else -> null
}

fun JSONObject.int(key: String, default: Int = 0): Int = intOrNull(key) ?: default

fun JSONObject.longOrNull(key: String): Long? = when (val value = opt(key)) {
    is Number -> value.toLong()
    is String -> value.trim().toLongOrNull()
    else -> null
}

fun JSONObject.long(key: String, default: Long = 0L): Long = longOrNull(key) ?: default

fun JSONObject.boolOrNull(key: String): Boolean? = when (val value = opt(key)) {
    is Boolean -> value
    is String -> when (value.trim().lowercase()) {
        "true", "t", "1" -> true
        "false", "f", "0" -> false
        else -> null
    }
    is Number -> value.toInt() != 0
    else -> null
}

fun JSONObject.bool(key: String, default: Boolean = false): Boolean = boolOrNull(key) ?: default

fun JSONObject.objectOrNull(key: String): JSONObject? = if (isNull(key)) null else optJSONObject(key)

fun JSONObject.arrayOrNull(key: String): JSONArray? = if (isNull(key)) null else optJSONArray(key)

/** Objects of the array stored under [key] (empty when missing or JSON null). */
fun JSONObject.objectsOrEmpty(key: String): List<JSONObject> = arrayOrNull(key)?.objects() ?: emptyList()

/** Non-blank strings of the array stored under [key] (empty when missing or JSON null). */
fun JSONObject.stringsOrEmpty(key: String): List<String> = arrayOrNull(key)?.strings() ?: emptyList()

/** Reads a JSON object whose values are strings (for example poll votes) into a map. */
fun JSONObject.toStringMap(): Map<String, String> {
    val result = LinkedHashMap<String, String>()
    val names = keys()
    while (names.hasNext()) {
        val name = names.next()
        val value = stringOrNull(name)
        if (value != null) result[name] = value
    }
    return result
}

fun JSONArray.objects(): List<JSONObject> {
    val result = ArrayList<JSONObject>(length())
    for (index in 0 until length()) {
        val item = optJSONObject(index)
        if (item != null) result.add(item)
    }
    return result
}

fun JSONArray.strings(): List<String> {
    val result = ArrayList<String>(length())
    for (index in 0 until length()) {
        if (isNull(index)) continue
        val text = optString(index, "")
        if (text.isNotBlank()) result.add(text)
    }
    return result
}

private fun unexpectedResponse(cause: Throwable): ApiException =
    ApiException(ApiErrorKind.Unknown, ApiErrorMapper.MSG_UNEXPECTED_RESPONSE, cause = cause)

/** Parses a PostgREST/RPC body into objects. Blank body is empty; a single object becomes a one-element list. */
fun String.parseJsonObjects(): List<JSONObject> {
    val text = trim()
    if (text.isEmpty()) return emptyList()
    return try {
        when (text[0]) {
            '[' -> JSONArray(text).objects()
            '{' -> listOf(JSONObject(text))
            else -> emptyList()
        }
    } catch (e: JSONException) {
        throw unexpectedResponse(e)
    }
}

/** First object of the body (array or object), or null when there is none. */
fun String.parseJsonObject(): JSONObject? = parseJsonObjects().firstOrNull()

/** Parses a JSON array of strings; returns an empty list for blank or non-array bodies. */
fun String.parseJsonStrings(): List<String> {
    val text = trim()
    if (text.isEmpty() || text[0] != '[') return emptyList()
    return try {
        JSONArray(text).strings()
    } catch (e: JSONException) {
        throw unexpectedResponse(e)
    }
}

/**
 * Extracts an id returned by an RPC: a bare JSON string ("uuid"), a bare uuid, an object with `id`,
 * or an array whose first element is one of those.
 */
fun String.extractId(): String? {
    val text = trim()
    if (text.isEmpty()) return null
    return try {
        when (text[0]) {
            '{' -> JSONObject(text).stringOrNull("id")
            '[' -> {
                val array = JSONArray(text)
                if (array.length() == 0) {
                    null
                } else {
                    val first = array.opt(0)
                    when (first) {
                        is JSONObject -> first.stringOrNull("id")
                        is String -> first.takeIf { it.isNotBlank() }
                        else -> null
                    }
                }
            }
            else -> text.trim('"').takeIf { it.isNotBlank() && it != "null" }
        }
    } catch (e: JSONException) {
        null
    }
}

/** Converts Kotlin values to org.json values: null becomes JSONObject.NULL, collections and maps are expanded. */
fun toJsonValue(value: Any?): Any = when (value) {
    null -> JSONObject.NULL
    is JSONObject -> value
    is JSONArray -> value
    is Collection<*> -> {
        val array = JSONArray()
        for (item in value) array.put(toJsonValue(item))
        array
    }
    is Map<*, *> -> {
        val obj = JSONObject()
        for ((k, v) in value) obj.put(k.toString(), toJsonValue(v))
        obj
    }
    else -> value ?: JSONObject.NULL
}

/** Builds a JSONObject from pairs; null values are sent as JSON null (not omitted). */
fun jsonObjectOf(vararg pairs: Pair<String, Any?>): JSONObject {
    val obj = JSONObject()
    for ((key, value) in pairs) obj.put(key, toJsonValue(value))
    return obj
}

/** Builds a JSONArray from the given items (each converted with [toJsonValue]). */
fun jsonArrayOf(items: Collection<*>): JSONArray = toJsonValue(items) as JSONArray
