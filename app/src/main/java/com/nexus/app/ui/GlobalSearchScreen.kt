package com.nexus.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.nexus.app.data.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(state: NexusUiState, onSearch: (String) -> Unit, onBack: () -> Unit,
    onOpenPost: (String) -> Unit, onOpenCourse: (String) -> Unit, onStartChat: (String) -> Unit, onOpenProfile: (String) -> Unit) {
    var query by rememberSaveable(state.searchQuery) { mutableStateOf(state.searchQuery) }
    var tab by rememberSaveable { mutableStateOf("all") }
    val results = state.searchResults
    LaunchedEffect(query) {
        delay(300)
        onSearch(query)
    }
    val tabs = listOf(
        "all" to "الكل", "posts" to "المنشورات", "questions" to "الأسئلة", "comments" to "التعليقات",
        "courses" to "المقررات", "materials" to "المواد", "users" to "الطلاب"
    )
    Scaffold(topBar = { TopAppBar(title = { Text("البحث") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                singleLine = true,
                placeholder = { Text("ابحث عن منشور أو سؤال أو تعليق أو مقرر أو طالب") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, "مسح البحث") } },
                shape = RoundedCornerShape(16.dp)
            )
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                tabs.forEach { (key, label) ->
                    val count = when (key) {
                        "posts" -> results?.posts?.size
                        "questions" -> results?.posts?.count { it.postType == "question" }
                        "comments" -> results?.comments?.size
                        "courses" -> results?.courses?.size
                        "materials" -> results?.materials?.size
                        "users" -> results?.users?.size
                        else -> null
                    }
                    FilterChip(selected = tab == key, onClick = { tab = key }, label = { Text(if (count != null) "$label $count" else label) })
                }
            }
            if (state.searchLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 5.dp))
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                if (query.trim().length < 2) {
                    item { Text("اكتب حرفين على الأقل لبدء البحث في محتوى NEXUS.", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else if (results == null && !state.searchLoading) {
                    item { Text("لا توجد نتائج بعد.", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else when (tab) {
                    "posts" -> {
                        val rows = results?.posts.orEmpty().filter { it.postType != "question" }
                        if (rows.isEmpty() && !state.searchLoading) item { EmptySearch("لا توجد منشورات مطابقة") }
                        items(rows, key = { "post-${it.id}" }) { post -> SearchPostRow(post, onOpenPost) }
                    }
                    "questions" -> {
                        val rows = results?.posts.orEmpty().filter { it.postType == "question" }
                        if (rows.isEmpty() && !state.searchLoading) item { EmptySearch("لا توجد أسئلة مطابقة") }
                        items(rows, key = { "question-${it.id}" }) { post -> SearchPostRow(post, onOpenPost) }
                    }
                    "comments" -> {
                        val rows = results?.comments.orEmpty()
                        if (rows.isEmpty() && !state.searchLoading) item { EmptySearch("لا توجد تعليقات مطابقة") }
                        items(rows, key = { "comment-${it.id}" }) { comment ->
                            Card(onClick = { onOpenPost(comment.postId) }, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(comment.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                    Text(comment.content, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Text("تعليق على منشور · ${comment.createdAt.take(10)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    "courses" -> {
                        val rows = results?.courses.orEmpty()
                        if (rows.isEmpty() && !state.searchLoading) item { EmptySearch("لا توجد مقررات مطابقة") }
                        items(rows, key = { "course-${it.id}" }) { course ->
                            Card(onClick = { onOpenCourse(course.id) }, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
                                    Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(course.name, fontWeight = FontWeight.SemiBold); Text("${course.major} · السنة ${course.year} · الفصل ${course.semester}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    Icon(Icons.Outlined.ChevronLeft, null)
                                }
                            }
                        }
                    }
                    "materials" -> {
                        val rows = results?.materials.orEmpty()
                        if (rows.isEmpty() && !state.searchLoading) item { EmptySearch("لا توجد مواد أو ملفات مطابقة") }
                        items(rows, key = { "material-${it.id}" }) { material ->
                            Card(onClick = { onOpenCourse(material.courseId) }, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (material.linkType == "file") Icons.Outlined.InsertDriveFile else Icons.Outlined.Link, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(material.title, fontWeight = FontWeight.SemiBold); Text(material.courseName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    Icon(Icons.Outlined.ChevronLeft, null)
                                }
                            }
                        }
                    }
                    "users" -> {
                        val rows = results?.users.orEmpty()
                        if (rows.isEmpty() && !state.searchLoading) item { EmptySearch("لا يوجد طلاب مطابقون") }
                        items(rows, key = { "user-${it.id}" }) { user -> SearchUserRow(user, onOpenProfile, onStartChat) }
                    }
                    else -> {
                        val posts = results?.posts.orEmpty().take(5)
                        val comments = results?.comments.orEmpty().take(4)
                        val courses = results?.courses.orEmpty().take(4)
                        val materials = results?.materials.orEmpty().take(4)
                        val users = results?.users.orEmpty().take(4)
                        if (posts.isEmpty() && comments.isEmpty() && courses.isEmpty() && materials.isEmpty() && users.isEmpty() && !state.searchLoading) item { EmptySearch("لا توجد نتائج مطابقة") }
                        if (posts.isNotEmpty()) { item { SearchSectionTitle("المنشورات والأسئلة") }; items(posts, key = { "all-post-${it.id}" }) { SearchPostRow(it, onOpenPost) } }
                        if (comments.isNotEmpty()) { item { SearchSectionTitle("التعليقات") }; items(comments, key = { "all-comment-${it.id}" }) { comment -> Card(onClick = { onOpenPost(comment.postId) }, shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) { Text(comment.authorName, fontWeight = FontWeight.SemiBold); Text(comment.content, maxLines = 2, overflow = TextOverflow.Ellipsis) } } } }
                        if (courses.isNotEmpty()) { item { SearchSectionTitle("المقررات") }; items(courses, key = { "all-course-${it.id}" }) { course -> Card(onClick = { onOpenCourse(course.id) }, shape = RoundedCornerShape(12.dp)) { Text(course.name, Modifier.fillMaxWidth().padding(13.dp), fontWeight = FontWeight.SemiBold) } } }
                        if (materials.isNotEmpty()) { item { SearchSectionTitle("المواد والملفات") }; items(materials, key = { "all-material-${it.id}" }) { material -> Card(onClick = { onOpenCourse(material.courseId) }, shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) { Text(material.title, fontWeight = FontWeight.SemiBold); Text(material.courseName, style = MaterialTheme.typography.labelSmall) } } } }
                        if (users.isNotEmpty()) { item { SearchSectionTitle("الطلاب") }; items(users, key = { "all-user-${it.id}" }) { SearchUserRow(it, onOpenProfile, onStartChat) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPostRow(post: FeedPost, onOpenPost: (String) -> Unit) {
    Card(onClick = { onOpenPost(post.id) }, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(post.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(post.content, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Text(if (post.postType == "question") "سؤال" else "منشور", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SearchUserRow(user: PublicUser, onOpenProfile: (String) -> Unit, onStartChat: (String) -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!user.avatarUrl.isNullOrBlank()) AsyncImage(user.avatarUrl, null, Modifier.size(42.dp).clip(CircleShape))
            else Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Text(user.fullName.take(1)) } }
            Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(user.fullName, fontWeight = FontWeight.SemiBold); Text(user.universityNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = { onOpenProfile(user.id) }) { Icon(Icons.Outlined.Person, "الملف الشخصي") }
            IconButton(onClick = { onStartChat(user.id) }) { Icon(Icons.Outlined.Chat, "مراسلة") }
        }
    }
}

@Composable
private fun SearchSectionTitle(title: String) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }

@Composable
private fun EmptySearch(text: String) { Text(text, Modifier.fillMaxWidth().padding(22.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
