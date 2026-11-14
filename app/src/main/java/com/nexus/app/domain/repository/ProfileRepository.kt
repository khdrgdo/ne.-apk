package com.nexus.app.domain.repository

import com.nexus.app.domain.model.ProfileTheme
import com.nexus.app.domain.model.PublicProfile
import com.nexus.app.domain.model.PublicProfileDetail
import com.nexus.app.domain.model.StudentProfile
import com.nexus.app.domain.model.UploadSource

/**
 * Profiles. All suspend functions throw `ApiException` (no Result wrapper).
 */
interface ProfileRepository {
    /** GET /rest/v1/profiles + user_roles (+ subadmin_permissions for sub-admins) for the signed-in user; avatar is signed. */
    suspend fun loadCurrentProfile(): StudentProfile

    /** POST /rest/v1/rpc/get_public_profiles for the given ids (empty set returns an empty map). */
    suspend fun loadPublicProfiles(ids: Set<String>): Map<String, PublicProfile>

    /** rpc/get_public_profiles + posts, comments and accepted-answer counts of another user (sub-admin profiles hidden from non-admins). */
    suspend fun loadPublicProfile(profileId: String): PublicProfileDetail

    /** PATCH /rest/v1/profiles?id=eq.<me> {theme}. */
    suspend fun updateProfileTheme(theme: ProfileTheme)

    /** PATCH /rest/v1/profiles?id=eq.<me> {bio}; blank becomes null. */
    suspend fun updateProfileBio(bio: String?)

    /** POST /storage/v1/object/avatars/<uid>/<file> then PATCH /rest/v1/profiles {avatar_url}; returns the new signed display URL. */
    suspend fun uploadProfileAvatar(source: UploadSource): String
}
