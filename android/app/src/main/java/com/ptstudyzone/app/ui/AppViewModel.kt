package com.ptstudyzone.app.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptstudyzone.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState<T>(val loading: Boolean = false, val data: T? = null, val error: String? = null, val message: String? = null)

class AppViewModel(context: Context) : ViewModel() {
    val repo = Repository(context.applicationContext)

    val user = MutableStateFlow<User?>(null)
    val dashboard = MutableStateFlow(UiState<DashboardData>(loading = true))
    val subjects = MutableStateFlow(UiState<List<Subject>>(loading = true))
    val challenges = MutableStateFlow(UiState<List<Challenge>>(loading = true))
    val notifications = MutableStateFlow(UiState<NotificationsData>(loading = true))
    val subscriptions = MutableStateFlow(UiState<List<Subscription>>(loading = true))
    val toast = MutableStateFlow<String?>(null)
    private val _loggedIn = MutableStateFlow<Boolean?>(null)
    val loggedIn: StateFlow<Boolean?> = _loggedIn.asStateFlow()

    init { viewModelScope.launch { repo.initToken(); refreshSession() } }

    fun showToast(m: String) { toast.value = m }
    fun clearToast() { toast.value = null }

    fun refreshSession() {
        viewModelScope.launch {
            try { user.value = repo.me(); _loggedIn.value = true; refreshAll() }
            catch (_: Exception) { _loggedIn.value = false }
        }
    }
    fun refreshAll() { loadDashboard(); loadSubjects(); loadChallenges(); loadNotifications(); loadSubscriptions() }

    fun loadDashboard() { viewModelScope.launch { dashboard.value = UiState(loading = true); try { dashboard.value = UiState(data = repo.dashboard()) } catch (e: Exception) { dashboard.value = UiState(error = e.message) } } }
    fun loadSubjects(q: String? = null) { viewModelScope.launch { subjects.value = UiState(loading = true); try { subjects.value = UiState(data = repo.subjects(q)) } catch (e: Exception) { subjects.value = UiState(error = e.message) } } }
    fun loadChallenges() { viewModelScope.launch { challenges.value = UiState(loading = true); try { challenges.value = UiState(data = repo.challenges()) } catch (e: Exception) { challenges.value = UiState(error = e.message) } } }
    fun loadNotifications() { viewModelScope.launch { try { notifications.value = UiState(data = repo.notifications()) } catch (e: Exception) { notifications.value = UiState(error = e.message) } } }
    fun loadSubscriptions() { viewModelScope.launch { try { subscriptions.value = UiState(data = repo.subscriptions()) } catch (e: Exception) { subscriptions.value = UiState(error = e.message) } } }

    fun login(identifier: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try { val d = repo.login(identifier, password); user.value = d.user; _loggedIn.value = true; showToast("أهلاً بك ${d.user?.displayName ?: ""}"); refreshAll(); onDone() }
            catch (e: Exception) { showToast(e.message ?: "فشل تسجيل الدخول") }
        }
    }
    fun register(req: RegisterRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            try { val d = repo.register(req); user.value = d.user; _loggedIn.value = true; showToast("تم إنشاء حسابك بنجاح، أهلاً بك!"); refreshAll(); onDone() }
            catch (e: Exception) { showToast(e.message ?: "فشل إنشاء الحساب") }
        }
    }
    fun logout() { viewModelScope.launch { repo.logout(); user.value = null; _loggedIn.value = false } }

    fun doAction(block: suspend () -> String, after: () -> Unit = {}) {
        viewModelScope.launch { try { showToast(block()); after() } catch (e: Exception) { showToast(e.message ?: "حدث خطأ") } }
    }
    suspend fun uploadFile(context: Context, uri: Uri, name: String, type: String, contentId: Int): String = repo.uploadFile(context, uri, name, type, contentId)
}
