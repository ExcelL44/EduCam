package com.excell44.educam.data.local

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth_secured_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // ========== User Session ==========
    
    fun saveUserId(id: String) {
        prefs.edit { putString("user_id", id) }
    }
    
    fun getUserId(): String? = prefs.getString("user_id", null)
    
    fun clearUserId() {
        prefs.edit { remove("user_id") }
    }
    
    // ========== Offline Credentials (for offline re-login) ==========
    
    /**
     * Save user credentials for offline re-login.
     * This allows the app to validate login even without network.
     */
    fun saveOfflineCredentials(pseudo: String, passwordHash: String) {
        prefs.edit { 
            putString("offline_pseudo", pseudo)
            putString("offline_hash", passwordHash)
        }
    }
    
    fun getOfflineCredentials(): Pair<String, String>? {
        val pseudo = prefs.getString("offline_pseudo", null)
        val hash = prefs.getString("offline_hash", null)
        return if (pseudo != null && hash != null) {
            Pair(pseudo, hash)
        } else {
            null
        }
    }
    
    fun clearOfflineCredentials() {
        prefs.edit { 
            remove("offline_pseudo")
            remove("offline_hash")
        }
    }
    
    // ========== Auth State ==========
    
    enum class AuthMode {
        OFFLINE,  // User logged in offline (no API token)
        ONLINE    // User logged in online (with API token)
    }
    
    fun saveAuthMode(mode: AuthMode) {
        prefs.edit { putString("auth_mode", mode.name) }
    }
    
    fun getAuthMode(): AuthMode? {
        val modeName = prefs.getString("auth_mode", null)
        return modeName?.let { AuthMode.valueOf(it) }
    }
    
    // ========== Complete Logout ==========
    
    fun clearAllAuthData() {
        prefs.edit { 
            remove("user_id")
            remove("offline_pseudo")
            remove("offline_hash")
            remove("auth_mode")
        }
    }
}
