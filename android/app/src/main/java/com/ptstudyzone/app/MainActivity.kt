package com.ptstudyzone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ptstudyzone.app.ui.AppViewModel
import com.ptstudyzone.app.ui.screens.*
import com.ptstudyzone.app.ui.theme.PTStudyZoneTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(applicationContext) as T
        }
        val vm = ViewModelProvider(this, factory)[AppViewModel::class.java]
        setContent { PTStudyZoneTheme { PTApp(vm) } }
    }
}

@Composable
fun PTApp(vm: AppViewModel) {
    val nav = rememberNavController()
    val loggedIn by vm.loggedIn.collectAsState()
    val toast by vm.toast.collectAsState()
    val user by vm.user.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSplash by remember { mutableStateOf(true) }
    var onboarded by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        onboarded = try { vm.repo.session.onboardedFlow.first() } catch (_: Exception) { false }
    }
    LaunchedEffect(toast) {
        toast?.let { snackbarHostState.showSnackbar(it); vm.clearToast() }
    }

    if (showSplash) { SplashScreen { showSplash = false }; return }

    // ننتظر معرفة حالة الجلسة والـ Onboarding قبل اختيار شاشة البداية
    if (loggedIn == null || onboarded == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val start = when {
        loggedIn == true -> "home"
        onboarded == false -> "onboarding"
        else -> "login"
    }

    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: ""
    val showBottom = loggedIn == true && route in listOf("home", "subjects", "challenges", "subscriptions", "notifications", "profile", "admin")

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    val items = listOf(
                        Triple("home", "الرئيسية", Icons.Default.Home),
                        Triple("subjects", "المواد", Icons.Default.MenuBook),
                        Triple("challenges", "التحديات", Icons.Default.EmojiEvents),
                        Triple("subscriptions", "اشتراكاتي", Icons.Default.Subscriptions),
                        Triple("notifications", "الإشعارات", Icons.Default.Notifications),
                        Triple("profile", "ملفي", Icons.Default.Person)
                    ) + if (user?.isAdmin == true) listOf(Triple("admin", "الأدمن", Icons.Default.AdminPanelSettings)) else emptyList()
                    items.forEach { (r, label, icon) ->
                        NavigationBarItem(selected = route == r, onClick = { nav.navigate(r) { popUpTo("home") { saveState = true }; launchSingleTop = true } },
                            icon = { Icon(icon, contentDescription = label) }, label = { Text(label) })
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        NavHost(navController = nav, startDestination = start, modifier = Modifier.padding(pad)) {
            composable("onboarding") {
                OnboardingScreen {
                    scope.launch { vm.repo.session.setOnboarded(); onboarded = true }
                    nav.navigate("login") { popUpTo("onboarding") { inclusive = true } }
                }
            }
            composable("login") { LoginScreen(vm, onLogin = { nav.navigate("home") { popUpTo("login") { inclusive = true } } }, onRegister = { nav.navigate("register") }) }
            composable("register") { RegisterScreen(vm, onDone = { nav.navigate("home") { popUpTo("register") { inclusive = true } } }, onBack = { nav.popBackStack() }) }
            composable("home") { HomeScreen(vm, goSubjects = { nav.navigate("subjects") }, goChallenges = { nav.navigate("challenges") }, goSubject = { nav.navigate("subject/$it") }, goNotifications = { nav.navigate("notifications") }) }
            composable("subjects") { SubjectsScreen(vm) { nav.navigate("subject/$it") } }
            composable("subject/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { e -> SubjectDetailScreen(vm, e.arguments?.getInt("id") ?: 0) }
            composable("challenges") { ChallengesScreen(vm) { nav.navigate("challenge/$it") } }
            composable("challenge/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { e -> ChallengeDetailScreen(vm, e.arguments?.getInt("id") ?: 0) }
            composable("subscriptions") { SubscriptionsScreen(vm) }
            composable("notifications") { NotificationsScreen(vm) }
            composable("profile") { ProfileScreen(vm) }
            composable("admin") { AdminScreen(vm) }
        }
    }
}
