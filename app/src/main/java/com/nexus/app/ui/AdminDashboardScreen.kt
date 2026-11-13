package com.nexus.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import com.nexus.app.data.*

private data class AdminTab(val id: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    state: NexusUiState,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onWarn: (String, String) -> Unit,
    onSuspend: (String, Int, String) -> Unit,
    onBan: (String, String) -> Unit,
    onUnban: (String) -> Unit,
    onAdjustPoints: (String, Int) -> Unit,
    onSetVerified: (String, Boolean) -> Unit,
    onSetYear: (String, Int) -> Unit,
    onDeleteUser: (String) -> Unit,
    onUpdateReport: (AdminReport, String) -> Unit,
    onAddWord: (String) -> Unit,
    onDeleteWord: (String) -> Unit,
    onBroadcast: (String, String, Int?, String?) -> Unit,
    onCreateTeacher: (String, String, String, List<String>) -> Unit,
    onCreateSubAdmin: (String, String, String, Map<String, Boolean>) -> Unit,
    onDeleteSubAdmin: (String) -> Unit,
    onUpdateSubAdminPermissions: (String, Map<String, Boolean>) -> Unit
) {
    val isAdmin = state.profile?.roles?.contains("admin") == true
    val permissions = state.profile?.subAdminPermissions.orEmpty()
    val allTabs = listOf(
        AdminTab("overview", "نظرة عامة"), AdminTab("users", "المستخدمون"), AdminTab("reports", "البلاغات"),
        AdminTab("courses", "المقررات"), AdminTab("teachers", "الأساتذة"), AdminTab("subadmins", "المشرفون"),
        AdminTab("words", "الكلمات"), AdminTab("activity", "النشاط"), AdminTab("pinned", "المثبت"),
        AdminTab("broadcast", "إشعارات جماعية"), AdminTab("names", "طلبات الأسماء")
    )
    val tabs = if (isAdmin) allTabs else buildList {
        add(allTabs[0]); add(allTabs[1])
        if ("courses" in permissions) add(allTabs[3])
        if ("teachers" in permissions) add(allTabs[4])
        if ("words" in permissions) add(allTabs[6])
        if ("reports" in permissions) add(allTabs[2])
    }
    var tab by rememberSaveable { mutableStateOf("overview") }
    var userSearch by rememberSaveable { mutableStateOf("") }
    var courseSearch by rememberSaveable { mutableStateOf("") }
    var courseMajorFilter by rememberSaveable { mutableStateOf("الكل") }
    var expandedUser by rememberSaveable { mutableStateOf<String?>(null) }
    var actionUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var actionType by rememberSaveable { mutableStateOf("") }
    var actionReason by rememberSaveable { mutableStateOf("") }
    var actionYear by rememberSaveable { mutableStateOf("1") }
    var wordDraft by rememberSaveable { mutableStateOf("") }
    var broadcastTitle by rememberSaveable { mutableStateOf("") }
    var broadcastBody by rememberSaveable { mutableStateOf("") }
    var broadcastMode by rememberSaveable { mutableStateOf("all") }
    var broadcastYear by rememberSaveable { mutableStateOf("1") }
    var broadcastMajor by rememberSaveable { mutableStateOf("") }
    var teacherFormOpen by rememberSaveable { mutableStateOf(false) }
    var teacherName by rememberSaveable { mutableStateOf("") }
    var teacherEmail by rememberSaveable { mutableStateOf("") }
    var teacherPassword by rememberSaveable { mutableStateOf("") }
    var teacherCourseIds by remember { mutableStateOf(setOf<String>()) }
    var subAdminFormOpen by rememberSaveable { mutableStateOf(false) }
    var subAdminLoginId by rememberSaveable { mutableStateOf("") }
    var subAdminName by rememberSaveable { mutableStateOf("") }
    var subAdminPassword by rememberSaveable { mutableStateOf("") }
    var subAdminFlags by remember { mutableStateOf(mapOf("can_reports" to true, "can_teachers" to true, "can_courses" to true, "can_warn" to true, "can_suspend" to true, "can_words" to true)) }
    var deleteSubAdminId by rememberSaveable { mutableStateOf<String?>(null) }
    if (tabs.none { it.id == tab }) tab = "overview"
    val selectedIndex = tabs.indexOfFirst { it.id == tab }.coerceAtLeast(0)
    val actionUser = state.adminUsers.firstOrNull { it.id == actionUserId }
    val filteredUsers = state.adminUsers.filter { userSearch.isBlank() || userSearch.let { q -> userSearchMatch(q, it) } }
    val filteredAdminCourses = state.adminCourses.filter { course ->
        (courseSearch.isBlank() || course.name.contains(courseSearch, true) || course.major.contains(courseSearch, true) || course.teacherName.orEmpty().contains(courseSearch, true)) &&
            (courseMajorFilter == "الكل" || course.major == courseMajorFilter)
    }
    val adminCourseMajors = state.adminCourses.map { it.major }.filter(String::isNotBlank).distinct().sorted()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("مركز الإدارة", fontWeight = FontWeight.Bold); Text(if (isAdmin) "صلاحيات المدير العام" else "صلاحيات المشرف المساعد", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } },
                actions = { IconButton(onClick = onRefresh, enabled = !state.adminLoading) { Icon(Icons.Outlined.Refresh, "تحديث البيانات") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.adminLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (!state.error.isNullOrBlank()) Text(state.error, Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            ScrollableTabRow(selectedTabIndex = selectedIndex, edgePadding = 10.dp) {
                tabs.forEach { item -> Tab(selected = tab == item.id, onClick = { tab = item.id }, text = { Text(item.label, maxLines = 1) }) }
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (tab) {
                    "overview" -> {
                        item { Text("ملخص المنصة", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AdminMetric("المستخدمون", state.adminUsers.size.toString(), Icons.Outlined.People, Modifier.weight(1f))
                                AdminMetric("المقررات", state.adminCourses.size.toString(), Icons.Outlined.MenuBook, Modifier.weight(1f))
                            }
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AdminMetric("البلاغات", state.adminReports.size.toString(), Icons.Outlined.Flag, Modifier.weight(1f))
                                AdminMetric("الكلمات", state.bannedWords.size.toString(), Icons.Outlined.GppBad, Modifier.weight(1f))
                            }
                        }
                        item { AdminSectionTitle("إجراءات سريعة") }
                        item { AdminQuickActions(onTab = { target -> if (tabs.any { it.id == target }) tab = target }) }
                        item { AdminSectionTitle("آخر النشاط الإداري") }
                        if (state.adminActivities.isEmpty()) item { AdminEmpty("لا توجد سجلات نشاط متاحة أو لا تملك الصلاحية لقراءتها.") }
                        items(state.adminActivities.take(6), key = { "activity-${it.id}" }) { AdminActivityRow(it) }
                    }
                    "users" -> {
                        item { Text("إدارة المستخدمين", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        item { OutlinedTextField(userSearch, { userSearch = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("بحث بالاسم أو الرقم الجامعي أو الدور") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }) }
                        item { Text("${filteredUsers.size} حساب ضمن النتائج المحمّلة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        items(filteredUsers, key = { "admin-user-${it.id}" }) { user ->
                            AdminUserCard(user, expandedUser == user.id, onExpand = { expandedUser = if (expandedUser == user.id) null else user.id },
                                onWarn = { actionUserId = user.id; actionType = "warn"; actionReason = "" },
                                onSuspend = { actionUserId = user.id; actionType = "suspend"; actionReason = "" },
                                onBan = { actionUserId = user.id; actionType = "ban"; actionReason = "" },
                                onUnban = { onUnban(user.id) },
                                onVerify = { onSetVerified(user.id, !user.verified) },
                                onPoints = { onAdjustPoints(user.id, 10) },
                                onYear = { actionUserId = user.id; actionType = "year"; actionYear = (user.year ?: 1).toString() },
                                onDelete = { actionUserId = user.id; actionType = "delete"; actionReason = "" },
                                isAdmin = isAdmin, canWarn = isAdmin || "warn" in permissions, canSuspend = isAdmin || "suspend" in permissions)
                        }
                    }
                    "reports" -> {
                        item { Text("صندوق البلاغات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        if (!isAdmin) item { AdminEmpty("صلاحية عرض البلاغات موجودة في واجهة الموقع لبعض المشرفين، لكن سياسات قاعدة البيانات الحية لا تسمح لهم بقراءتها حالياً.") }
                        else if (state.adminReports.isEmpty()) item { AdminEmpty("لا توجد بلاغات حالياً.") }
                        items(if (isAdmin) state.adminReports else emptyList(), key = { "report-${it.reportType}-${it.id}" }) { report ->
                            AdminReportCard(report, onStatus = { onUpdateReport(report, it) })
                        }
                    }
                    "courses" -> {
                        item { Text("إدارة المقررات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        item { Text("عرض المقررات المسجلة والأساتذة المرتبطين بها.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        item { OutlinedTextField(courseSearch, { courseSearch = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("ابحث عن مقرر أو أستاذ أو تخصص") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }) }
                        item { Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(courseMajorFilter == "الكل", { courseMajorFilter = "الكل" }, label = { Text("الكل") })
                            adminCourseMajors.forEach { major -> FilterChip(courseMajorFilter == major, { courseMajorFilter = major }, label = { Text(major) }) }
                        } }
                        if (filteredAdminCourses.isEmpty()) item { AdminEmpty("لا توجد مقررات مطابقة.") }
                        items(filteredAdminCourses, key = { "admin-course-${it.id}" }) { course ->
                            Card(shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(course.name, fontWeight = FontWeight.SemiBold)
                                    Text("${course.major} · السنة ${course.year} · الفصل ${course.semester}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("الأستاذ: ${course.teacherName ?: "غير معيّن"}", style = MaterialTheme.typography.bodySmall)
                                    Text("${course.filesCount} ملف · ${course.linksCount} رابط", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    "teachers" -> {
                        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("الأساتذة", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); if (isAdmin || "teachers" in permissions) TextButton(onClick = { teacherFormOpen = !teacherFormOpen }) { Icon(Icons.Outlined.PersonAdd, null); Text("إضافة أستاذ") } } }
                        if (teacherFormOpen) {
                            item { OutlinedTextField(teacherName, { teacherName = it }, Modifier.fillMaxWidth(), label = { Text("اسم الأستاذ") }, singleLine = true) }
                            item { OutlinedTextField(teacherEmail, { teacherEmail = it }, Modifier.fillMaxWidth(), label = { Text("البريد الإلكتروني") }, singleLine = true) }
                            item { OutlinedTextField(teacherPassword, { teacherPassword = it }, Modifier.fillMaxWidth(), label = { Text("كلمة المرور الأولية") }, visualTransformation = PasswordVisualTransformation(), singleLine = true) }
                            item { Text("المقررات التي سيدرسها", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
                            items(state.adminCourses, key = { "teacher-course-${it.id}" }) { course ->
                                Row(Modifier.fillMaxWidth().clickable { teacherCourseIds = if (course.id in teacherCourseIds) teacherCourseIds - course.id else teacherCourseIds + course.id }.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = course.id in teacherCourseIds, onCheckedChange = { checked -> teacherCourseIds = if (checked) teacherCourseIds + course.id else teacherCourseIds - course.id })
                                    Column(Modifier.weight(1f)) { Text(course.name, style = MaterialTheme.typography.bodyMedium); Text("${course.major} · سنة ${course.year} · فصل ${course.semester}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                            item { Button(onClick = { onCreateTeacher(teacherName, teacherEmail, teacherPassword, teacherCourseIds.toList()) }, enabled = teacherName.isNotBlank() && teacherEmail.contains("@") && teacherPassword.length >= 6 && !state.busy, modifier = Modifier.fillMaxWidth()) { Text("إنشاء حساب الأستاذ وتعيين المقررات") } }
                        }
                        val teachers = state.adminUsers.filter { "teacher" in it.roles }
                        if (teachers.isEmpty()) item { AdminEmpty("لا توجد حسابات أستاذ ظاهرة ضمن قائمة المستخدمين المحمّلة.") }
                        items(teachers, key = { "teacher-${it.id}" }) { AdminUserCard(it, false, {}, {}, {}, {}, {}, {}, {}, {}, {}, isAdmin = false, canWarn = false, canSuspend = false) }
                    }
                    "subadmins" -> {
                        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("المشرفون المساعدون", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); TextButton(onClick = { subAdminFormOpen = !subAdminFormOpen }) { Icon(Icons.Outlined.PersonAdd, null); Text("إضافة مشرف") } } }
                        if (subAdminFormOpen) {
                            item { OutlinedTextField(subAdminLoginId, { subAdminLoginId = it }, Modifier.fillMaxWidth(), label = { Text("اسم الدخول") }, supportingText = { Text("سيكون الدخول عبر ${subAdminLoginId.ifBlank { "username" }}@subadmin.edu") }, singleLine = true) }
                            item { OutlinedTextField(subAdminName, { subAdminName = it }, Modifier.fillMaxWidth(), label = { Text("الاسم الكامل") }, singleLine = true) }
                            item { OutlinedTextField(subAdminPassword, { subAdminPassword = it }, Modifier.fillMaxWidth(), label = { Text("كلمة المرور الأولية") }, visualTransformation = PasswordVisualTransformation(), singleLine = true) }
                            item { Text("الصلاحيات", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
                            items(listOf("can_reports" to "البلاغات", "can_teachers" to "الأساتذة", "can_courses" to "المقررات", "can_warn" to "التحذير", "can_suspend" to "الإيقاف", "can_words" to "الكلمات المحظورة"), key = { "new-perm-${it.first}" }) { (key, label) ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f)); Switch(checked = subAdminFlags[key] == true, onCheckedChange = { subAdminFlags = subAdminFlags + (key to it) }) }
                            }
                            item { Button(onClick = { onCreateSubAdmin(subAdminLoginId, subAdminName, subAdminPassword, subAdminFlags) }, enabled = subAdminLoginId.isNotBlank() && subAdminName.isNotBlank() && subAdminPassword.length >= 6 && !state.busy, modifier = Modifier.fillMaxWidth()) { Text("إنشاء حساب المشرف المساعد") } }
                        }
                        if (state.adminSubAdmins.isEmpty()) item { AdminEmpty("لا توجد حسابات مشرف مساعد أو لا تملك صلاحية عرضها.") }
                        items(state.adminSubAdmins, key = { "subadmin-${it.id}" }) { sub ->
                            Card(shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(sub.fullName, fontWeight = FontWeight.SemiBold); Text(sub.universityNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; IconButton(onClick = { deleteSubAdminId = sub.id }) { Icon(Icons.Outlined.DeleteOutline, "حذف المشرف", tint = MaterialTheme.colorScheme.error) } }
                                    listOf("can_reports" to "البلاغات", "can_teachers" to "الأساتذة", "can_courses" to "المقررات", "can_warn" to "التحذير", "can_suspend" to "الإيقاف", "can_words" to "الكلمات المحظورة").forEach { (key, label) ->
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall); Switch(checked = sub.permissionFlags[key] == true, onCheckedChange = { enabled -> onUpdateSubAdminPermissions(sub.id, sub.permissionFlags + (key to enabled)) }) }
                                    }
                                }
                            }
                        }
                    }
                    "words" -> {
                        item { Text("الكلمات المحظورة", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        item { Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(wordDraft, { wordDraft = it }, Modifier.weight(1f), singleLine = true, placeholder = { Text("أضف كلمة أو عبارة") })
                            Spacer(Modifier.width(8.dp)); Button(onClick = { onAddWord(wordDraft); wordDraft = "" }, enabled = isAdmin && wordDraft.trim().length >= 2 && !state.busy) { Icon(Icons.Outlined.Add, null) }
                        } }
                        if (!isAdmin) item { AdminEmpty("يمكن للمشرف المساعد الاطلاع على القائمة فقط؛ إدارة الكلمات محصورة بالمدير العام في قاعدة البيانات.") }
                        if (state.bannedWords.isEmpty()) item { AdminEmpty("قائمة الكلمات فارغة أو لا تملك صلاحية قراءتها.") }
                        items(state.bannedWords, key = { "word-${it.id}" }) { word ->
                            Card(shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(word.word, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                    IconButton(onClick = { onDeleteWord(word.id) }, enabled = isAdmin) { Icon(Icons.Outlined.DeleteOutline, "حذف الكلمة", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                    "activity" -> {
                        item { Text("سجل النشاط الإداري", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        if (state.adminActivities.isEmpty()) item { AdminEmpty("لا توجد سجلات نشاط متاحة.") }
                        items(state.adminActivities, key = { "full-activity-${it.id}" }) { AdminActivityRow(it) }
                    }
                    "pinned" -> {
                        item { Text("المحتوى المثبت", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        val pinned = state.pinnedCard
                        if (pinned == null) item { AdminEmpty("لا توجد بطاقة مثبتة متاحة.") }
                        else item {
                            Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) { Text(if (pinned.enabled) "مفعّلة" else "متوقفة", color = if (pinned.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium); Spacer(Modifier.weight(1f)); Text(pinned.badgeText, style = MaterialTheme.typography.labelSmall) }
                                    Text(pinned.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    if (!pinned.imageUrl.isNullOrBlank()) AsyncImage(pinned.imageUrl, "صورة المحتوى المثبت", Modifier.fillMaxWidth().heightIn(max = 190.dp), contentScale = ContentScale.Crop)
                                    Text(pinned.description)
                                    Text("النوع: ${pinned.type} · القالب: ${pinned.theme}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("التصويتات: ${pinned.votes.size} · المشاركات: ${pinned.participants.size}", style = MaterialTheme.typography.bodySmall)
                                    if (!pinned.targetMajor.isNullOrBlank() || pinned.targetYear != null) Text("الاستهداف: ${pinned.targetMajor ?: "كل التخصصات"} · ${pinned.targetYear?.let { "السنة $it" } ?: "كل السنوات"}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    "broadcast" -> {
                        item { Text("إشعار جماعي", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        item { Text("إرسال إعلان إلى كل الطلاب أو استهداف سنة/تخصص محدد.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        item { OutlinedTextField(broadcastTitle, { broadcastTitle = it }, Modifier.fillMaxWidth(), label = { Text("عنوان الإشعار") }, singleLine = true) }
                        item { OutlinedTextField(broadcastBody, { broadcastBody = it }, Modifier.fillMaxWidth(), label = { Text("نص الإشعار") }, minLines = 3, maxLines = 6) }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(broadcastMode == "all", { broadcastMode = "all" }, label = { Text("الكل") })
                            FilterChip(broadcastMode == "year", { broadcastMode = "year" }, label = { Text("سنة") })
                            FilterChip(broadcastMode == "major", { broadcastMode = "major" }, label = { Text("تخصص") })
                        } }
                        if (broadcastMode == "year") item { OutlinedTextField(broadcastYear, { broadcastYear = it.filter(Char::isDigit).take(1) }, Modifier.fillMaxWidth(), label = { Text("السنة الدراسية") }, singleLine = true) }
                        if (broadcastMode == "major") item { OutlinedTextField(broadcastMajor, { broadcastMajor = it }, Modifier.fillMaxWidth(), label = { Text("اسم التخصص") }, singleLine = true) }
                        item { Button(onClick = { onBroadcast(broadcastTitle, broadcastBody, if (broadcastMode == "year") broadcastYear.toIntOrNull() else null, if (broadcastMode == "major") broadcastMajor.trim().takeIf(String::isNotBlank) else null) }, enabled = broadcastTitle.isNotBlank() && broadcastBody.isNotBlank() && !state.busy, modifier = Modifier.fillMaxWidth()) {
                            if (state.busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Icon(Icons.Outlined.Campaign, null)
                            Spacer(Modifier.width(7.dp)); Text("إرسال الإشعار")
                        } }
                    }
                    "names" -> {
                        item { Text("طلبات تغيير الاسم", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        item { AdminEmpty("واجهة طلبات تغيير الاسم موجودة في الموقع، لكن جدول الطلبات وعمود الحالة غير موجودين في قاعدة البيانات الحية وفق التدقيق؛ لذلك لا يمكن عرض طلبات حقيقية أو اتخاذ إجراء عليها الآن.") }
                    }
                }
            }
        }
    }

    if (deleteSubAdminId != null) AlertDialog(
        onDismissRequest = { deleteSubAdminId = null },
        title = { Text("حذف المشرف المساعد؟") },
        text = { Text("سيتم حذف حساب المشرف وصلاحياته. لا يمكن التراجع عن ذلك.") },
        confirmButton = { TextButton(onClick = { deleteSubAdminId?.let(onDeleteSubAdmin); deleteSubAdminId = null }) { Text("حذف", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { deleteSubAdminId = null }) { Text("إلغاء") } }
    )
    if (actionUser != null && actionType.isNotBlank()) {
        val title = when (actionType) { "warn" -> "إرسال تحذير"; "suspend" -> "إيقاف الحساب 7 أيام"; "ban" -> "حظر الحساب"; "year" -> "تغيير السنة الدراسية"; else -> "حذف الحساب نهائياً" }
        AlertDialog(
            onDismissRequest = { actionUserId = null; actionType = "" },
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(actionUser?.fullName.orEmpty(), fontWeight = FontWeight.SemiBold)
                    when (actionType) {
                        "warn", "suspend", "ban" -> OutlinedTextField(actionReason, { actionReason = it }, Modifier.fillMaxWidth(), label = { Text("السبب") }, minLines = 2)
                        "year" -> OutlinedTextField(actionYear, { actionYear = it.filter(Char::isDigit).take(1) }, Modifier.fillMaxWidth(), label = { Text("السنة: 1 أو 2 أو 3") }, singleLine = true)
                        "delete" -> Text("سيتم حذف حساب الطالب وبياناته المرتبطة. هذا الإجراء لا يمكن التراجع عنه.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = { TextButton(onClick = {
                val id = actionUser?.id
                if (id != null) when (actionType) {
                    "warn" -> onWarn(id, actionReason)
                    "suspend" -> onSuspend(id, 7, actionReason)
                    "ban" -> onBan(id, actionReason)
                    "year" -> actionYear.toIntOrNull()?.takeIf { it in 1..3 }?.let { onSetYear(id, it) }
                    "delete" -> onDeleteUser(id)
                }
                actionUserId = null; actionType = ""
            }, enabled = actionType in setOf("delete", "year") || actionReason.isNotBlank()) { Text("تأكيد") } },
            dismissButton = { TextButton(onClick = { actionUserId = null; actionType = "" }) { Text("إلغاء") } }
        )
    }
}

private fun userSearchMatch(query: String, user: AdminUser): Boolean =
    user.fullName.contains(query, true) || user.universityNumber.contains(query, true) || user.roles.any { it.contains(query, true) }

@Composable
private fun AdminMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminQuickActions(onTab: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf("users" to "المستخدمون", "reports" to "البلاغات", "courses" to "المقررات", "words" to "الكلمات", "broadcast" to "إشعار جماعي").forEach { (id, label) ->
            AssistChip(onClick = { onTab(id) }, label = { Text(label) }, leadingIcon = { Icon(Icons.Outlined.Bolt, null) })
        }
    }
}

@Composable
private fun AdminSectionTitle(title: String) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

@Composable
private fun AdminEmpty(text: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, Modifier.fillMaxWidth().padding(15.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AdminUserCard(
    user: AdminUser, expanded: Boolean, onExpand: () -> Unit,
    onWarn: () -> Unit, onSuspend: () -> Unit, onBan: () -> Unit, onUnban: () -> Unit,
    onVerify: () -> Unit, onPoints: () -> Unit, onYear: () -> Unit, onDelete: () -> Unit,
    isAdmin: Boolean, canWarn: Boolean, canSuspend: Boolean
) {
    Card(shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().clickable(onClick = onExpand).padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NexusAvatar(user.avatarUrl, user.fullName, 42)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(user.fullName, fontWeight = FontWeight.SemiBold)
                    Text("${user.universityNumber} · ${user.major ?: "—"} · ${user.year?.let { "سنة $it" } ?: "—"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${user.points} نقطة · ${user.roles.joinToString()}", style = MaterialTheme.typography.labelSmall)
                }
                if (user.banned) AssistChip(onClick = {}, label = { Text("محظور") })
                else if (user.suspendedUntil != null) AssistChip(onClick = {}, label = { Text("موقوف") })
                Icon(Icons.Outlined.ExpandMore, null)
            }
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (canWarn) TextButton(onClick = onWarn) { Text("تحذير") }
                    if (canSuspend) TextButton(onClick = onSuspend) { Text("إيقاف") }
                    if (isAdmin) {
                        TextButton(onClick = if (user.banned) onUnban else onBan) { Text(if (user.banned) "رفع الحظر" else "حظر") }
                        TextButton(onClick = onVerify) { Text(if (user.verified) "إلغاء التوثيق" else "توثيق") }
                    }
                }
                if (isAdmin) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    TextButton(onClick = onPoints) { Text("+10 نقاط") }
                    TextButton(onClick = onYear) { Text("تغيير السنة") }
                    TextButton(onClick = onDelete) { Text("حذف", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@Composable
private fun AdminReportCard(report: AdminReport, onStatus: (String) -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(if (report.reportType == "message") "بلاغ رسالة" else "بلاغ منشور") })
                Spacer(Modifier.weight(1f)); Text(report.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(report.reason, fontWeight = FontWeight.SemiBold)
            Text(report.content, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Text("المبلّغ: ${report.reporterName} · ${report.createdAt.take(16).replace('T',' ')}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (report.reportType == "message") Text(if (report.messageId == null) "لم يُحفظ معرّف الرسالة في قاعدة البيانات بعد" else "معرّف الرسالة: ${report.messageId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onStatus("dismissed") }) { Text("رفض البلاغ") }
                Button(onClick = { onStatus("confirmed") }) { Text("تأكيد البلاغ") }
            }
        }
    }
}

@Composable
private fun AdminActivityRow(activity: AdminActivity) {
    Card(shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(activity.action, fontWeight = FontWeight.SemiBold)
            Text(activity.details, style = MaterialTheme.typography.bodySmall)
            Text("${activity.adminName} ← ${activity.targetName} · ${activity.createdAt.take(16).replace('T',' ')}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
