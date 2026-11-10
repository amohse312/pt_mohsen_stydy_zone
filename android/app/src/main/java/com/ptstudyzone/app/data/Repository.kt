package com.ptstudyzone.app.data

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** طبقة الوصول للبيانات — كل العمليات تمر عبر ApiService فقط. */
class Repository(context: Context) {
    val session = SessionStore(context)
    @Volatile private var tokenCache: String? = null
    val api: ApiService = ApiService.create { tokenCache }

    suspend fun initToken() { tokenCache = session.currentToken() }

    private fun <T> unwrap(res: ApiResponse<T>): T {
        if (!res.success) throw IllegalStateException(res.message ?: "حدث خطأ غير متوقع")
        return res.data ?: throw IllegalStateException(res.message ?: "لا توجد بيانات")
    }
    private fun check(res: ApiResponse<*>): String {
        if (!res.success) throw IllegalStateException(res.message ?: "حدث خطأ غير متوقع")
        return res.message ?: "تم بنجاح"
    }

    suspend fun login(identifier: String, password: String): AuthData {
        val d = unwrap(api.login(LoginRequest(identifier, password)))
        d.token?.let { tokenCache = it; session.saveSession(it, d.user) }
        return d
    }
    suspend fun register(req: RegisterRequest): AuthData {
        val d = unwrap(api.register(req))
        d.token?.let { tokenCache = it; session.saveSession(it, d.user) }
        return d
    }
    suspend fun logout() { try { api.logout() } catch (_: Exception) {} finally { tokenCache = null; session.clearSession() } }
    suspend fun me(): User = unwrap(api.me()).user ?: throw IllegalStateException("لا يوجد مستخدم")
    suspend fun dashboard(): DashboardData = unwrap(api.dashboardStats())
    suspend fun subjects(q: String? = null, mine: Boolean = false): List<Subject> =
        unwrap(api.subjects(q?.takeIf { it.isNotBlank() }, if (mine) "1" else null)).subjects
    suspend fun subject(id: Int): Subject = unwrap(api.subject(id)).subject ?: throw IllegalStateException("المادة غير موجودة")
    suspend fun contents(id: Int): ContentsData = unwrap(api.contents(id))
    suspend fun createSubject(name: String, visibility: String, description: String): String = check(api.createSubject(mapOf("subject_name" to name, "visibility" to visibility, "description" to description)))
    suspend fun updateSubject(id: Int, name: String, visibility: String, description: String): String = check(api.updateSubject(id, mapOf("subject_name" to name, "visibility" to visibility, "description" to description)))
    suspend fun deleteSubject(id: Int): String = check(api.deleteSubject(id))
    suspend fun subscribe(id: Int): String = check(api.subscribe(id))
    suspend fun unsubscribe(id: Int): String = check(api.unsubscribe(id))
    suspend fun addContent(subjectId: Int, type: String, name: String): String = check(api.addContent(subjectId, mapOf("content_type" to type, "content_name" to name)))
    suspend fun renameContent(type: String, id: Int, name: String): String = check(api.renameContent(type, id, mapOf("name" to name)))
    suspend fun deleteContent(type: String, id: Int): String = check(api.deleteContent(type, id))
    suspend fun updateProgress(type: String, id: Int, status: String): String = check(api.updateProgress(type, id, mapOf("status" to status)))
    suspend fun uploadFile(context: Context, uri: Uri, fileName: String, type: String, contentId: Int): String {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: throw IllegalStateException("تعذر قراءة الملف")
        val part = MultipartBody.Part.createFormData("file", fileName, bytes.toRequestBody("application/pdf".toMediaTypeOrNull()))
        return check(api.uploadFile(part, type.toRequestBody("text/plain".toMediaTypeOrNull()), contentId.toString().toRequestBody("text/plain".toMediaTypeOrNull())))
    }
    suspend fun deleteFile(id: Int): String = check(api.deleteFile(id))
    suspend fun challenges(scope: String? = null): List<Challenge> = unwrap(api.challenges(scope)).challenges
    suspend fun challenge(id: Int): Challenge = unwrap(api.challenge(id)).challenge ?: throw IllegalStateException("التحدي غير موجود")
    suspend fun createChallenge(name: String, tl: Int, ts: Int, tc: Int, minutes: Int, visibility: String, description: String): String =
        check(api.createChallenge(mapOf("challenge_name" to name, "target_lectures" to tl.toString(), "target_sections" to ts.toString(), "target_clinicals" to tc.toString(), "duration_minutes" to minutes.toString(), "visibility" to visibility, "description" to description)))
    suspend fun joinChallenge(id: Int): String = check(api.joinChallenge(id))
    suspend fun leaveChallenge(id: Int): String = check(api.leaveChallenge(id))
    suspend fun completeChallenge(id: Int): String = check(api.completeChallenge(id))
    suspend fun cancelChallenge(id: Int): String = check(api.cancelChallenge(id))
    suspend fun deleteChallenge(id: Int): String = check(api.deleteChallenge(id))
    suspend fun challengeProgress(id: Int): ChallengeProgressData = unwrap(api.challengeProgress(id))
    suspend fun subscriptions(): List<Subscription> = unwrap(api.subscriptions()).subscriptions
    suspend fun notifications(): NotificationsData = unwrap(api.notifications())
    suspend fun markNotificationRead(id: Int): String = check(api.markNotificationRead(id))
    suspend fun markAllNotificationsRead(): String = check(api.markAllNotificationsRead())
    suspend fun updateProfile(fullName: String, phone: String): User = unwrap(api.updateProfile(mapOf("full_name" to fullName, "phone" to phone))).user ?: throw IllegalStateException("فشل التحديث")
    suspend fun changePassword(old: String, new: String): String = check(api.changePassword(mapOf("old_password" to old, "new_password" to new)))
    suspend fun adminStats(): AdminStats = unwrap(api.adminStats())
    suspend fun adminUsers(q: String? = null): List<User> = unwrap(api.adminUsers(q?.takeIf { it.isNotBlank() })).users
    suspend fun banUser(id: Int, type: String, hours: Int, reason: String): String = check(api.banUser(id, mapOf("ban_type" to type, "hours" to hours.toString(), "reason" to reason)))
    suspend fun unbanUser(id: Int): String = check(api.unbanUser(id))
    suspend fun changeRole(id: Int, role: String): String = check(api.changeRole(id, mapOf("role" to role)))
    suspend fun toggleActive(id: Int, state: Int): String = check(api.toggleActive(id, mapOf("state" to state.toString())))
    suspend fun deleteUser(id: Int): String = check(api.deleteUser(id))
    suspend fun assign(type: String, contentId: Int, userId: Int): String = check(api.assign(mapOf("content_type" to type, "content_id" to contentId.toString(), "user_id" to userId.toString())))
}
