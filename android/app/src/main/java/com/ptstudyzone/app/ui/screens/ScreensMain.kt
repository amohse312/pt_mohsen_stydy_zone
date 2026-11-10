package com.ptstudyzone.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptstudyzone.app.data.*
import com.ptstudyzone.app.ui.AppViewModel
import com.ptstudyzone.app.ui.theme.Gold
import com.ptstudyzone.app.ui.theme.Navy
import com.ptstudyzone.app.ui.theme.TealMed
import kotlinx.coroutines.launch

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column { Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp); Text(label, color = Color.Gray, fontSize = 13.sp) }
        }
    }
}

@Composable
fun SubjectRow(s: Subject, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Brush.linearGradient(listOf(Navy, TealMed)), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(s.subjectName.take(1), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.subjectName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${s.stats?.completed ?: 0}/${s.stats?.total ?: 0} مكتمل • ${s.visibility}", color = Color.Gray, fontSize = 12.sp)
                LinearProgressIndicator(progress = { ((s.stats?.progress ?: 0f) / 100f) }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), color = Gold, trackColor = Color(0xFFE3EAF2))
            }
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
        }
    }
}

// ---------- لوحة التحكم ----------
@Composable
fun HomeScreen(vm: AppViewModel, goSubjects: () -> Unit, goChallenges: () -> Unit, goSubject: (Int) -> Unit, goNotifications: () -> Unit) {
    val dash by vm.dashboard.collectAsState()
    val subs by vm.subjects.collectAsState()
    val user by vm.user.collectAsState()
    val scope = rememberCoroutineScope()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy)) {
                Column(Modifier.padding(20.dp)) {
                    Text("أهلاً، ${user?.displayName ?: "بطلنا"} 👋", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("PT Study Zone — منصة العلاج الطبيعي", color = Gold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(dash.data?.motivation ?: "ابدأ أول محاضرة اليوم — خطوة صغيرة تصنع فرقاً كبيراً.", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ProgressRing(dash.data?.progress ?: 0f, label = "إنجازك العام")
            }
            if (dash.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            dash.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) { StatCard("محاضرة مكتملة", "${dash.data?.completedLectures ?: 0}/${dash.data?.totalLectures ?: 0}", Icons.Default.MenuBook) }
                Column(Modifier.weight(1f)) { StatCard("تحدٍ نشط", "${dash.data?.activeChallenges ?: 0}", Icons.Default.EmojiEvents) }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) { StatCard("موادي", "${dash.data?.mySubjects ?: 0}", Icons.Default.Folder) }
                Column(Modifier.weight(1f)) { StatCard("إشعارات غير مقروءة", "${dash.data?.unreadNotifications ?: 0}", Icons.Default.Notifications) }
            }
        }
        item { SectionTitle("موادي", Icons.Default.MenuBook) }
        if (subs.loading) item { CircularProgressIndicator(color = Gold) }
        subs.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        items(subs.data?.take(5) ?: emptyList()) { SubjectRow(it) { goSubject(it.subjectId) } }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = goSubjects, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = TealMed)) { Text("كل المواد") }
                Button(onClick = goChallenges, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("التحديات") }
                Button(onClick = goNotifications, modifier = Modifier.weight(1f)) { Text("الإشعارات") }
            }
            TextButton(onClick = { scope.launch { vm.loadDashboard(); vm.loadSubjects() } }) { Text("تحديث البيانات", color = TealMed) }
        }
    }
}

// ---------- المواد + بحث ----------
@Composable
fun SubjectsScreen(vm: AppViewModel, goSubject: (Int) -> Unit) {
    val subs by vm.subjects.collectAsState()
    var q by remember { mutableStateOf("") }
    var mineOnly by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newVis by remember { mutableStateOf("private") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("المواد الدراسية", Icons.Default.MenuBook)
        OutlinedTextField(value = q, onValueChange = { q = it; vm.loadSubjects(q) }, label = { Text("ابحث عن مادة...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = mineOnly, onCheckedChange = { mineOnly = it })
            Text("موادي فقط")
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { showCreate = true }) { Text("+ مادة جديدة", color = TealMed, fontWeight = FontWeight.Bold) }
        }
        if (showCreate) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    PTTextField(newName, { newName = it }, "اسم المادة", Icons.Default.MenuBook)
                    Spacer(Modifier.height(8.dp))
                    Row { listOf("private" to "خاصة", "registered" to "مسجلين", "public" to "عامة").forEach { (v, l) -> FilterChip(selected = newVis == v, onClick = { newVis = v }, label = { Text(l) }); Spacer(Modifier.width(6.dp)) } }
                    Spacer(Modifier.height(8.dp))
                    GoldButton("إنشاء المادة") { vm.doAction({ vm.repo.createSubject(newName.trim(), newVis, "") }) { newName = ""; showCreate = false; vm.loadSubjects() } }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (subs.loading) { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Gold) } }
        subs.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items((subs.data ?: emptyList()).filter { !mineOnly || it.isOwner }) { SubjectRow(it) { goSubject(it.subjectId) } }
        }
    }
}

