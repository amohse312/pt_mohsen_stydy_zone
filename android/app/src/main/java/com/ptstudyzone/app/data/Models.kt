package com.ptstudyzone.app.data

import com.google.gson.annotations.SerializedName

// ---------- الرد الموحّد من الـ API ----------
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

// ---------- المستخدم ----------
data class User(
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("username") val username: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("role") val role: String = "user",
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("is_active") val isActive: Int = 1,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("last_login") val lastLogin: String? = null,
    @SerializedName("total_visits") val totalVisits: Int = 0,
    @SerializedName("banned") val banned: Boolean = false,
    @SerializedName("ban_reason") val banReason: String? = null
) {
    val displayName: String get() = fullName?.takeIf { it.isNotBlank() } ?: username ?: email ?: "مستخدم"
    val isAdmin: Boolean get() = role == "admin" || role == "moderator"
    val isSuperAdmin: Boolean get() = role == "admin"
}

data class AuthData(
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: User? = null
)

data class LoginRequest(val identifier: String, val password: String)
data class RegisterRequest(
    val username: String, val email: String, val full_name: String,
    val phone: String = "", val password: String, val confirm_password: String
)

// ---------- المواد والمحتوى ----------
data class SubjectStats(
    @SerializedName("total_lectures") val totalLectures: Int = 0,
    @SerializedName("completed_lectures") val completedLectures: Int = 0,
    @SerializedName("total_sections") val totalSections: Int = 0,
    @SerializedName("completed_sections") val completedSections: Int = 0,
    @SerializedName("total_clinicals") val totalClinicals: Int = 0,
    @SerializedName("completed_clinicals") val completedClinicals: Int = 0,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("completed") val completed: Int = 0,
    @SerializedName("progress") val progress: Float = 0f
)

data class SubjectFile(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("path") val path: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("size") val size: Int = 0,
    @SerializedName("type") val type: String? = null,
    @SerializedName("uploaded_at") val uploadedAt: String? = null
) {
    val downloadUrl: String get() = url ?: path ?: ""
    val sizeText: String get() = when {
        size <= 0 -> ""
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "%.1f KB".format(size / 1024f)
        else -> "%.1f MB".format(size / 1024f / 1024f)
    }
    val isPdf: Boolean get() = name.endsWith(".pdf", true) || (type ?: "").contains("pdf")
}

data class ContentItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("status") val status: String = "pending",
    @SerializedName("owner_status") val ownerStatus: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("files") val files: List<SubjectFile> = emptyList()
) {
    val isCompleted: Boolean get() = status == "completed"
    val needsReview: Boolean get() = status == "needs_review"
}

data class Subject(
    @SerializedName("subject_id") val subjectId: Int = 0,
    @SerializedName("subject_name") val subjectName: String = "",
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("visibility") val visibility: String = "private",
    @SerializedName("description") val description: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("owner_name") val ownerName: String? = null,
    @SerializedName("stats") val stats: SubjectStats? = null,
    @SerializedName("is_subscribed") val isSubscribed: Boolean = false,
    @SerializedName("is_owner") val isOwner: Boolean = false
)

data class SubjectsData(@SerializedName("subjects") val subjects: List<Subject> = emptyList(), @SerializedName("count") val count: Int = 0)
data class SubjectData(@SerializedName("subject") val subject: Subject? = null)
data class ContentsData(
    @SerializedName("subject_id") val subjectId: Int = 0,
    @SerializedName("lectures") val lectures: List<ContentItem> = emptyList(),
    @SerializedName("sections") val sections: List<ContentItem> = emptyList(),
    @SerializedName("clinicals") val clinicals: List<ContentItem> = emptyList(),
    @SerializedName("stats") val stats: SubjectStats? = null
)

// ---------- لوحة التحكم ----------
data class DashboardData(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("completed") val completed: Int = 0,
    @SerializedName("pending") val pending: Int = 0,
    @SerializedName("progress") val progress: Float = 0f,
    @SerializedName("total_lectures") val totalLectures: Int = 0,
    @SerializedName("completed_lectures") val completedLectures: Int = 0,
    @SerializedName("total_sections") val totalSections: Int = 0,
    @SerializedName("completed_sections") val completedSections: Int = 0,
    @SerializedName("total_clinicals") val totalClinicals: Int = 0,
    @SerializedName("completed_clinicals") val completedClinicals: Int = 0,
    @SerializedName("unread_notifications") val unreadNotifications: Int = 0,
    @SerializedName("my_subjects") val mySubjects: Int = 0,
    @SerializedName("active_challenges") val activeChallenges: Int = 0,
    @SerializedName("motivation") val motivation: String? = null
)

