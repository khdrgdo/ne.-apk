package com.nexus.app.domain.model

data class ConversationPreview(
    val id: String,
    val title: String,
    val isGroup: Boolean,
    val otherUserId: String?,
    val lastMessage: String,
    val updatedAt: String,
    val avatarUrl: String? = null,
    val memberCount: Int = 0
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val createdAt: String
)

/** Block state of a conversation: [blockedByMe] = I blocked someone in it, [blockedMe] = someone blocked me. */
data class BlockStatus(val blockedByMe: Boolean, val blockedMe: Boolean) {
    val any: Boolean get() = blockedByMe || blockedMe
}