// ---------- تفاصيل مادة ----------
@Composable
fun SubjectDetailScreen(vm: AppViewModel, subjectId: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var contents by remember { mutableStateOf<ContentsData?>(null) }
    var subject by remember { mutableStateOf<Subject?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(0) } // 0 محاضرات 1 سكاشن 2 كلينيكال
    var newContentName by remember { mutableStateOf("") }
    val currentUser by vm.user.collectAsState()
    fun reload() {
        scope.launch {
            loading = true; error = null
            try { subject = vm.repo.subject(subjectId); contents = vm.repo.contents(subjectId) }
            catch (e: Exception) { error = e.message }
            loading = false
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment ?: "file.pdf"
            scope.launch {
                try {
                    // الرفع يحتاج content_id: نرفع لأول عنصر في التبويب الحالي إن وُجد
                    val current = when (tab) { 0 -> contents?.lectures?.firstOrNull(); 1 -> contents?.sections?.firstOrNull(); else -> contents?.clinicals?.firstOrNull() }
                    if (current == null) { vm.showToast("أضف محتوى أولاً قبل رفع ملف"); return@launch }
                    val type = when (tab) { 0 -> "lecture"; 1 -> "section"; else -> "clinical" }
                    vm.showToast(vm.repo.uploadFile(ctx, uri, name, type, current.id))
                    reload()
                } catch (e: Exception) { vm.showToast(e.message ?: "فشل الرفع") }
            }
        }
    }
    LaunchedEffect(subjectId) { reload() }
    if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Gold) }; return }
    error?.let { Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) { Text(it, color = MaterialTheme.colorScheme.error) }; return }
    val s = subject ?: return
    val list = when (tab) { 0 -> contents?.lectures ?: emptyList(); 1 -> contents?.sections ?: emptyList(); else -> contents?.clinicals ?: emptyList() }
    val type = when (tab) { 0 -> "lecture"; 1 -> "section"; else -> "clinical" }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(s.subjectName, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        Text("${s.visibility} • ${s.stats?.completed ?: 0}/${s.stats?.total ?: 0} مكتمل • ${s.stats?.progress?.toInt() ?: 0}%", color = Color.Gray)
        LinearProgressIndicator(progress = { ((s.stats?.progress ?: 0f) / 100f) }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = Gold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.doAction({ vm.repo.subscribe(subjectId) }) { reload(); vm.loadSubjects() } }, colors = ButtonDefaults.buttonColors(containerColor = TealMed)) { Text("اشتراك") }
            Button(onClick = { vm.doAction({ vm.repo.unsubscribe(subjectId) }) { reload(); vm.loadSubjects() } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("إلغاء الاشتراك") }
            Button(onClick = { picker.launch("application/pdf") }) { Text("رفع PDF") }
        }
        TabRow(selectedTabIndex = tab) {
            listOf("محاضرات", "سكاشن", "كلينيكال").forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }) { Text(t, modifier = Modifier.padding(12.dp)) } }
        }
        if (s.isOwner || (currentUser?.isAdmin == true)) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = newContentName, onValueChange = { newContentName = it }, label = { Text("اسم محتوى جديد") }, modifier = Modifier.weight(1f), singleLine = true)
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.doAction({ vm.repo.addContent(subjectId, type, newContentName.trim()) }) { newContentName = ""; reload() } }) { Text("إضافة") }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(list) { item ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (item.isCompleted) TealMed else Gold)
                            Spacer(Modifier.width(8.dp))
                            Text(item.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { vm.doAction({ vm.repo.updateProgress(type, item.id, if (item.isCompleted) "pending" else "completed") }) { reload(); vm.loadDashboard() } }) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "تبديل الحالة")
                            }
                            IconButton(onClick = { vm.doAction({ vm.repo.updateProgress(type, item.id, "needs_review") }) { reload() } }) {
                                Icon(Icons.Default.Flag, contentDescription = "يحتاج مراجعة")
                            }
                        }
                        Text("الحالة: ${when (item.status) { "completed" -> "مكتملة ✅"; "needs_review" -> "تحتاج مراجعة ⚠️"; else -> "قيد الانتظار" }}", color = Color.Gray, fontSize = 13.sp)
                        item.files.forEach { f ->
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red)
                                Spacer(Modifier.width(8.dp))
                                Text("${f.name} ${f.sizeText}", modifier = Modifier.weight(1f), fontSize = 13.sp)
                                TextButton(onClick = {
                                    val url = f.downloadUrl
                                    val full = if (url.startsWith("http")) url else "https://ptmohsen.free.nf/$url"
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(full)))
                                }) { Text("فتح / تحميل", color = TealMed) }
                            }
                        }
                    }
                }
            }
            if (list.isEmpty()) item { Text("لا يوجد محتوى بعد في هذا القسم.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
        }
    }
}

