package com.ptstudyzone.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptstudyzone.app.data.User
import com.ptstudyzone.app.ui.AppViewModel
import com.ptstudyzone.app.ui.theme.Gold
import com.ptstudyzone.app.ui.theme.TealMed
import kotlinx.coroutines.launch

// ---------- اشتراكاتي ----------
@Composable
fun SubscriptionsScreen(vm: AppViewModel) {
    val subs by vm.subscriptions.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("اشتراكاتي", Icons.Default.Subscriptions)
        if (subs.data.isNullOrEmpty() && !subs.loading) Text("لا توجد اشتراكات بعد — اشترك في مادة من تبويب المواد.", color = Color.Gray)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(subs.data ?: emptyList()) { s ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(s.subjectName ?: "${s.contentType} #${s.contentId}", fontWeight = FontWeight.Bold); Text(s.subscribedAt ?: "", color = Color.Gray, fontSize = 12.sp) }
                        Text(if (s.isActive == 1) "نشط ✅" else "ملغى", color = if (s.isActive == 1) TealMed else Color.Gray)
                    }
                }
            }
        }
    }
}

// ---------- الإشعارات ----------
@Composable
fun NotificationsScreen(vm: AppViewModel) {
    val notif by vm.notifications.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("الإشعارات", Icons.Default.Notifications)
        Button(onClick = { vm.doAction({ vm.repo.markAllNotificationsRead() }) { vm.loadNotifications(); vm.loadDashboard() } }, colors = ButtonDefaults.buttonColors(containerColor = Gold)) { Text("تعليم الكل كمقروء", color = com.ptstudyzone.app.ui.theme.Navy) }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notif.data?.notifications ?: emptyList()) { n ->
                Card(Modifier.fillMaxWidth().clickable { vm.doAction({ vm.repo.markNotificationRead(n.notificationId) }) { vm.loadNotifications() } }, shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(n.title, fontWeight = if (n.isRead == 0) FontWeight.ExtraBold else FontWeight.Bold)
                        n.message?.let { Text(it, color = Color.Gray, fontSize = 13.sp) }
                        Text(n.createdAt ?: "", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
            if ((notif.data?.notifications ?: emptyList()).isEmpty()) item { Text("لا توجد إشعارات.", color = Color.Gray) }
        }
    }
}

// ---------- الملف الشخصي ----------
@Composable
fun ProfileScreen(vm: AppViewModel) {
    val user by vm.user.collectAsState()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    val u = user
    if (u != null && !loaded) { name = u.fullName ?: ""; phone = u.phone ?: ""; loaded = true }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("ملفي الشخصي", Icons.Default.Person)
        if (u != null) {
            Text(u.displayName, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Text("${u.email} • ${u.username} • الدور: ${u.role}", color = Color.Gray)
            Text("زيارات: ${u.totalVisits} • آخر دخول: ${u.lastLogin ?: "—"}", color = Color.Gray, fontSize = 13.sp)
        }
        PTTextField(name, { name = it }, "الاسم الكامل", Icons.Default.Person)
        PTTextField(phone, { phone = it }, "رقم الهاتف", Icons.Default.Phone, keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
        GoldButton("حفظ التعديلات") { vm.doAction({ vm.repo.updateProfile(name.trim(), phone.trim()); "تم تحديث الملف الشخصي" }) { vm.refreshSession() } }
        Divider()
        Text("تغيير كلمة المرور", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        PTTextField(oldPass, { oldPass = it }, "كلمة المرور الحالية", Icons.Default.Lock, isPassword = true, keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
        PTTextField(newPass, { newPass = it }, "كلمة المرور الجديدة", Icons.Default.Lock, isPassword = true, keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
        GoldButton("تغيير كلمة المرور") { vm.doAction({ vm.repo.changePassword(oldPass, newPass) }) { oldPass = ""; newPass = "" } }
        Spacer(Modifier.weight(1f))
        Button(onClick = { vm.logout() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth()) { Text("تسجيل الخروج") }
    }
}

// ---------- لوحة الأدمن ----------
@Composable
fun AdminScreen(vm: AppViewModel) {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf<com.ptstudyzone.app.data.AdminStats?>(null) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var q by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    fun reload() { scope.launch { loading = true; try { stats = vm.repo.adminStats(); users = vm.repo.adminUsers(q) } catch (e: Exception) { error = e.message }; loading = false } }
    LaunchedEffect(Unit) { reload() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("لوحة تحكم الأدمن", Icons.Default.AdminPanelSettings)
        if (loading) { CircularProgressIndicator(color = Gold); return@Column }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        stats?.let { s ->
            Text("المستخدمون: ${s.totalUsers} • المواد: ${s.totalSubjects} • المحاضرات: ${s.totalLectures} • الملفات: ${s.totalFiles} • التحديات النشطة: ${s.activeChallenges} • المحظورون: ${s.bannedUsers}", fontSize = 14.sp)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = q, onValueChange = { q = it; reload() }, label = { Text("ابحث عن مستخدم...") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(users) { u ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(u.displayName, fontWeight = FontWeight.Bold)
                        Text("${u.email} • ${u.role} • ${if (u.banned) "محظور 🚫" else "نشط"}", color = Color.Gray, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            Button(onClick = { vm.doAction({ vm.repo.banUser(u.userId, "temporary", 24, "مخالفة قواعد المنصة") }) { reload() } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE85D6D))) { Text("حظر 24س", fontSize = 12.sp) }
                            Button(onClick = { vm.doAction({ vm.repo.unbanUser(u.userId) }) { reload() } }) { Text("إلغاء الحظر", fontSize = 12.sp) }
                            Button(onClick = { vm.doAction({ vm.repo.changeRole(u.userId, if (u.role == "user") "moderator" else "user") }) { reload() } }) { Text(if (u.role == "user") "مشرف" else "مستخدم", fontSize = 12.sp) }
                            Button(onClick = { vm.doAction({ vm.repo.deleteUser(u.userId) }) { reload() } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("حذف", fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}
