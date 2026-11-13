@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.nexus.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.app.data.*
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(state: NexusUiState, onRefresh: () -> Unit, onSearch: (String) -> Unit,
    onStartChat: (String) -> Unit, onCreateGroup: (String, List<String>) -> Unit, onOpenChat: (String) -> Unit, onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var groupMode by rememberSaveable { mutableStateOf(false) }
    var groupName by rememberSaveable { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(query) { onSearch(query) }
    Scaffold(topBar = { TopAppBar(title = { Text("المحادثات") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } }, actions = {
        IconButton(onClick = { groupMode = !groupMode; selectedIds = emptySet() }) { Icon(if (groupMode) Icons.Outlined.Close else Icons.Outlined.GroupAdd, "إنشاء مجموعة") }
        IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, "تحديث") }
    }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (groupMode) item {
                OutlinedTextField(groupName, { groupName = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("اسم المجموعة") })
                Button(onClick = { onCreateGroup(groupName, selectedIds.toList()); groupMode = false; groupName = ""; selectedIds = emptySet() },
                    enabled = groupName.isNotBlank() && selectedIds.size >= 1, modifier = Modifier.fillMaxWidth()) { Text("إنشاء مجموعة (${selectedIds.size} أعضاء)") }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(query, { query = it }, Modifier.weight(1f), singleLine = true,
                        placeholder = { Text("ابحث عن زميل بالاسم أو الرقم") }, shape = RoundedCornerShape(13.dp))
                    IconButton(onClick = { onSearch(query) }, enabled = query.trim().length >= 2) { Icon(Icons.Outlined.Search, "بحث") }
                }
            }
            if (state.messageSearchResults.isNotEmpty()) {
                item { Text("نتائج البحث", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                items(state.messageSearchResults, key = { "user-" + it.id }) { user ->
                    Card(onClick = { if (groupMode) selectedIds = if (user.id in selectedIds) selectedIds - user.id else selectedIds + user.id else onStartChat(user.id) }, shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            NexusAvatar(user.avatarUrl, user.fullName, 42)
                            Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(user.fullName, fontWeight = FontWeight.SemiBold); Text(user.universityNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            if (groupMode) Checkbox(user.id in selectedIds, { selectedIds = if (it) selectedIds + user.id else selectedIds - user.id })
                            else Icon(Icons.Outlined.Chat, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            item { Text("محادثاتك", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
            if (state.messagesLoading && state.conversations.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (!state.messagesLoading && state.conversations.isEmpty()) item { Text("لا توجد محادثات بعد. ابحث عن زميل لبدء محادثة.", Modifier.fillMaxWidth().padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(state.conversations, key = { it.id }) { c ->
                Card(onClick = { onOpenChat(c.id) }, shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!c.isGroup) NexusAvatar(c.avatarUrl, c.title, 44) else Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Groups, null) } }
                        Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) {
                            Text(c.title, fontWeight = FontWeight.SemiBold)
                            Text(c.lastMessage.ifBlank { if (c.isGroup) "مجموعة" else "ابدأ المحادثة" }, maxLines = 1, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Outlined.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(state: NexusUiState, onBack: () -> Unit, onRefresh: () -> Unit, onSend: (String) -> Unit,
    onBlock: (String) -> Unit, onUnblock: (String) -> Unit, onReport: (String, String, String?, String?, Boolean) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    var blockConfirm by rememberSaveable { mutableStateOf(false) }
    var reportOpen by rememberSaveable { mutableStateOf(false) }
    var reportTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var reportMessageId by rememberSaveable { mutableStateOf<String?>(null) }
    var reportNote by rememberSaveable { mutableStateOf("") }
    var reportReason by rememberSaveable { mutableStateOf("محتوى مسيء أو تحرّش") }
    var reportConfirmed by rememberSaveable { mutableStateOf(false) }
    var blockAfterReport by rememberSaveable { mutableStateOf(true) }
    var groupInfoOpen by rememberSaveable { mutableStateOf(false) }
    var lastSuccessCounter by remember { mutableStateOf(state.chatSendSuccessCounter) }
    val listState = rememberLazyListState()
    val active = state.conversations.firstOrNull { it.id == state.activeConversationId }
    val otherId = active?.otherUserId
    LaunchedEffect(state.chatSendSuccessCounter) {
        if (state.chatSendSuccessCounter > lastSuccessCounter) {
            draft = ""
            lastSuccessCounter = state.chatSendSuccessCounter
        }
    }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }
    Scaffold(topBar = { TopAppBar(title = { Column { Text(active?.title ?: "المحادثة"); Text(if (active?.isGroup == true) "مجموعة" else "محادثة خاصة", style = MaterialTheme.typography.labelSmall) } },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } },
        actions = {
            if (active?.isGroup == true) IconButton(onClick = { groupInfoOpen = true }) { Icon(Icons.Outlined.Info, "معلومات المجموعة") }
            else if (otherId != null) {
                IconButton(onClick = { blockConfirm = true }, enabled = !state.chatBlocked) { Icon(Icons.Outlined.Block, "حظر") }
                IconButton(onClick = { reportTarget = otherId; reportMessageId = null; reportNote = ""; reportConfirmed = false; blockAfterReport = true; reportOpen = true }) { Icon(Icons.Outlined.Flag, "إبلاغ") }
            }
            IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, "تحديث") }
        }) },
        bottomBar = {
            Surface(Modifier.fillMaxWidth().imePadding(), tonalElevation = 3.dp) {
                if (state.chatBlocked) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Block, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (state.chatBlockedByMe) "لقد حظرت هذا المستخدم؛ لا يمكن إرسال رسائل في هذه المحادثة." else "لا يمكنك مراسلة هذا المستخدم بسبب إعدادات الحظر.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        if (state.chatBlockedByMe && otherId != null) TextButton(onClick = { onUnblock(otherId) }) { Text("رفع الحظر") }
                    }
                } else {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("اكتب رسالة...") },
                            maxLines = 5,
                            shape = RoundedCornerShape(20.dp),
                            enabled = !state.chatSending,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { if (draft.isNotBlank() && !state.chatSending) onSend(draft) })
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { onSend(draft) }, enabled = draft.isNotBlank() && !state.chatSending) {
                            if (state.chatSending) CircularProgressIndicator(Modifier.size(21.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Outlined.Send, "إرسال", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }) { padding ->
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!state.error.isNullOrBlank()) item { Text(state.error, Modifier.fillMaxWidth().padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            if (state.messages.isEmpty()) item { Text("ابدأ المحادثة برسالة جديدة", Modifier.fillMaxWidth().padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(state.messages, key = { it.id }) { msg ->
                val mine = msg.senderId == state.profile?.id
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                    Surface(shape = RoundedCornerShape(if (mine) 18.dp else 18.dp), color = if (mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 320.dp).combinedClickable(onClick = {}, onLongClick = { if (!mine) { reportTarget = msg.senderId; reportMessageId = msg.id; reportNote = "نص الرسالة: ${msg.content}"; reportConfirmed = false; reportOpen = true } })) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                            Text(msg.content, style = MaterialTheme.typography.bodyLarge)
                            Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                                Text(msg.createdAt.take(16).replace('T',' '), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (mine) { Spacer(Modifier.width(4.dp)); Icon(Icons.Outlined.Done, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                    }
                }
            }
        }
    }
    if (groupInfoOpen && active?.isGroup == true) AlertDialog(
        onDismissRequest = { groupInfoOpen = false },
        title = { Text("معلومات المجموعة") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(active.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("محادثة جماعية · ${active.memberCount} أعضاء", style = MaterialTheme.typography.bodyMedium)
            Text("الأعضاء", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            state.chatMembers.forEach { member ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    NexusAvatar(member.avatarUrl, member.fullName, 30)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(member.fullName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text(member.universityNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text("تظهر الرسائل الجديدة تلقائياً أثناء فتح المحادثة.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } },
        confirmButton = { TextButton(onClick = { groupInfoOpen = false }) { Text("إغلاق") } }
    )
    if (blockConfirm && otherId != null) AlertDialog(onDismissRequest = { blockConfirm = false }, title = { Text("حظر هذا المستخدم؟") },
        text = { Text("لن تتمكن من متابعة التواصل معه. يمكنك إدارة الحظر لاحقاً من إعدادات الخصوصية.") },
        confirmButton = { TextButton(onClick = { onBlock(otherId); blockConfirm = false }) { Text("حظر المستخدم", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { blockConfirm = false }) { Text("إلغاء") } })
    if (reportOpen && reportTarget != null) AlertDialog(onDismissRequest = { reportOpen = false }, title = { Text("إرسال بلاغ") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(if (reportMessageId != null) "نوع البلاغ عن الرسالة المحددة" else "نوع البلاغ عن المستخدم", style = MaterialTheme.typography.labelMedium)
            listOf("محتوى مسيء أو تحرّش", "تهديد أو عنف", "انتحال شخصية", "رسائل مزعجة أو احتيال", "مشاركة معلومات خاصة", "أخرى").forEach { reason -> Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = reportReason == reason, onClick = { reportReason = reason }); Text(reason, style = MaterialTheme.typography.bodySmall) } }
            OutlinedTextField(reportNote, { reportNote = it }, Modifier.fillMaxWidth(), minLines = 2, maxLines = 4, label = { Text("تفاصيل إضافية (اختياري)") })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = reportConfirmed, onCheckedChange = { reportConfirmed = it })
                Text("أؤكد أن البلاغ يصف مخالفة حقيقية", style = MaterialTheme.typography.bodySmall)
            }
        } }, confirmButton = { TextButton(enabled = reportConfirmed, onClick = { onReport(reportTarget!!, reportReason, reportNote, reportMessageId, blockAfterReport); reportOpen = false }) { Text("إرسال البلاغ") } },
        dismissButton = { TextButton(onClick = { reportOpen = false }) { Text("إلغاء") } })
}

private fun Boolean?.orFalse(): Boolean = this ?: false
