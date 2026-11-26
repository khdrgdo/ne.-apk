package com.nexus.app.domain.repository

import com.nexus.app.domain.model.Course
import com.nexus.app.domain.model.CourseDetail
import com.nexus.app.domain.model.CourseMaterial
import com.nexus.app.domain.model.HomeData
import com.nexus.app.domain.model.ScheduleEntry

/**
 * Courses, materials, announcements and course Q&A. All suspend functions throw `ApiException` (no Result wrapper).
 */
interface CourseRepository {
    /** GET /rest/v1/courses (filtered by [major]) + course_links counts and the 8 newest materials. */
    suspend fun loadHome(major: String?): HomeData

    /** GET /rest/v1/courses?id=eq.<id> + course_links, course_updates, posts with prefix [course:<id>] and their comments. */
    suspend fun loadCourseDetail(courseId: String): CourseDetail

    /** POST /rest/v1/course_links {course_id,title,url,created_by}; url must be https. */
    suspend fun addCourseLink(courseId: String, title: String, url: String)

    /** DELETE /storage/v1/object/course-files/<path> (files only) then DELETE /rest/v1/course_links?id=eq.<id>. */
    suspend fun deleteCourseMaterial(material: CourseMaterial)

    /** PATCH /rest/v1/course_links?id=eq.<id> {is_important: important}; desired state, not a toggle. */
    suspend fun setCourseMaterialImportant(materialId: String, important: Boolean)

    /** POST /rest/v1/course_updates {course_id,author_id,content}. */
    suspend fun addCourseUpdate(courseId: String, content: String)

    /** PATCH /rest/v1/course_updates?id=eq.<id> {content}. */
    suspend fun editCourseUpdate(updateId: String, content: String)

    /** DELETE /rest/v1/course_updates?id=eq.<id>. */
    suspend fun deleteCourseUpdate(updateId: String)

    /** PATCH /rest/v1/courses?id=eq.<id> {schedule}. */
    suspend fun updateCourseSchedule(courseId: String, entries: List<ScheduleEntry>)

    /** POST /rest/v1/rpc/create_post_as with content prefixed by [course:<id>] and post_type question. */
    suspend fun postQuestion(courseId: String, content: String, anonymous: Boolean)

    /** POST /rest/v1/rpc/create_comment_as (reply to a course question, not anonymous). */
    suspend fun addCourseComment(postId: String, content: String)
}
