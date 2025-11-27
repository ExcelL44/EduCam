package com.excell44.educam.core.fallback

import android.content.Context
import android.content.SharedPreferences

/**
 * Système de fallback d'urgence pour situations critiques.
 * Active un "Mode Sans Échec" si l'app crash de manière répétée.
 */
class EmergencyFallback private constructor(
    private val context: Context
) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("emergency_fallback", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_CRASH_COUNT = "crash_count"
        private const val KEY_LAST_CRASH_TIME = "last_crash_time"
        private const val KEY_SAFE_MODE_ACTIVE = "safe_mode_active"
        private const val KEY_FIRST_CRASH_TIME = "first_crash_time"
        
        private const val CRASH_THRESHOLD = 3  // 3 crashs en 5 min = Safe Mode
        private const val TIME_WINDOW_MS = 5 * 60 * 1000L  // 5 minutes
        private const val SAFE_MODE_DURATION_MS = 30 * 60 * 1000L  // 30 min
        
        @Volatile
        private var instance: EmergencyFallback? = null
        
        fun getInstance(context: Context): EmergencyFallback {
            return instance ?: synchronized(this) {
                instance ?: EmergencyFallback(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Enregistre un crash et active le Safe Mode si nécessaire.
     */
    fun recordCrash() {
        val now = System.currentTimeMillis()
        val lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0L)
        val firstCrashTime = prefs.getLong(KEY_FIRST_CRASH_TIME, now)
        val crashCount = prefs.getInt(KEY_CRASH_COUNT, 0)
        
        // Si le dernier crash était il y a > 5 min, reset le compteur
        if (now - lastCrashTime > TIME_WINDOW_MS) {
            prefs.edit()
                .putInt(KEY_CRASH_COUNT, 1)
                .putLong(KEY_LAST_CRASH_TIME, now)
                .putLong(KEY_FIRST_CRASH_TIME, now)
                .apply()
            return
        }
        
        // Incrémenter le compteur
        val newCrashCount = crashCount + 1
        prefs.edit()
            .putInt(KEY_CRASH_COUNT, newCrashCount)
            .putLong(KEY_LAST_CRASH_TIME, now)
            .apply()
        
        // Activer Safe Mode si seuil atteint
        if (newCrashCount >= CRASH_THRESHOLD) {
            activateSafeMode()
        }
    }
    
    /**
     * Active le mode sans échec.
     */
    private fun activateSafeMode() {
        prefs.edit()
            .putBoolean(KEY_SAFE_MODE_ACTIVE, true)
            .putLong("safe_mode_activated_at", System.currentTimeMillis())
            .apply()
        
        println("🚨 SAFE MODE ACTIVATED - Too many crashes detected")
    }
    
    /**
     * Vérifie si le mode sans échec est actif.
     */
    fun isSafeModeActive(): Boolean {
        if (!prefs.getBoolean(KEY_SAFE_MODE_ACTIVE, false)) {
            return false
        }
        
        // Vérifier si le Safe Mode a expiré
        val activatedAt = prefs.getLong("safe_mode_activated_at", 0L)
        val now = System.currentTimeMillis()
        
        if (now - activatedAt > SAFE_MODE_DURATION_MS) {
            // Safe Mode expiré, désactiver
            deactivateSafeMode()
            return false
        }
        
        return true
    }
    
    /**
     * Désactive manuellement le Safe Mode.
     */
    fun deactivateSafeMode() {
        prefs.edit()
            .putBoolean(KEY_SAFE_MODE_ACTIVE, false)
            .putInt(KEY_CRASH_COUNT, 0)
            .remove("safe_mode_activated_at")
            .apply()
        
        println("✅ Safe Mode deactivated")
    }
    
    /**
     * Obtient le compteur de crashs actuels.
     */
    fun getCrashCount(): Int {
        return prefs.getInt(KEY_CRASH_COUNT, 0)
    }
    
    /**
     * Configuration pour le Safe Mode.
     */
    fun getSafeModeConfig(): SafeModeConfig {
        return SafeModeConfig(
            disableAnimations = true,
            disableBackgroundSync = true,
            disableCache = true,
            disableHeavyFeatures = true,
            forceOfflineMode = true,
            reducedFunctionality = true,
            showSafeModeIndicator = true
        )
    }
    
    /**
     * Reset complet du système d'urgence.
     */
    fun resetEmergencySystem() {
        prefs.edit().clear().apply()
        println("🔄 Emergency system reset")
    }
}

/**
 * Configuration du mode sans échec.
 */
data class SafeModeConfig(
    val disableAnimations: Boolean,
    val disableBackgroundSync: Boolean,
    val disableCache: Boolean,
    val disableHeavyFeatures: Boolean,
    val forceOfflineMode: Boolean,
    val reducedFunctionality: Boolean,
    val showSafeModeIndicator: Boolean
)
