package com.nexus.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.nexus.app.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(state: NexusUiState, onBack: () -> Unit, onComment: (String, Boolean, String?) -> Unit,
    onAccept: (String?) -> Unit, onDelete: (String) -> Unit, onMention: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    var anonymous by rememberSaveable { mutableStateOf(false) }
    var replyTo by rememberSaveable { mutableStateOf<String?>(null) }
    var lastCommentCounter by remember { mutableStateOf(state.commentSuccessCounter) }
    LaunchedEffect(state.commentSuccessCounter, state.lastCommentPostId) {
        if (state.commentSuccessCounter > lastCommentCounter && state.lastCommentPostId == state.postDetail?.post?.id) {
            draft = ""; anonymous = false; replyTo = null
            lastCommentCounter = state.commentSuccessCounter
        }
    }
    val detail = state.postDetail
    val isSubAdmin = state.profile?.roles?.contains("sub_admin") == true
    val isAuthor = detail?.post?.authorId == state.profile?.id
    val isAdmin = state.profile?.roles?.contains("admin") == true
    Scaffold(topBar = { TopAppBar(title = { Text("المنشور والتعليقات") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } }) },
        bottomBar = { Surface(Modifier.fillMaxWidth().imePadding(), tonalElevation = 3.dp) { Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (replyTo != null) Row(verticalAlignment = Alignment.CenterVertically) { Text("الرد على تعليق", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall); TextButton(onClick = { replyTo = null }) { Text("إلغاء الرد") } }
            OutlinedTextField(draft, { draft = it }, Modifier.fillMaxWidth(), enabled = !isSubAdmin, placeholder = { Text(if (isSubAdmin) "المشرف المساعد لا يملك صلاحية التعليق" else "اكتب تعليقاً أو رداً...") }, maxLines = 4, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(anonymous, { anonymous = it }, enabled = !isSubAdmin); Text("تعليق مجهول", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f)); Button(onClick = { onComment(draft, anonymous, replyTo) }, enabled = draft.isNotBlank() && !isSubAdmin) { Icon(Icons.Outlined.Send, null); Spacer(Modifier.width(5.dp)); Text("إرسال") }
            }
        } } }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.postDetailLoading && detail == null) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (detail != null) {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text(detail.post.authorName, Modifier.weight(1f), fontWeight = FontWeight.Bold); AssistChip(onClick = {}, label = { Text(if (detail.post.postType == "question") "سؤال" else "منشور") }) }
                        ExpandablePostText(detail.post.content, onMention = onMention)
                        detail.post.images.forEach { AsyncImage(it, "صورة المنشور", Modifier.fillMaxWidth().heightIn(max = 340.dp)) }
                        if (detail.post.postType == "question" && isAuthor && detail.post.acceptedAnswerId != null) TextButton(onClick = { onAccept(null) }) { Text("إلغاء اعتماد الإجابة") }
                        Text("${detail.comments.size} تعليق", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } }
                }
                val roots = detail.comments.filter { it.parentId == null }
                if (roots.isEmpty()) item { Text("لا توجد تعليقات بعد.", Modifier.fillMaxWidth().padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                itemsIndexed(roots, key = { _, comment -> comment.id }) { index, comment ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                        CommentThreadItem(comment, detail.comments.filter { it.parentId == comment.id }, detail.post, state.profile, isAdmin,
                            onReply = { replyTo = it }, onAccept = onAccept, onDelete = onDelete, onMention = onMention)
                    }
            }
        }
    }
}
}

@Composable
private fun CommentThreadItem(comment: FeedComment, replies: List<FeedComment>, post: FeedPost, profile: StudentProfile?, isAdmin: Boolean,
    onReply: (String) -> Unit, onAccept: (String?) -> Unit, onDelete: (String) -> Unit, onMention: (String) -> Unit) {
    val accepted = post.acceptedAnswerId == comment.id
    Card(shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = if (accepted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NexusAvatar(comment.avatarUrl, comment.authorName, 34)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(comment.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(comment.createdAt.take(16).replace('T',' '), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (accepted) Icon(Icons.Outlined.CheckCircle, "إجابة معتمدة", tint = MaterialTheme.colorScheme.tertiary)
            }
            ExpandablePostText(comment.content, collapsedLines = 3, onMention = onMention)
            if (comment.revealedAuthor != null) Text("الهوية (للإدارة فقط): ${comment.revealedAuthor.fullName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onReply(comment.id) }) { Text("رد") }
                if (post.postType == "question" && profile?.id == post.authorId) TextButton(onClick = { onAccept(if (accepted) null else comment.id) }) { Text(if (accepted) "إلغاء الاعتماد" else "اعتماد الإجابة") }
                if (comment.authorId == profile?.id || comment.isMyAnonymous || isAdmin) TextButton(onClick = { onDelete(comment.id) }) { Text("حذف") }
            }
            replies.forEachIndexed { index, reply ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .75f))
                Row(Modifier.padding(start = 18.dp, top = 5.dp, bottom = 3.dp), verticalAlignment = Alignment.Top) {
                    NexusAvatar(reply.avatarUrl, reply.authorName, 28)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(reply.authorName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(reply.createdAt.take(16).replace('T',' '), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ExpandablePostText(reply.content, collapsedLines = 2, onMention = onMention)
                        if (reply.revealedAuthor != null) Text("الهوية (للإدارة فقط): ${reply.revealedAuthor.fullName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        Row { TextButton(onClick = { onReply(reply.id) }) { Text("رد") }; if (reply.authorId == profile?.id || reply.isMyAnonymous || isAdmin) TextButton(onClick = { onDelete(reply.id) }) { Text("حذف") } }
                    }
                }
            }
        }
    }
}
