package com.nexus.app.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/** Tolerant ISO-8601 parsing (PostgREST style) and Arabic relative time. */
object DateFormat {
    private val SHORT_OFFSET = Regex("^[+-]\\d{2}$")
    private val COMPACT_OFFSET = Regex("^[+-]\\d{4}$")
    private val ABSOLUTE = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH)

    enum class DayBucket { TODAY, YESTERDAY, EARLIER }

    /**
     * Parses "2024-05-01T10:00:00Z", "...+00:00", "...+00", "2024-05-01 10:00:00.123456+00:00",
     * values without offset (treated as UTC) and plain dates. Returns null when unparseable.
     */
    fun parse(value: String?): Instant? {
        val raw = value?.trim()
        if (raw.isNullOrEmpty()) return null
        var s = raw.replaceFirst(' ', 'T')
        val tIndex = s.indexOf('T')
        if (tIndex < 0) {
            return try {
                LocalDate.parse(s).atStartOfDay(ZoneOffset.UTC).toInstant()
            } catch (e: DateTimeParseException) {
                null
            }
        }
        val timePart = s.substring(tIndex + 1)
        val offsetIndex = timePart.indexOfFirst { it == 'Z' || it == '+' || it == '-' }
        if (offsetIndex < 0) {
            s += "Z"
        } else {
            val offset = timePart.substring(offsetIndex)
            val head = s.substring(0, tIndex + 1 + offsetIndex)
            s = when {
                offset == "Z" || offset.matches(Regex("^[+-]\\d{2}:\\d{2}$")) -> s
                SHORT_OFFSET.matches(offset) -> "$head$offset:00"
                COMPACT_OFFSET.matches(offset) -> head + offset.substring(0, 3) + ":" + offset.substring(3)
                else -> return null
            }
        }
        return try {
            OffsetDateTime.parse(s).toInstant()
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /** Epoch millis of [value], or 0 when unparseable (useful for sorting). */
    fun toEpochMillis(value: String?): Long = parse(value)?.toEpochMilli() ?: 0L

    /** Current instant in the ISO form the backend expects ("...Z"). */
    fun nowIso(): String = Instant.now().toString()

    /** True when [value] parses and lies after [nowMillis]. */
    fun isInFuture(value: String?, nowMillis: Long): Boolean {
        val instant = parse(value) ?: return false
        return instant.toEpochMilli() > nowMillis
    }

    /** Arabic relative time such as "الآن", "منذ 5 دقائق", "أمس", or an absolute date (yyyy/MM/dd) after 30 days. */
    fun relativeArabic(value: String?, now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): String {
        val instant = parse(value) ?: return ""
        val seconds = now.epochSecond - instant.epochSecond
        if (seconds < 60L) return "الآن"
        val minutes = seconds / 60L
        if (minutes < 60L) return ago(minutes, "دقيقة", "دقيقتين", "دقائق", "دقيقة")
        val hours = minutes / 60L
        if (hours < 24L) return ago(hours, "ساعة", "ساعتين", "ساعات", "ساعة")
        val days = hours / 24L
        if (days == 1L) return "أمس"
        if (days < 30L) return ago(days, "يوم", "يومين", "أيام", "يوماً")
        return ABSOLUTE.format(instant.atZone(zone))
    }

    /** Groups an instant into today / yesterday / earlier for the given zone (notification sections). */
    fun dayBucket(value: String?, now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): DayBucket {
        val instant = parse(value) ?: return DayBucket.EARLIER
        val date = instant.atZone(zone).toLocalDate()
        val today = now.atZone(zone).toLocalDate()
        return when {
            !date.isBefore(today) -> DayBucket.TODAY
            date == today.minusDays(1) -> DayBucket.YESTERDAY
            else -> DayBucket.EARLIER
        }
    }

    private fun ago(n: Long, one: String, two: String, few: String, many: String): String = when {
        n == 1L -> "منذ $one"
        n == 2L -> "منذ $two"
        n in 3L..10L -> "منذ $n $few"
        else -> "منذ $n $many"
    }
}