// ---------- التحديات ----------
data class ChallengeParticipant(
    @SerializedName("participant_id") val participantId: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("username") val username: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("status") val status: String = "active",
    @SerializedName("joined_at") val joinedAt: String? = null,
    @SerializedName("progress_percent") val progressPercent: Float = 0f
)

data class Challenge(
    @SerializedName("challenge_id") val challengeId: Int = 0,
    @SerializedName("challenge_name") val challengeName: String = "",
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("target_lectures") val targetLectures: Int = 0,
    @SerializedName("target_sections") val targetSections: Int = 0,
    @SerializedName("target_clinicals") val targetClinicals: Int = 0,
    @SerializedName("duration_hours") val durationHours: Int = 0,
    @SerializedName("duration_minutes") val durationMinutes: Int = 0,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("status") val status: String = "active",
    @SerializedName("visibility") val visibility: String = "private",
    @SerializedName("description") val description: String? = null,
    @SerializedName("max_participants") val maxParticipants: Int = 0,
    @SerializedName("current_participants") val currentParticipants: Int = 0,
    @SerializedName("owner_name") val ownerName: String? = null,
    @SerializedName("my_status") val myStatus: String? = null,
    @SerializedName("participants") val participants: List<ChallengeParticipant> = emptyList()
) {
    val targetTotal: Int get() = targetLectures + targetSections + targetClinicals
    val durationText: String get() = if (durationHours > 0) "$durationHours ساعة" else "$durationMinutes دقيقة"
}

data class ChallengesData(@SerializedName("challenges") val challenges: List<Challenge> = emptyList(), @SerializedName("count") val count: Int = 0)
data class ChallengeData(@SerializedName("challenge") val challenge: Challenge? = null)
data class ChallengeProgressData(
    @SerializedName("progress") val progress: List<Map<String, @JvmSuppressWildcards Any>> = emptyList(),
    @SerializedName("completed_count") val completedCount: Int = 0,
    @SerializedName("target_total") val targetTotal: Int = 0,
    @SerializedName("percent") val percent: Float = 0f
)

// ---------- اشتراكات وإشعارات ----------
data class Subscription(
    @SerializedName("subscription_id") val subscriptionId: Int = 0,
    @SerializedName("content_type") val contentType: String = "subject",
    @SerializedName("content_id") val contentId: Int = 0,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("is_active") val isActive: Int = 1,
    @SerializedName("subscribed_at") val subscribedAt: String? = null
)
data class SubscriptionsData(@SerializedName("subscriptions") val subscriptions: List<Subscription> = emptyList(), @SerializedName("count") val count: Int = 0)

data class AppNotification(
    @SerializedName("notification_id") val notificationId: Int = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("message") val message: String? = null,
    @SerializedName("type") val type: String = "info",
    @SerializedName("link") val link: String? = null,
    @SerializedName("is_read") val isRead: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)
data class NotificationsData(
    @SerializedName("notifications") val notifications: List<AppNotification> = emptyList(),
    @SerializedName("unread") val unread: Int = 0,
    @SerializedName("count") val count: Int = 0
)

// ---------- أدمن ----------
data class AdminStats(
    @SerializedName("total_users") val totalUsers: Int = 0,
    @SerializedName("total_subjects") val totalSubjects: Int = 0,
    @SerializedName("total_lectures") val totalLectures: Int = 0,
    @SerializedName("total_sections") val totalSections: Int = 0,
    @SerializedName("total_clinicals") val totalClinicals: Int = 0,
    @SerializedName("total_files") val totalFiles: Int = 0,
    @SerializedName("total_challenges") val totalChallenges: Int = 0,
    @SerializedName("active_challenges") val activeChallenges: Int = 0,
    @SerializedName("banned_users") val bannedUsers: Int = 0
)
data class UsersData(@SerializedName("users") val users: List<User> = emptyList(), @SerializedName("count") val count: Int = 0)
data class FilesData(@SerializedName("files") val files: List<SubjectFile> = emptyList(), @SerializedName("count") val count: Int = 0)
data class GenericData(@SerializedName("subject_id") val subjectId: Int? = null, @SerializedName("challenge_id") val challengeId: Int? = null, @SerializedName("content_id") val contentId: Int? = null, @SerializedName("file_id") val fileId: Int? = null, @SerializedName("status") val status: String? = null, @SerializedName("url") val url: String? = null, @SerializedName("file_name") val fileName: String? = null, @SerializedName("file_size") val fileSize: Int? = null)
