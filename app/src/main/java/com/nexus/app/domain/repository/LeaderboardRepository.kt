package com.nexus.app.domain.repository

import com.nexus.app.domain.model.LeaderboardCourse
import com.nexus.app.domain.model.LeaderboardTimeframe
import com.nexus.app.domain.model.LeaderboardUser

/**
 * Rankings. All suspend functions throw `ApiException` (no Result wrapper).
 */
interface LeaderboardRepository {
    /** GET /rest/v1/courses?select=id,name,major,year&order=name.asc&limit=300. */
    suspend fun loadLeaderboardCourses(): List<LeaderboardCourse>

    /** POST /rest/v1/rpc/get_leaderboard_profiles {_major,_year} (+ posts/comments counts for week/month); sorted by score descending. */
    suspend fun loadLeaderboard(
        timeframe: LeaderboardTimeframe = LeaderboardTimeframe.ALL,
        major: String? = null,
        year: Int? = null,
        courseId: String? = null
    ): List<LeaderboardUser>
}
