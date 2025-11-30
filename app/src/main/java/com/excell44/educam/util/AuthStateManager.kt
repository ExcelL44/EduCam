package com.excell44.educam.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // ✅ Lazy initialization to avoid blocking constructor
    private val prefs: SharedPreferences by lazy { 
        context.getSharedPreferences("bacx_prefs", Context.MODE_PRIVATE)
    }

    // Basic user id storage
    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun clearUserId() {
        prefs.edit().remove("user_id").apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null
    }

    // ✅ Async variant to avoid UI thread blocking
    suspend fun isLoggedInAsync(): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        getUserId() != null
    }

    // Account type: ACTIVE, PASSIVE, BETA_T, ADMIN
    fun saveAccountType(type: String) {
        prefs.edit().putString("account_type", type).apply()
    }

    fun getAccountType(): String = prefs.getString("account_type", "PASSIVE") ?: "PASSIVE"

    // Phone -> account count (to limit 3 accounts per phone)
    fun incAccountsForPhone(phone: String) {
        val key = "phone_count_$phone"
        val count = prefs.getInt(key, 0) + 1
        prefs.edit().putInt(key, count).apply()
    }

    fun getAccountsForPhone(phone: String): Int {
        val key = "phone_count_$phone"
        return prefs.getInt(key, 0)
    }

    // Small storage for profile JSON (optional)
    fun saveProfileJson(userId: String, json: String) {
        prefs.edit().putString("profile_$userId", json).apply()
    }

    fun getProfileJson(userId: String): String? = prefs.getString("profile_$userId", null)

    // Trial period logic (7 days)
    fun saveTrialStartDate(timestamp: Long) {
        prefs.edit().putLong("trial_start_date", timestamp).apply()
    }

    fun getTrialStartDate(): Long {
        return prefs.getLong("trial_start_date", 0L)
    }

    fun isTrialExpired(): Boolean {
        val start = getTrialStartDate()
        if (start == 0L) return false // Not started or not applicable
        val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L
        return (System.currentTimeMillis() - start) > sevenDaysMillis
    }
}
