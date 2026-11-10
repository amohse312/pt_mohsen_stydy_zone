package com.ptstudyzone.app.data

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login") suspend fun login(@Body body: LoginRequest): ApiResponse<AuthData>
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): ApiResponse<AuthData>
    @GET("auth/me") suspend fun me(): ApiResponse<AuthData>
    @POST("auth/logout") suspend fun logout(): ApiResponse<GenericData>
    @POST("auth/change-password") suspend fun changePassword(@Body body: Map<String, String>): ApiResponse<GenericData>

    @GET("dashboard/stats") suspend fun dashboardStats(): ApiResponse<DashboardData>

    @GET("subjects") suspend fun subjects(@Query("q") q: String? = null, @Query("mine") mine: String? = null): ApiResponse<SubjectsData>
    @GET("subjects/{id}") suspend fun subject(@Path("id") id: Int): ApiResponse<SubjectData>
    @POST("subjects") suspend fun createSubject(@Body body: Map<String, String>): ApiResponse<GenericData>
    @PUT("subjects/{id}") suspend fun updateSubject(@Path("id") id: Int, @Body body: Map<String, String>): ApiResponse<GenericData>
    @DELETE("subjects/{id}") suspend fun deleteSubject(@Path("id") id: Int): ApiResponse<GenericData>
    @POST("subjects/{id}/subscribe") suspend fun subscribe(@Path("id") id: Int): ApiResponse<GenericData>
    @POST("subjects/{id}/unsubscribe") suspend fun unsubscribe(@Path("id") id: Int): ApiResponse<GenericData>
    @GET("subjects/{id}/contents") suspend fun contents(@Path("id") id: Int): ApiResponse<ContentsData>
    @POST("subjects/{id}/contents") suspend fun addContent(@Path("id") id: Int, @Body body: Map<String, String>): ApiResponse<GenericData>

    @PUT("contents/{type}/{id}") suspend fun renameContent(@Path("type") type: String, @Path("id") id: Int, @Body body: Map<String, String>): ApiResponse<GenericData>
    @DELETE("contents/{type}/{id}") suspend fun deleteContent(@Path("type") type: String, @Path("id") id: Int): ApiResponse<GenericData>
    @POST("contents/{type}/{id}/progress") suspend fun updateProgress(@Path("type") type: String, @Path("id") id: Int, @Body body: Map<String, String>): ApiResponse<GenericData>

    @GET("files") suspend fun files(@Query("content_type") type: String? = null, @Query("content_id") contentId: Int? = null): ApiResponse<FilesData>
    @GET("files/{id}/download-url") suspend fun fileDownloadUrl(@Path("id") id: Int): ApiResponse<GenericData>
    @Multipart @POST("files/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part, @Part("content_type") type: RequestBody, @Part("content_id") contentId: RequestBody): ApiResponse<GenericData>
    @DELETE("files/{id}") suspend fun deleteFile(@Path("id") id: Int): ApiResponse<GenericData>

    @GET("challenges") suspend fun challenges(@Query("scope") scope: String? = null): ApiResponse<ChallengesData>
    @GET("challenges/{id}") suspend fun challenge(@Path("id") id: Int): ApiResponse<ChallengeData>
    @POST("challenges") suspend fun createChallenge(@Body body: Map<String, String>): ApiResponse<GenericData>
    @DELETE("challenges/{id}") suspend fun deleteChallenge(@Path("id") id: Int): ApiResponse<GenericData>
    @POST("challenges/{id}/join") suspend fun joinChallenge(@Path("id") id: Int): ApiResponse<GenericData>
    @POST("challenges/{id}/leave") suspend fun leaveChallenge(@Path("id") id: Int): ApiResponse<GenericData>
    @POST("challenges/{id}/complete") suspend fun completeChallenge(@Path("id") id: Int): ApiResponse<GenericData>
    @POST("challenges/{id}/cancel") suspend fun cancelChallenge(@Path("id") id: Int): ApiResponse<GenericData>
    @GET("challenges/{id}/progress") suspend fun challengeProgress(@Path("id") id: Int): ApiResponse<ChallengeProgressData>
    @GET("challenges/{id}/records") suspend fun challengeRecords(@Path("id") id: Int): ApiResponse<Map<String, Any>>
    @GET("challenges/{id}/report") suspend fun challengeReport(@Path("id") id: Int): ApiResponse<Map<String, Any>>

    @GET("subscriptions") suspend fun subscriptions(): ApiResponse<SubscriptionsData>
    @GET("notifications") suspend fun notifications(): ApiResponse<NotificationsData>
    @POST("notifications/{id}/read") suspend fun markNotificationRead(@Path("id") id: Int): ApiResponse<GenericData>
    @POST("notifications/read-all") suspend fun markAllNotificationsRead(): ApiResponse<GenericData>

    @GET("profile") suspend fun profile(): ApiResponse<AuthData>
    @PUT("profile") suspend fun updateProfile(@Body body: Map<String, String>): ApiResponse<AuthData>

    @GET("admin/stats") suspend fun adminStats(): ApiResponse<AdminStats>
    @GET("admin/users") suspend fun adminUsers(@Query("q") q: String? = null): ApiResponse<UsersData>
    @POST("admin/users/{id}/ban") suspend fun banUser(@Path("id") id: Int, @Body body: Map<String, String>): ApiResponse<GenericData>
    @POST("admin/users/{id}/unban") suspend fun unbanUser(@Path("id") id: Int, @Body body: Map<String, String> = emptyMap()): ApiResponse<GenericData>
    @POST("admin/users/{id}/role") suspend fun changeRole(@Path("id") id: Int, @Body body: Map<String, String>): ApiResponse<GenericData>
    @POST("admin/users/{id}/toggle-active") suspend fun toggleActive(@Path("id") id: Int, @Body body: Map<String, String>): ApiResponse<GenericData>
    @POST("admin/users/{id}/change-password") suspend fun adminChangePassword(@Path("id") id: Int, @Body body: Map<String, String>): ApiResponse<GenericData>
    @DELETE("admin/users/{id}") suspend fun deleteUser(@Path("id") id: Int): ApiResponse<GenericData>
    @POST("admin/assign") suspend fun assign(@Body body: Map<String, String>): ApiResponse<GenericData>
    @GET("admin/activity") suspend fun adminActivity(): ApiResponse<Map<String, Any>>

    companion object {
        fun create(tokenProvider: () -> String?): ApiService {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val token = tokenProvider()
                    val req = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .apply { if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token") }
                        .build()
                    chain.proceed(req)
                }
                .build()
            return Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
