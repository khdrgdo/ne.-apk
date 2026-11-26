package com.nexus.app.domain.model

data class SearchComment(
    val id: String,
    val postId: String,
    val authorName: String,
    val content: String,
    val createdAt: String,
    val anonymous: Boolean
)

data class SearchMaterial(
    val id: String,
    val courseId: String,
    val courseName: String,
    val title: String,
    val url: String,
    val linkType: String?,
    val createdAt: String
)

data class SearchBundle(
    val posts: List<FeedPost>,
    val courses: List<Course>,
    val users: List<PublicUser>,
    val comments: List<SearchComment> = emptyList(),
    val materials: List<SearchMaterial> = emptyList()
) {
    val isEmpty: Boolean
        get() = posts.isEmpty() && courses.isEmpty() && users.isEmpty() && comments.isEmpty() && materials.isEmpty()

    companion object {
        val EMPTY = SearchBundle(emptyList(), emptyList(), emptyList())
    }
}
