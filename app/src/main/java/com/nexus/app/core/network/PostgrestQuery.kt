package com.nexus.app.core.network

/**
 * Safe builder for PostgREST read/update/delete URLs.
 * Every value is percent-encoded; ilike patterns escape LIKE wildcards; values placed in `in.(...)` or `or=(...)`
 * are double-quoted when they contain reserved characters so user input can never add extra filter clauses.
 */
class PostgrestQuery private constructor(private val table: String) {
    private var select: String? = null
    private val filters = mutableListOf<String>()
    private val orders = mutableListOf<String>()
    private var limit: Int? = null
    private var offset: Int? = null

    fun select(columns: String): PostgrestQuery = apply { select = columns }

    /** Generic filter `column=operator.value` (value is encoded). */
    fun filter(column: String, operator: String, value: String): PostgrestQuery =
        apply { filters.add("$column=$operator.${urlEncode(value)}") }

    fun eq(column: String, value: String) = filter(column, "eq", value)
    fun neq(column: String, value: String) = filter(column, "neq", value)
    fun gt(column: String, value: String) = filter(column, "gt", value)
    fun gte(column: String, value: String) = filter(column, "gte", value)
    fun lt(column: String, value: String) = filter(column, "lt", value)
    fun lte(column: String, value: String) = filter(column, "lte", value)

    fun isNull(column: String): PostgrestQuery = apply { filters.add("$column=is.null") }
    fun isNotNull(column: String): PostgrestQuery = apply { filters.add("$column=not.is.null") }

    /** `column=in.(a,b,c)` with safe quoting of every element. */
    fun isIn(column: String, values: Collection<String>): PostgrestQuery =
        apply { filters.add("$column=in.${inList(values)}") }

    /** `column=ilike.*text*` where LIKE wildcards inside [text] are escaped. */
    fun ilikeContains(column: String, text: String): PostgrestQuery =
        apply { filters.add("$column=ilike.${urlEncode(containsPattern(text))}") }

    /** `column=like.prefix%` style prefix match using ilike, with wildcards in [prefix] escaped. */
    fun ilikeStartsWith(column: String, prefix: String): PostgrestQuery =
        apply { filters.add("$column=ilike.${urlEncode(escapeLike(prefix) + "*")}") }

    /** `or=(col.ilike."*text*",...)` with the pattern quoted so ',', '(' and ')' in [text] are harmless. */
    fun orIlikeContains(columns: List<String>, text: String): PostgrestQuery {
        val pattern = urlEncode(quote(containsPattern(text)))
        val clauses = columns.joinToString(",") { "$it.ilike.$pattern" }
        filters.add("or=($clauses)")
        return this
    }

    fun order(column: String, ascending: Boolean = true): PostgrestQuery =
        apply { orders.add(column + "." + if (ascending) "asc" else "desc") }

    fun limit(count: Int): PostgrestQuery = apply { limit = count }
    fun offset(count: Int): PostgrestQuery = apply { offset = count }

    /** Returns the request path including `/rest/v1/` and the query string. */
    fun build(): String {
        val parts = mutableListOf<String>()
        select?.let { parts.add("select=$it") }
        parts.addAll(filters)
        if (orders.isNotEmpty()) parts.add("order=" + orders.joinToString(","))
        limit?.let { parts.add("limit=$it") }
        offset?.let { parts.add("offset=$it") }
        val base = "/rest/v1/$table"
        return if (parts.isEmpty()) base else base + "?" + parts.joinToString("&")
    }

    override fun toString(): String = build()

    companion object {
        private val SAFE_LIST_VALUE = Regex("^[A-Za-z0-9_.:@-]+$")

        fun from(table: String): PostgrestQuery = PostgrestQuery(table)

        /** Escapes LIKE metacharacters (backslash, %, _) and removes '*' (PostgREST's own wildcard). */
        fun escapeLike(text: String): String {
            val out = StringBuilder(text.length + 8)
            for (ch in text) {
                when (ch) {
                    '*' -> Unit
                    '\\', '%', '_' -> out.append('\\').append(ch)
                    else -> out.append(ch)
                }
            }
            return out.toString()
        }

        /** `*escaped*` pattern for "contains" searches (not yet percent-encoded). */
        fun containsPattern(text: String): String = "*" + escapeLike(text) + "*"

        /** Wraps [value] in double quotes, escaping backslash and double quote (PostgREST reserved-character quoting). */
        fun quote(value: String): String {
            val out = StringBuilder(value.length + 2)
            out.append('"')
            for (ch in value) {
                if (ch == '\\' || ch == '"') out.append('\\')
                out.append(ch)
            }
            out.append('"')
            return out.toString()
        }

        /** `(a,b,c)` list for `in.` filters; elements with reserved characters are quoted, then percent-encoded. */
        fun inList(values: Collection<String>): String =
            values.joinToString(",", "(", ")") { value ->
                urlEncode(if (SAFE_LIST_VALUE.matches(value)) value else quote(value))
            }
    }
}
