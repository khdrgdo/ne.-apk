package com.nexus.app.domain.repository

import com.nexus.app.domain.model.BlockStatus
import com.nexus.app.domain.model.ChatMessage
import com.nexus.app.domain.model.ConversationPreview
import com.nexus.app.domain.model.Page
import com.nexus.app.domain.model.PublicUser

/**
 * Direct and group messaging (no Realtime; the chat screen polls [loadNewerMessages]).
 * All suspend functions throw `ApiException` (no Result wrapper).
 */
interface MessagingRepository {
    /** GET /rest/v1/conversation_members + conversations + last message and other member per conversation (batched). */
    suspend fun loadConversations(): List<ConversationPreview>

    /**
     * GET /rest/v1/messages?order=created_at.desc&created_at=lt.<cursor>&limit=<limit>: the NEWEST page first (fix of the oldest-300 bug).
     * Items are returned oldest to newest; nextCursor is the createdAt of the oldest item, to load older messages.
     */
    suspend fun loadMessages(conversationId: String, cursor: String?, limit: Int = 50): Page<ChatMessage>

    /** GET /rest/v1/messages?created_at=gt.<afterCreatedAt>&order=created_at.asc, used by the 4 s poll; items oldest to newest. */
    suspend fun loadNewerMessages(conversationId: String, afterCreatedAt: String): List<ChatMessage>

    /** POST /rest/v1/rpc/create_dm {_other}, falling back to conversations (Prefer: return=representation, array parsing) + conversation_members; returns the id. */
    suspend fun createDirectConversation(otherUserId: String): String

    /** POST /rest/v1/rpc/create_group {_name,_members}; returns the id. */
    suspend fun createGroupConversation(name: String, memberIds: List<String>): String

    /** Checks blocked_users, POST /rest/v1/messages {conversation_id,sender_id,content}, PATCH conversations.updated_at; returns the stored message. */
    suspend fun sendMessage(conversationId: String, content: String): ChatMessage

    /** POST /rest/v1/blocked_users {blocker_id,blocked_id} (idempotent). */
    suspend fun blockUser(userIdToBlock: String)

    /** DELETE /rest/v1/blocked_users?blocker_id=eq.<me>&blocked_id=eq.<id>. */
    suspend fun unblockUser(userId: String)

    /** POST /rest/v1/message_reports {reporter_id,reported_user_id,conversation_id,reason,note,message_id?}. */
    suspend fun reportMessageUser(conversationId: String, reportedUserId: String, reason: String, note: String, messageId: String? = null)

    /** GET /rest/v1/conversation_members?order=joined_at.asc + rpc/get_public_profiles. */
    suspend fun loadConversationMembers(conversationId: String): List<PublicUser>

    /** GET /rest/v1/blocked_users for both directions among the other members of the conversation. */
    suspend fun getBlockStatus(conversationId: String): BlockStatus
}
