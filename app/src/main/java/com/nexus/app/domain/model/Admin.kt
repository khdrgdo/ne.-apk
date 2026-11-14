package com.nexus.app.domain.model

/** `post_reports.status` / `message_reports.status`; unknown values map to [PENDING]. */
enum class ReportStatus(val value: String) {
    PENDING("pending"),
    REVIEWED("reviewed"),
    RESOLVED("resolved"),
    DISMISSED("dismissed");

    companion object {
        fun fromValue(value: String?): ReportStatus = entries.firstOrNull { it.value == value } ?: PENDING
    }
}

enum class ReportType(val value: String) {
    POST("post"),
    MESSAGE("message");

    companion object {
        fun fromValue(value: String?): ReportType = entries.firstOrNull { it.value == value } ?: POST
    }
}

data class AdminUser(
    val id: String,
    val fullName: String,
    val universityNumber: String,
    val major: String?,
    val year: Int?,
    val points: Int,
    val banned: Boolean,
    val suspendedUntil: String?,
    val roles: Set<UserRole>,
    val avatarUrl: String? = null,
    val verified: Boolean = false
)

data class AdminCourse(
    val id: String,
    val name: String,
    val major: String,
    val year: Int,
    val semester: Int,
    val teacherName: String?,
    val filesCount: Int = 0,
    val linksCount: Int = 0
)

data class AdminWord(val id: String, val word: String)

data class AdminActivity(
    val id: String,
    val action: String,
    val details: String,
    val adminName: String,
    val targetName: String,
    val createdAt: String
)

data class AdminSubAdmin(
    val id: String,
    val fullName: String,
    val universityNumber: String,
    val permissions: Set<SubAdminPermission>
)

/** For [ReportType.MESSAGE] reports, [postId] holds the conversation id. */
data class AdminReport(
    val id: String,
    val postId: String,
    val content: String,
    val reporterName: String,
    val reason: String,
    val status: ReportStatus,
    val createdAt: String,
    val reportType: ReportType = ReportType.POST,
    val reportedUserId: String? = null,
    val messageId: String? = null
)
