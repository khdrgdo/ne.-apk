package com.nexus.app.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PostgrestQueryTest {
    @Test
    fun noFiltersGivesBasePath() {
        assertEquals("/rest/v1/posts", PostgrestQuery.from("posts").build())
    }

    @Test
    fun eqEncodesReservedCharacters() {
        val path = PostgrestQuery.from("t").eq("id", "a b&c=d").build()
        assertEquals("/rest/v1/t?id=eq.a%20b%26c%3Dd", path)
    }

    @Test
    fun fullQueryOrderIsSelectFiltersOrderLimitOffset() {
        val path = PostgrestQuery.from("posts")
            .select("*")
            .eq("user_id", "1")
            .order("created_at", ascending = false)
            .limit(20)
            .offset(40)
            .build()
        assertEquals("/rest/v1/posts?select=*&user_id=eq.1&order=created_at.desc&limit=20&offset=40", path)
    }

    @Test
    fun nullFilters() {
        assertEquals("/rest/v1/t?a=is.null&b=not.is.null", PostgrestQuery.from("t").isNull("a").isNotNull("b").build())
    }

    @Test
    fun escapeLikeEscapesWildcardsAndDropsStar() {
        assertEquals("50\\%\\_a\\\\", PostgrestQuery.escapeLike("50%_a*\\"))
    }

    @Test
    fun containsPatternWrapsEscapedText() {
        assertEquals("*a\\_b*", PostgrestQuery.containsPattern("a_b"))
    }

    @Test
    fun ilikeContainsEncodesPattern() {
        val path = PostgrestQuery.from("p").ilikeContains("name", "a%b").build()
        assertEquals("/rest/v1/p?name=ilike.*a%5C%25b*", path)
    }

    @Test
    fun ilikeStartsWithEscapesPrefix() {
        val path = PostgrestQuery.from("p").ilikeStartsWith("n", "a_").build()
        assertEquals("/rest/v1/p?n=ilike.a%5C_*", path)
    }

    @Test
    fun orFilterCannotInjectExtraClauses() {
        val path = PostgrestQuery.from("p").orIlikeContains(listOf("a"), "x,y)").build()
        assertEquals("/rest/v1/p?or=(a.ilike.%22*x%2Cy%29*%22)", path)
        assertFalse(path.contains("x,y"))
    }

    @Test
    fun inListQuotesUnsafeElements() {
        val path = PostgrestQuery.from("t").isIn("id", listOf("a", "b c", "d\"e")).build()
        assertEquals("/rest/v1/t?id=in.(a,%22b%20c%22,%22d%5C%22e%22)", path)
    }

    @Test
    fun inListKeepsUuidsReadable() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        assertEquals("($uuid)", PostgrestQuery.inList(listOf(uuid)))
    }

    @Test
    fun emptyInList() {
        assertEquals("()", PostgrestQuery.inList(emptyList()))
    }

    @Test
    fun quoteEscapesBackslashAndQuote() {
        assertEquals("\"a\\\"b\\\\c\"", PostgrestQuery.quote("a\"b\\c"))
    }
}
