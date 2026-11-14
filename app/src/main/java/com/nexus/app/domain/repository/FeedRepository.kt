package com.nexus.app.domain.repository

import com.nexus.app.domain.model.FeedPost
import com.nexus.app.domain.model.Page
import com.nexus.app.domain.model.PostDetail
import com.nexus.app.domain.model.PostType
import com.nexus.app.domain.model.Reaction

/**
 * Feed, posts, comments, reactions, saved posts. All suspend functions throw `ApiException` (no Result wrapper).
 * Pagination: pass the previous page's nextCursor (created_at of its last row) as [cursor]; null loads the first page.
 * Every returned post/comment is anonymity-sanitized with `forViewer`.
 */
interface FeedRepository {
    /** GET /rest/v1/posts?order=created_at.desc&created_at=lt.<cursor>&limit=<limit> with comment counts, reactions, saved flags and signed images. */
    suspend fun loadFeed(cursor: String?, limit: Int = 20): Page<FeedPost>

    /** GET /rest/v1/saved_posts then GET /rest/v1/posts?id=in.(...) ordered by saved date, paginated. */
    suspend fun loadSavedPosts(cursor: String?, limit: Int = 20): Page<FeedPost>

    /** GET /rest/v1/posts?id=eq.<id> + comments + reactions + saved flag (reactions and saved are now filled in). */
    suspend fun loadPostDetail(postId: String): PostDetail

    /** POST /rest/v1/rpc/create_post_as {_content,_images,_post_type,_anonymous}; content must not be blank. */
    suspend fun createFeedPost(content: String, anonymous: Boolean, postType: PostType = PostType.GENERAL, images: List<String> = emptyList())

    /** POST /rest/v1/rpc/create_comment_as {_post_id,_content,_anonymous,_parent_id?}. */
    suspend fun createPostComment(postId: String, content: String, anonymous: Boolean, parentId: String? = null)

    /** null: DELETE /rest/v1/post_reactions; otherwise POST /rest/v1/post_reactions?on_conflict=post_id,user_id (merge-duplicates). */
    suspend fun setPostReaction(postId: String, reaction: Reaction?)

    /** [saved] is the DESIRED state: true POSTs /rest/v1/saved_posts, false DELETEs it. */
    suspend fun setPostSaved(postId: String, saved: Boolean)

    /** POST /rest/v1/post_reports {post_id,reporter_id,reason}. */
    suspend fun reportPost(postId: String, reason: String)

    /** DELETE /rest/v1/posts?id=eq.<id>. */
    suspend fun deletePost(postId: String)

    /** PATCH /rest/v1/posts?id=eq.<id> {accepted_answer_id}; null un-accepts. */
    suspend fun acceptPostAnswer(postId: String, commentId: String?)

    /** DELETE /rest/v1/comments?id=eq.<id>. */
    suspend fun deletePostComment(commentId: String)
}
