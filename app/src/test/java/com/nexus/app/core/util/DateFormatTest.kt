package com.nexus.app.core.util

import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DateFormatTest {
    private val base = 1714557600000L // 2024-05-01T10:00:00Z

    @Test
    fun parsesZuluAndOffsets() {
        assertEquals(base, DateFormat.parse("2024-05-01T10:00:00Z")?.toEpochMilli())
        assertEquals(base, DateFormat.parse("2024-05-01T10:00:00+00:00")?.toEpochMilli())
        assertEquals(base, DateFormat.parse("2024-05-01T10:00:00+00")?.toEpochMilli())
        assertEquals(base, DateFormat.parse("2024-05-01T10:00:00+0000")?.toEpochMilli())
        assertEquals(base, DateFormat.parse("2024-05-01T12:00:00+02:00")?.toEpochMilli())
        assertEquals(base, DateFormat.parse("2024-05-01T05:00:00-05:00")?.toEpochMilli())
    }

    @Test
    fun parsesPostgresStyleWithSpaceAndMicros() {
        assertEquals(base + 123L, DateFormat.parse("2024-05-01 10:00:00.123000+00:00")?.toEpochMilli())
    }

    @Test
    fun missingOffsetIsUtc() {
        assertEquals(base, DateFormat.parse("2024-05-01T10:00:00")?.toEpochMilli())
    }

    @Test
    fun plainDateIsStartOfDayUtc() {
        assertEquals(1714521600000L, DateFormat.parse("2024-05-01")?.toEpochMilli())
    }

    @Test
    fun garbageIsNull() {
        assertNull(DateFormat.parse(null))
        assertNull(DateFormat.parse(""))
        assertNull(DateFormat.parse("not a date"))
        assertEquals(0L, DateFormat.toEpochMillis("nope"))
    }

    @Test
    fun futureCheck() {
        assertTrue(DateFormat.isInFuture("2024-05-01T10:00:01Z", base))
        assertFalse(DateFormat.isInFuture("2024-05-01T10:00:00Z", base))
        assertFalse(DateFormat.isInFuture("bad", base))
    }

    private fun rel(secondsAgo: Long): String {
        val now = Instant.ofEpochMilli(base)
        val then = now.minusSeconds(secondsAgo).toString()
        return DateFormat.relativeArabic(then, now, ZoneOffset.UTC)
    }

    @Test
    fun relativeTimeInArabic() {
        assertEquals("الآن", rel(30))
        assertEquals("منذ دقيقة", rel(60))
        assertEquals("منذ دقيقتين", rel(120))
        assertEquals("منذ 5 دقائق", rel(5 * 60))
        assertEquals("منذ 11 دقيقة", rel(11 * 60))
        assertEquals("منذ ساعة", rel(3600))
        assertEquals("منذ ساعتين", rel(2 * 3600))
        assertEquals("منذ 5 ساعات", rel(5 * 3600))
        assertEquals("أمس", rel(30 * 3600))
        assertEquals("منذ 3 أيام", rel(3 * 86400))
        assertEquals("منذ 15 يوماً", rel(15 * 86400))
        assertEquals("2024/03/22", rel(40 * 86400))
    }

    @Test
    fun relativeOfUnparseableIsEmpty() {
        assertEquals("", DateFormat.relativeArabic("bad"))
    }

    @Test
    fun dayBuckets() {
        val now = Instant.parse("2024-05-01T10:00:00Z")
        assertEquals(DateFormat.DayBucket.TODAY, DateFormat.dayBucket("2024-05-01T00:30:00Z", now, ZoneOffset.UTC))
        assertEquals(DateFormat.DayBucket.YESTERDAY, DateFormat.dayBucket("2024-04-30T23:59:00Z", now, ZoneOffset.UTC))
        assertEquals(DateFormat.DayBucket.EARLIER, DateFormat.dayBucket("2024-04-20T10:00:00Z", now, ZoneOffset.UTC))
        assertEquals(DateFormat.DayBucket.EARLIER, DateFormat.dayBucket("bad", now, ZoneOffset.UTC))
    }
}
