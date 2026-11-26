package com.nexus.app.domain.repository

import com.nexus.app.domain.model.PublicUser
import com.nexus.app.domain.model.SearchBundle

/**
 * Global search and user search. Queries shorter than 2 characters return empty results.
 * All suspend functions throw `ApiException` (no Result wrapper).
 */
interface SearchRepository {
    /** GET /rest/v1/posts, comments, courses, course_links with escaped ilike filters (parallel) + rpc/search_public_profiles. */
    suspend fun searchGlobal(query: String, limit: Int = 40): SearchBundle

    /** POST /rest/v1/rpc/search_public_profiles {_q}; excludes me, sub-admin and admin-looking accounts. */
    suspend fun searchPublicUsers(query: String): List<PublicUser>
}
