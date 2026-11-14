package com.nexus.app.domain.model

enum class LeaderboardTimeframe(val value: String, val days: Int?) {
    ALL("all", null),
    WEEK("week", 7),
    MONTH("month", 30);

    companion object {
        fun fromValue(value: String?): LeaderboardTimeframe = entries.firstOrNull { it.value == value } ?: ALL
    }
}

data class LeaderboardCourse(val id: String, val name: String, val major: String?, val year: Int?)

data class LeaderboardUser(
    val id: String,
    val fullName: String,
    val avatarUrl: String?,
    val points: Int,
    val major: String?,
    val year: Int?,
    val verified: Boolean,
    val score: Int,
    val contributions: Int
)
