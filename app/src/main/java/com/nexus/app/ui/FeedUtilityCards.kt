package com.nexus.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexus.app.data.AdminReport
import com.nexus.app.data.NotificationItem
import com.nexus.app.data.NexusUiState
import com.nexus.app.data.PinnedCardConfig
import com.nexus.app.data.StudentProfile

@Composable
fun PinnedEventCard(
    config: PinnedCardConfig?,
    profile: StudentProfile?,
    onVote: (String) -> Unit,
    onParticipate: () -> Unit,
    onOpenAction: (String) -> Unit
) {
    if (config == null || !config.enabled) return
    if (config.targetYear != null && profile?.year != config.targetYear) return
    if (!config.targetMajor.isNullOrBlank() && profile?.major != config.targetMajor) return
    val isPoll = config.type.equals("poll", true) || config.type.equals("survey", true)
    val selected = profile?.id?.let(config.votes::get)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!config.imageUrl.isNullOrBlank()) {
                AsyncImage(config.imageUrl, config.title, Modifier.fillMaxWidth().height(148.dp), contentScale = ContentScale.Crop)
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(config.badgeText.ifBlank { "مثبّت" }, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.weight(1f))
                    config.endDate?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Text(config.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (config.description.isNotBlank()) Text(config.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isPoll) {
                    config.pollOptions.forEach { option ->
                        val count = config.votes.values.count { it == option.id }
                        OutlinedButton(onClick = { onVote(option.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Text(option.text, Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(count.toString(), style = MaterialTheme.typography.labelMedium)
                            if (selected == option.id) Icon(Icons.Outlined.Check, null, Modifier.size(16.dp))
                        }
                    }
                }
                if (config.type.equals("participation", true) || config.type.equals("event", true)) {
                    OutlinedButton(onClick = onParticipate, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("تأكيد المشاركة")
                    }
                }
                config.actionButtonUrl?.takeIf(String::isNotBlank)?.let { url ->
                    Button(onClick = { onOpenAction(url) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(config.actionButtonText?.ifBlank { "فتح التفاصيل" } ?: "فتح التفاصيل")
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPostsScreen(
    state: NexusUiState,
    onLoad: () -> Unit,
    onUnsave: (String) -> Unit,
    onHome: () -> Unit,
    onCourses: () -> Unit,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onHome) { Icon(Icons.Outlined.ArrowForward, "العودة للمجتمع") }
            Column(Modifier.weight(1f)) {
                Text("المنشورات المحفوظة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("محتوى احتفظت به للرجوع إليه", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        when {
            state.loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.feedPosts.isEmpty() -> Text("لا توجد منشورات محفوظة حالياً.", Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.feedPosts, key = { it.id }) { post ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NexusAvatar(post.avatarUrl, post.authorName, 38)
                                Column(Modifier.weight(1f).padding(start = 9.dp)) {
                                    Text(post.authorName, fontWeight = FontWeight.SemiBold)
                                    Text(post.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onUnsave(post.id) }) { Icon(Icons.Outlined.BookmarkRemove, "إزالة من المحفوظات") }
                            }
                            Text(post.content, maxLines = 8, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                            post.images.firstOrNull()?.let { AsyncImage(it, null, Modifier.fillMaxWidth().height(170.dp), contentScale = ContentScale.Crop) }
                            Text("${post.commentsCount} تعليق", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    state: NexusUiState,
    onLoad: () -> Unit,
    onRead: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMarkAll: () -> Unit,
    onClearAll: () -> Unit,
    onHome: () -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onHome) { Icon(Icons.Outlined.ArrowForward, "العودة للمجتمع") }
            Column(Modifier.weight(1f)) {
                Text("الإشعارات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("آخر التنبيهات والتحديثات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onMarkAll, enabled = state.notifications.any { !it.read }) { Icon(Icons.Outlined.DoneAll, "تحديد الكل كمقروء") }
            IconButton(onClick = onClearAll, enabled = state.notifications.isNotEmpty()) { Icon(Icons.Outlined.DeleteOutline, "مسح الكل") }
        }
        when {
            state.notificationsLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.notifications.isEmpty() -> Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.NotificationsNone, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("لا توجد إشعارات جديدة", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.notifications, key = { it.id }) { item ->
                    NotificationRow(item, onRead, onDelete)
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem, onRead: (String) -> Unit, onDelete: (String) -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable { if (!item.read) onRead(item.id) },
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = if (item.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = .38f))
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.NotificationsNone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 3.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.title, fontWeight = FontWeight.SemiBold)
                Text(item.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(item.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onDelete(item.id) }) { Icon(Icons.Outlined.DeleteOutline, "حذف الإشعار") }
        }
    }
}
