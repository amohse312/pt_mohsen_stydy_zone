package com.ptstudyzone.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pt_session")

/** حفظ التوكن وبيانات الجلسة محلياً (DataStore — لا يُحفظ أي شيء في كود التطبيق). */
class SessionStore(private val context: Context) {
    companion object {
        private val TOKEN = stringPreferencesKey("token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val ONBOARDED = stringPreferencesKey("onboarded")
    }
    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN] }
    val onboardedFlow: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDED] == "1" }

    suspend fun saveSession(token: String, user: User?) {
        context.dataStore.edit {
            it[TOKEN] = token
            if (user != null) {
                it[USER_ID] = user.userId.toString()
                it[USER_NAME] = user.displayName
                it[USER_ROLE] = user.role
            }
        }
    }
    suspend fun clearSession() { context.dataStore.edit { it.remove(TOKEN); it.remove(USER_ID); it.remove(USER_NAME); it.remove(USER_ROLE) } }
    suspend fun setOnboarded() { context.dataStore.edit { it[ONBOARDED] = "1" } }
    suspend fun currentToken(): String? = tokenFlow.first()
}
