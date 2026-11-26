package com.nexus.app.domain.repository

import com.nexus.app.domain.model.StudentProfile
import com.nexus.app.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Authentication and session lifecycle (Supabase GoTrue).
 * All suspend functions throw `com.nexus.app.core.network.ApiException` (no Result wrapper); callers use `safeCall`.
 */
interface AuthRepository {
    /** Current session (null = signed out); emits on sign in, refresh and sign out. Backed by SessionManager. */
    fun observeSession(): Flow<UserSession?>

    /** POST /auth/v1/token?grant_type=password, then profile load and access gate (banned/suspended/no role). Clears the local session on failure. */
    suspend fun signIn(identifier: String, password: String): StudentProfile

    /** Reads the stored session, refreshes it, GET /auth/v1/user, loads the profile and applies the access gate; null when not signed in. Never wipes the session on transient errors. */
    suspend fun restoreSession(): StudentProfile?

    /** Accepts `com.nexus.app://login-callback#...` (validating state/nonce) and stores the session; false when the link is not a valid callback. */
    suspend fun acceptDeepLink(url: String): Boolean

    /** POST /auth/v1/recover with the normalized identifier. */
    suspend fun recoverPassword(identifier: String)

    /** PUT /auth/v1/user {password}; also clears profiles.must_change_password and refreshes the stored session. */
    suspend fun updatePassword(password: String)

    /** POST /auth/v1/logout?scope=local; local state is always cleared, even when the call fails. */
    suspend fun signOut()
}
