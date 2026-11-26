package com.nexus.app.domain.model

/** Tokens of the signed-in user. [expiresAt] is epoch seconds. [userId] may be blank right after a deep-link handoff. */
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val userId: String
)

/** Roles stored in `user_roles.role`. */
enum class UserRole(val value: String) {
    STUDENT("student"),
    TEACHER("teacher"),
    ADMIN("admin"),
    SUB_ADMIN("sub_admin");

    companion object {
        fun fromValue(value: String?): UserRole? = entries.firstOrNull { it.value == value }
    }
}

/** Profile page decoration, stored in `profiles.theme`. */
enum class ProfileTheme(val value: String) {
    STARS("stars"),
    CLOUDS("clouds"),
    MOON("moon"),
    SUN("sun"),
    GALAXY("galaxy");

    companion object {
        fun fromValue(value: String?): ProfileTheme = entries.firstOrNull { it.value == value } ?: STARS
    }
}

/** Sub-admin permission flags; [column] is the `subadmin_permissions.can_*` column. */
enum class SubAdminPermission(val key: String, val column: String, val label: String) {
    WARN("warn", "can_warn", "التحذير"),
    SUSPEND("suspend", "can_suspend", "الإيقاف"),
    COURSES("courses", "can_courses", "المقررات"),
    REPORTS("reports", "can_reports", "البلاغات"),
    WORDS("words", "can_words", "الكلمات"),
    TEACHERS("teachers", "can_teachers", "الأساتذة");

    companion object {
        fun fromKey(key: String?): SubAdminPermission? = entries.firstOrNull { it.key == key }
    }
}

data class StudentProfile(
    val id: String,
    val universityNumber: String,
    val fullName: String,
    val major: String?,
    val year: Int?,
    val avatarUrl: String?,
    val bio: String?,
    val mustChangePassword: Boolean,
    val points: Int,
    val banned: Boolean,
    val suspendedUntil: String?,
    val theme: ProfileTheme = ProfileTheme.STARS,
    val roles: Set<UserRole> = setOf(UserRole.STUDENT),
    val subAdminPermissions: Set<SubAdminPermission> = emptySet(),
    val verified: Boolean = false
) {
    val isAdmin: Boolean get() = UserRole.ADMIN in roles
    val isSubAdmin: Boolean get() = UserRole.SUB_ADMIN in roles
    val isTeacher: Boolean get() = UserRole.TEACHER in roles
    /** True for admins and sub-admins (may see anonymous authors). */
    val isStaff: Boolean get() = isAdmin || isSubAdmin
}

/** Row of `get_public_profiles`. */
data class PublicProfile(
    val id: String,
    val fullName: String,
    val avatarUrl: String?,
    val verified: Boolean,
    val universityNumber: String = "",
    val points: Int = 0,
    val major: String? = null
)

data class PublicUser(
    val id: String,
    val fullName: String,
    val universityNumber: String,
    val avatarUrl: String?,
    val major: String?
)

/**
 * Public profile page. Old bug fixed: `profile.verified` now carries the real `verified` value
 * (it used to be put into `mustChangePassword`).
 */
data class PublicProfileDetail(
    val profile: StudentProfile,
    val posts: List<FeedPost>,
    val postsCount: Int,
    val commentsCount: Int,
    val acceptedAnswers: Int
)
