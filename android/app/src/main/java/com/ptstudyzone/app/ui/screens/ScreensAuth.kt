package com.ptstudyzone.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptstudyzone.app.data.RegisterRequest
import com.ptstudyzone.app.ui.AppViewModel
import com.ptstudyzone.app.ui.theme.Gold
import com.ptstudyzone.app.ui.theme.Navy
import com.ptstudyzone.app.ui.theme.TealMed

// ---------- مكوّنات مشتركة ----------
@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
        shape = RoundedCornerShape(16.dp)) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
fun PTTextField(value: String, onChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isPassword: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TealMed) },
        trailingIcon = { if (isPassword) IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null) } },
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
}

@Composable
fun ProgressRing(progress: Float, size: Int = 120, label: String = "") {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(Color(0xFFE3EAF2), -90f, 360f, false, style = Stroke(14f, cap = StrokeCap.Round))
            drawArc(Brush.linearGradient(listOf(TealMed, Gold)), -90f, 360f * (progress / 100f).coerceIn(0f, 1f), false, style = Stroke(14f, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${progress.toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Navy)
            if (label.isNotBlank()) Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LogoBadge(size: Int = 96) {
    Box(modifier = Modifier.size(size.dp).clip(CircleShape)
        .background(Brush.linearGradient(listOf(Navy, TealMed))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Gold, modifier = Modifier.size((size * 0.45).dp))
            Text("PT", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = (size * 0.2).sp)
        }
    }
}

@Composable
fun SectionTitle(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, contentDescription = null, tint = Gold)
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

// ---------- Splash ترحيبية فاخرة ----------
@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(2200); onDone() }
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy, Color(0xFF0E3A5C), TealMed))),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            LogoBadge(120)
            Spacer(Modifier.height(24.dp))
            Text("PT Study Zone", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, textAlign = TextAlign.Center)
            Text("منصة العلاج الطبيعي", color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))
            Text("تعلّم • تقدّم • تحدَّ نفسك", color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp)
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(color = Gold)
        }
    }
}

// ---------- Onboarding ----------
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        Triple("موادك كلها في مكان واحد", "9 مواد علاج طبيعي: نيورولوجي، أطفال، أشعة، جينات، والمزيد — محاضرات وسكاشن وكلينيكال مع ملفات PDF.", Icons.Default.MenuBook),
        Triple("تتبّع تقدّمك لحظة بلحظة", "علّم كل محاضرة كمكتملة أو تحتاج مراجعة، وشاهد نسبة إنجازك في كل مادة برسوم واضحة.", Icons.Default.TrendingUp),
        Triple("تحدَّ نفسك وزملاءك", "أنشئ تحديات بوقت وأهداف، انضم لتحديات الآخرين، وتابع سجلك وتقاريرك.", Icons.Default.EmojiEvents)
    )
    var page by remember { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        LogoBadge(80)
        Spacer(Modifier.height(28.dp))
        Icon(pages[page].third, contentDescription = null, tint = Gold, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(pages[page].first, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(pages[page].second, textAlign = TextAlign.Center, color = Color.Gray, fontSize = 15.sp, lineHeight = 24.sp)
        Spacer(Modifier.height(32.dp))
        Row { repeat(3) { i -> Box(Modifier.size(10.dp).clip(CircleShape).background(if (i == page) Gold else Color.LightGray)); if (i < 2) Spacer(Modifier.width(8.dp)) } }
        Spacer(Modifier.height(32.dp))
        GoldButton(if (page < 2) "التالي" else "ابدأ الآن") { if (page < 2) page++ else onFinish() }
        if (page < 2) TextButton(onClick = onFinish) { Text("تخطَّ", color = TealMed) }
    }
}

// ---------- تسجيل الدخول ----------
@Composable
fun LoginScreen(vm: AppViewModel, onLogin: () -> Unit, onRegister: () -> Unit) {
    var id by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        LogoBadge()
        Spacer(Modifier.height(16.dp))
        Text("أهلاً بعودتك", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
        Text("سجّل دخولك لمتابعة دراستك", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        PTTextField(id, { id = it }, "البريد الإلكتروني أو اسم المستخدم", Icons.Default.Person, keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        PTTextField(pass, { pass = it }, "كلمة المرور", Icons.Default.Lock, isPassword = true, keyboardType = KeyboardType.Password)
        Spacer(Modifier.height(20.dp))
        GoldButton("تسجيل الدخول", enabled = !busy && id.isNotBlank() && pass.isNotBlank()) {
            busy = true; vm.login(id.trim(), pass) { busy = false; onLogin() }
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRegister) { Text("ليس لديك حساب؟ أنشئ حساباً جديداً", color = TealMed, fontWeight = FontWeight.Bold) }
        Text("بالدخول أنت توافق على شروط الاستخدام وسياسة الخصوصية", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

// ---------- إنشاء حساب ----------
@Composable
fun RegisterScreen(vm: AppViewModel, onDone: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("إنشاء حساب جديد", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
        Text("انضم إلى PT Study Zone وابدأ رحلتك", color = Color.Gray)
        Spacer(Modifier.height(20.dp))
        PTTextField(name, { name = it }, "الاسم الكامل", Icons.Default.Person)
        Spacer(Modifier.height(10.dp))
        PTTextField(username, { username = it }, "اسم المستخدم", Icons.Default.AccountCircle)
        Spacer(Modifier.height(10.dp))
        PTTextField(email, { email = it }, "البريد الإلكتروني", Icons.Default.Email, keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        PTTextField(phone, { phone = it }, "رقم الهاتف (اختياري)", Icons.Default.Phone, keyboardType = KeyboardType.Phone)
        Spacer(Modifier.height(10.dp))
        PTTextField(pass, { pass = it }, "كلمة المرور (8 أحرف على الأقل + رقم)", Icons.Default.Lock, isPassword = true, keyboardType = KeyboardType.Password)
        Spacer(Modifier.height(10.dp))
        PTTextField(confirm, { confirm = it }, "تأكيد كلمة المرور", Icons.Default.Lock, isPassword = true, keyboardType = KeyboardType.Password)
        Spacer(Modifier.height(18.dp))
        GoldButton("إنشاء الحساب", enabled = !busy && name.isNotBlank() && username.isNotBlank() && email.isNotBlank() && pass.length >= 8 && pass == confirm) {
            busy = true
            vm.register(RegisterRequest(username.trim(), email.trim(), name.trim(), phone.trim(), pass, confirm)) { busy = false; onDone() }
        }
        TextButton(onClick = onBack) { Text("لدي حساب بالفعل — تسجيل الدخول", color = TealMed) }
    }
}