// ---------- التحديات ----------
@Composable
fun ChallengesScreen(vm: AppViewModel, goChallenge: (Int) -> Unit) {
    val ch by vm.challenges.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var tl by remember { mutableStateOf("5") }
    var ts by remember { mutableStateOf("0") }
    var tc by remember { mutableStateOf("0") }
    var hours by remember { mutableStateOf("24") }
    var vis by remember { mutableStateOf("public") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("التحديات", Icons.Default.EmojiEvents)
        Button(onClick = { showCreate = !showCreate }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text(if (showCreate) "إغلاق" else "+ إنشاء تحدي جديد") }
        if (showCreate) {
            Card(Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PTTextField(name, { name = it }, "اسم التحدي", Icons.Default.EmojiEvents)
                    PTTextField(tl, { tl = it }, "هدف المحاضرات", Icons.Default.MenuBook, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    PTTextField(ts, { ts = it }, "هدف السكاشن", Icons.Default.Group, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    PTTextField(tc, { tc = it }, "هدف الكلينيكال", Icons.Default.LocalHospital, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    PTTextField(hours, { hours = it }, "المدة بالساعات", Icons.Default.Schedule, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    GoldButton("إنشاء التحدي") {
                        vm.doAction({ vm.repo.createChallenge(name.trim(), tl.toIntOrNull() ?: 0, ts.toIntOrNull() ?: 0, tc.toIntOrNull() ?: 0, (hours.toIntOrNull() ?: 24) * 60, vis, "") }) { name = ""; showCreate = false; vm.loadChallenges() }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (ch.loading) CircularProgressIndicator(color = Gold)
        ch.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ch.data ?: emptyList()) { c ->
                Card(Modifier.fillMaxWidth().clickable { goChallenge(c.challengeId) }, shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(c.challengeName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("الهدف: ${c.targetTotal} عنصر • المدة: ${c.durationText} • الحالة: ${c.status} • المشاركون: ${c.currentParticipants}", color = Color.Gray, fontSize = 13.sp)
                        Text("من: ${c.ownerName ?: ""} • أنت: ${c.myStatus ?: "غير مشارك"}", color = TealMed, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeDetailScreen(vm: AppViewModel, challengeId: Int) {
    val scope = rememberCoroutineScope()
    var ch by remember { mutableStateOf<Challenge?>(null) }
    var prog by remember { mutableStateOf<ChallengeProgressData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    fun reload() { scope.launch { loading = true; try { ch = vm.repo.challenge(challengeId); prog = vm.repo.challengeProgress(challengeId) } catch (e: Exception) { error = e.message }; loading = false } }
    LaunchedEffect(challengeId) { reload() }
    if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Gold) }; return }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)); return }
    val c = ch ?: return
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(c.challengeName, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        Text("الحالة: ${c.status} • ينتهي: ${c.endTime ?: "—"}", color = Color.Gray)
        ProgressRing(prog?.percent ?: 0f, label = "تقدمك في التحدي")
        Text("أكملت ${prog?.completedCount ?: 0} من ${prog?.targetTotal ?: c.targetTotal} عنصر", fontWeight = FontWeight.Bold)
        Text("المشاركون (${c.participants.size}): " + c.participants.joinToString(", ") { it.fullName ?: it.username ?: "مستخدم" }, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.doAction({ vm.repo.joinChallenge(challengeId) }) { reload(); vm.loadChallenges() } }, colors = ButtonDefaults.buttonColors(containerColor = TealMed)) { Text("انضمام") }
            Button(onClick = { vm.doAction({ vm.repo.leaveChallenge(challengeId) }) { reload() } }) { Text("مغادرة") }
            Button(onClick = { vm.doAction({ vm.repo.completeChallenge(challengeId) }) { reload() } }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("إكمال") }
            Button(onClick = { vm.doAction({ vm.repo.cancelChallenge(challengeId) }) { reload() } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("إلغاء") }
        }
    }
}
