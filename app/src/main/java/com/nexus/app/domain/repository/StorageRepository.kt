package com.nexus.app.domain.repository

import com.nexus.app.domain.model.CourseMaterial
import com.nexus.app.domain.model.UploadSource

/**
 * Supabase Storage (buckets post-images, course-files, avatars). Uploads refresh the token first.
 * All suspend functions throw `ApiException` (no Result wrapper).
 */
interface StorageRepository {
    /** POST /storage/v1/object/post-images/<uid>/<file> (image, max 5 MB); returns the storage path to pass to createFeedPost. */
    suspend fun uploadPostImage(source: UploadSource): String

    /** POST /storage/v1/object/course-files/<courseId>/<file> (25 MB, video 100 MB, streamed) then POST /rest/v1/course_links {link_type:file}; removes the object if the insert fails. */
    suspend fun uploadCourseFile(courseId: String, source: UploadSource): CourseMaterial

    /** Links return their url; files POST /storage/v1/object/sign/course-files/<path> {expiresIn:600}. */
    suspend fun resolveMaterialUrl(material: CourseMaterial): String

    /** POST /storage/v1/object/sign/<bucket>/<path> {expiresIn:3600}, cached (SignedUrlCache, 50 min). */
    suspend fun signedUrl(bucket: String, path: String): String

    /** Turns a stored avatar value (path, public or signed URL) into a signed URL, or null when blank or signing fails. */
    suspend fun avatarDisplayUrl(value: String?): String?

    /** Signs many paths of one bucket in parallel (path to URL; failed ones are omitted). */
    suspend fun signedUrls(bucket: String, paths: List<String>): Map<String, String>
}
