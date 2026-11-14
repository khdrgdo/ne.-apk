package com.nexus.app.domain.model

data class ScheduleEntry(
    val day: String,
    val start: String,
    val end: String,
    val room: String
)

data class Course(
    val id: String,
    val name: String,
    val description: String?,
    val major: String,
    val year: Int,
    val semester: Int,
    val teacherId: String?,
    val teacherName: String?,
    val schedule: List<ScheduleEntry>,
    val fileCount: Int = 0,
    val linkCount: Int = 0,
    val lastUpdated: String? = null
)

data class CourseMaterial(
    val id: String,
    val courseId: String,
    val title: String,
    val note: String?,
    /** External URL, or the storage path inside bucket `course-files` when [isFile]. */
    val url: String,
    /** `file` for uploads, null for plain links. */
    val linkType: String?,
    val createdAt: String,
    val important: Boolean,
    val courseName: String? = null
) {
    val isFile: Boolean get() = linkType == "file"
    val extension: String get() = url.substringAfterLast('.', "").substringBefore('?').lowercase()
}

data class CourseUpdate(
    val id: String,
    val courseId: String,
    val authorName: String,
    val content: String,
    val createdAt: String
)

data class CourseComment(
    val id: String,
    val authorId: String?,
    val authorName: String,
    val avatarUrl: String?,
    val content: String,
    val createdAt: String,
    val isTeacher: Boolean
)

data class CourseQuestion(
    val id: String,
    val authorId: String?,
    val authorName: String,
    val avatarUrl: String?,
    val verified: Boolean,
    val content: String,
    val createdAt: String,
    val replies: List<CourseComment>
)

data class CourseDetail(
    val course: Course,
    val files: List<CourseMaterial>,
    val links: List<CourseMaterial>,
    val questions: List<CourseQuestion>,
    val updates: List<CourseUpdate>
)

/** Result of the home screen load. */
data class HomeData(
    val courses: List<Course>,
    val recentMaterials: List<CourseMaterial>
)
