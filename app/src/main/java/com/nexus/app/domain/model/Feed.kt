package com.nexus.app.domain.model

/** Reaction types of `post_reactions.reaction`. */
enum class Reaction(val value: String) {
    LIKE("like"),
    LOVE("love"),
    HAHA("haha"),
    WOW("wow"),
    SAD("sad");

    companion object {
        fun fromValue(value: String?): Reaction? = entries.firstOrNull { it.value == value }
    }
}

/** `posts.post_type`. Unknown backend values map to [GENERAL]. */
enum class PostType(val value: String) {
    GENERAL("general"),
    QUESTION("question");

    companion object {
        fun fromValue(value: String?): PostType = entries.firstOrNull { it.value == value } ?: GENERAL
    }
}

/** Real author of an anonymous post/comment; only ever filled for admins and sub-admins (RPC `get_anonymous_authors`). */
data class AnonymousAuthor(
    val authorId: String,
    val fullName: String,
    val avatarUrl: String?,
    val universityNumber: String
)

data class FeedReaction(val userId: String, val type: Reaction)

/**
 * A post as shown to the CURRENT viewer.
 *
 * Anonymity contract: when [anonymous] is true and the viewer is not staff, [authorId] is null (unless [isMyAnonymous]),
 * [avatarUrl] is null, [authorVerified] false, [authorPoints] 0, [authorMajor] null and [revealedAuthor] null.
 * Repositories MUST build posts through [forViewer] so the leak cannot reappear.
 */
data class FeedPost(
    val id: String,
    val authorId: String?,
    val authorName: String,
    val avatarUrl: String?,
    val anonymous: Boolean,
    val content: String,
    val postType: PostType,
    val createdAt: String,
    val commentsCount: Int,
    val images: List<String> = emptyList(),
    val reactions: List<FeedReaction> = emptyList(),
    val myReaction: Reaction? = null,
    val saved: Boolean = false,
    val acceptedAnswerId: String? = null,
    val authorVerified: Boolean = false,
    val authorPoints: Int = 0,
    val authorMajor: String? = null,
    val isMyAnonymous: Boolean = false,
    val revealedAuthor: AnonymousAuthor? = null
) {
    val isQuestion: Boolean get() = postType == PostType.QUESTION

    /** Returns a copy with every identity-revealing field removed when the viewer may not see it. */
    fun forViewer(viewerIsStaff: Boolean): FeedPost {
        if (!anonymous || viewerIsStaff) return this
        return copy(
            authorId = if (isMyAnonymous) authorId else null,
            avatarUrl = null,
            authorVerified = false,
            authorPoints = 0,
            authorMajor = null,
            revealedAuthor = null
        )
    }
}

/** Comment on a post; same anonymity contract as [FeedPost], see [forViewer]. */
data class FeedComment(
    val id: String,
    val postId: String,
    val authorId: String?,
    val authorName: String,
    val avatarUrl: String?,
    val content: String,
    val createdAt: String,
    val anonymous: Boolean,
    val parentId: String?,
    val authorVerified: Boolean = false,
    val isMyAnonymous: Boolean = false,
    val revealedAuthor: AnonymousAuthor? = null
) {
    fun forViewer(viewerIsStaff: Boolean): FeedComment {
        if (!anonymous || viewerIsStaff) return this
        return copy(
            authorId = if (isMyAnonymous) authorId else null,
            avatarUrl = null,
            authorVerified = false,
            revealedAuthor = null
        )
    }
}

/** Post with all comments and, unlike the old app, real reactions, `myReaction` and `saved`. */
data class PostDetail(val post: FeedPost, val comments: List<FeedComment>)
