package com.nexus.app.domain.repository

import com.nexus.app.domain.model.NotificationFilter
import com.nexus.app.domain.model.NotificationItem
import com.nexus.app.domain.model.Page
import kotlinx.coroutines.flow.Flow

/**
 * Server notifications plus client-synthesized activity (comments, reactions, messages, warnings) with local read/hidden state.
 * All suspend functions throw `ApiException` (no Result wrapper).
 */
interface NotificationRepository {
    /** Number of unread notifications; emits again after every load, read, delete or [refreshUnreadCount]. */
    fun observeUnreadCount(): Flow<Int>

    /** Re-queries unread state (GET /rest/v1/notifications?read=eq.false + local activity) and pushes it to [observeUnreadCount]. */
    suspend fun refreshUnreadCount()

    /** GET /rest/v1/notifications?recipient_id=eq.<me>&created_at=lt.<cursor>&limit=<limit> merged with local activity, newest first. */
    suspend fun loadNotifications(filter: NotificationFilter, cursor: String?, limit: Int = 30): Page<NotificationItem>

    /** PATCH /rest/v1/notifications?id=eq.<id>&recipient_id=eq.<me> {read:true} (local items: local read set). */
    suspend fun markNotificationRead(notification: NotificationItem)

    /** PATCH /rest/v1/notifications?recipient_id=eq.<me>&read=eq.false {read:true} and marks all local items read. */
    suspend fun markAllNotificationsRead()

    /** DELETE /rest/v1/notifications?id=eq.<id>&recipient_id=eq.<me> (local items: local hidden set). */
    suspend fun deleteNotification(notification: NotificationItem)

    /** DELETE /rest/v1/notifications?recipient_id=eq.<me> and hides all local items. */
    suspend fun clearAllNotifications()
}
