package com.nexus.app.domain.repository

import com.nexus.app.domain.model.AdminActivity
import com.nexus.app.domain.model.AdminCourse
import com.nexus.app.domain.model.AdminReport
import com.nexus.app.domain.model.AdminSubAdmin
import com.nexus.app.domain.model.AdminUser
import com.nexus.app.domain.model.AdminWord
import com.nexus.app.domain.model.ReportStatus
import com.nexus.app.domain.model.SubAdminPermission

/**
 * Moderation and administration. All suspend functions throw `ApiException` (no Result wrapper).
 */
interface AdminRepository {
    /** GET /rest/v1/profiles?order=points.desc&limit=500 + user_roles. */
    suspend fun loadAdminUsers(): List<AdminUser>

    /** POST /rest/v1/rpc/get_courses_with_counts, falling back to GET /rest/v1/courses. */
    suspend fun loadAdminCourses(): List<AdminCourse>

    /** GET /rest/v1/banned_words?order=word.asc. */
    suspend fun loadBannedWords(): List<AdminWord>

    /** GET /rest/v1/admin_actions?order=created_at.desc&limit=200. */
    suspend fun loadAdminActions(): List<AdminActivity>

    /** GET /rest/v1/user_roles?role=eq.sub_admin + profiles + subadmin_permissions. */
    suspend fun loadAdminSubAdmins(): List<AdminSubAdmin>

    /** GET /rest/v1/post_reports and message_reports merged, newest first. */
    suspend fun loadAdminReports(): List<AdminReport>

    /** POST /rest/v1/banned_words {word}; at least 2 characters. */
    suspend fun addBannedWord(word: String)

    /** DELETE /rest/v1/banned_words?id=eq.<id>. */
    suspend fun deleteBannedWord(id: String)

    /** POST /rest/v1/rpc/admin_warn {_user,_reason}. */
    suspend fun adminWarn(userId: String, reason: String)

    /** POST /rest/v1/rpc/admin_suspend {_user,_days,_reason}. */
    suspend fun adminSuspend(userId: String, days: Int, reason: String)

    /** POST /rest/v1/rpc/admin_ban {_user,_reason}. */
    suspend fun adminBan(userId: String, reason: String)

    /** POST /rest/v1/rpc/admin_unban {_user}. */
    suspend fun adminUnban(userId: String)

    /** POST /rest/v1/rpc/admin_adjust_points {_user,_delta}. */
    suspend fun adminAdjustPoints(userId: String, delta: Int)

    /** POST /rest/v1/rpc/admin_set_verified {_user,_verified}. */
    suspend fun adminSetVerified(userId: String, verified: Boolean)

    /** POST /rest/v1/rpc/admin_set_year {_user,_year}. */
    suspend fun adminSetYear(userId: String, year: Int)

    /** POST /rest/v1/rpc/admin_delete_user {_user}; also used to delete sub-admins (the old deleteSubAdmin duplicate is merged). */
    suspend fun adminDeleteUser(userId: String)

    /** PATCH /rest/v1/post_reports or message_reports (by report type) ?id=eq.<id> {status}. */
    suspend fun updateAdminReportStatus(report: AdminReport, status: ReportStatus)

    /** POST /rest/v1/subadmin_permissions?on_conflict=user_id (merge-duplicates); permissions not in the set are stored as false. */
    suspend fun updateSubAdminPermissions(userId: String, permissions: Set<SubAdminPermission>)

    /** POST /auth/v1/signup + rpc/admin_set_teacher_role + PATCH courses.teacher_id; rolls back with admin_delete_user on failure. */
    suspend fun createTeacherAccount(name: String, email: String, password: String, courseIds: List<String>)

    /** POST /auth/v1/signup + profile poll + PATCH profiles.verified + subadmin_permissions + rpc/admin_set_user_role; rolls back on failure. */
    suspend fun createSubAdminAccount(loginId: String, fullName: String, password: String, permissions: Set<SubAdminPermission>)

    /** POST /rest/v1/notifications in batches of 100 (type announcement) to profiles filtered by [year] and [major]; returns the recipient count. */
    suspend fun broadcastAnnouncement(title: String, body: String, year: Int? = null, major: String? = null): Int
}
