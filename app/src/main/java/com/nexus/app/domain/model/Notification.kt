package com.nexus.app.domain.model

/** `notifications.type`; unknown values map to [OTHER]. */
enum class NotificationType(val value: String) {
    POST_COMMENT("post_comment"),
    POST_LIKE("post_like"),
    COMMENT_REPLY("comment_reply"),
    ANNOUNCEMENT("announcement"),
    MESSAGE("message"),
    OTHER("other");

    companion object {
        fun fromValue(value: String?): NotificationType = entries.firstOrNull { it.value == value } ?: OTHER
    }
}

enum class NotificationPriority(val value: String) {
    NORMAL("normal"),
    IMPORTANT("important");

    companion object {
        fun fromValue(value: String?): NotificationPriority = entries.firstOrNull { it.value == value } ?: NORMAL
    }
}

enum class NotificationFilter { ALL, UNREAD, IMPORTANT }

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val link: String?,
    val read: Boolean,
    val createdAt: String,
    val actorName: String? = null,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    /** True for items synthesized client-side (comments, reactions, messages, warnings); their state is stored locally. */
    val localActivity: Boolean = false,
    val actorId: String? = null
)
