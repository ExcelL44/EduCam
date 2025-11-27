package com.excell44.educam.core.fallback

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Gestionnaire de données d'urgence.
 * Sauvegarde et restaure les données critiques en cas de problème.
 */
class DataRecoveryManager(private val context: Context) {
    
    private val backupDir = File(context.filesDir, "emergency_backup")
    
    init {
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
    }
    
    /**
     * Sauvegarde les données critiques.
     */
    suspend fun backupCriticalData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Backup SharedPreferences
            backupSharedPreferences()
            
            // Backup Database (copie)
            backupDatabase()
            
            // Save backup timestamp
            saveBackupTimestamp()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Restaure les données depuis le dernier backup.
     */
    suspend fun restoreFromBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupExists = checkBackupExists()
            if (!backupExists) {
                return@withContext Result.failure(Exception("No backup found"))
            }
            
            // Restore SharedPreferences
            restoreSharedPreferences()
            
            // Restore Database
            restoreDatabase()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Vérifie si un backup existe.
     */
    fun checkBackupExists(): Boolean {
        val timestampFile = File(backupDir, "backup_timestamp.txt")
        return timestampFile.exists()
    }
    
    /**
     * Obtient la date du dernier backup.
     */
    fun getLastBackupTimestamp(): Long? {
        val timestampFile = File(backupDir, "backup_timestamp.txt")
        return try {
            timestampFile.readText().toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Efface les données en cache (pour un reset complet).
     */
    suspend fun clearCacheData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.cacheDir.deleteRecursively()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset complet de l'application (dernier recours).
     */
    suspend fun performFactoryReset(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Backup critical user data first
            backupCriticalData()
            
            // Clear cache
            context.cacheDir.deleteRecursively()
            
            // Clear all SharedPreferences except emergency
            clearNonCriticalPreferences()
            
            // Clear database
            clearDatabaseData()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // === PRIVATE METHODS ===
    
    private fun backupSharedPreferences() {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        val backupPrefsDir = File(backupDir, "shared_prefs")
        
        if (!backupPrefsDir.exists()) {
            backupPrefsDir.mkdirs()
        }
        
        prefsDir.listFiles()?.forEach { prefsFile ->
            val backupFile = File(backupPrefsDir, prefsFile.name)
            prefsFile.copyTo(backupFile, overwrite = true)
        }
    }
    
    private fun restoreSharedPreferences() {
        val backupPrefsDir = File(backupDir, "shared_prefs")
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        
        backupPrefsDir.listFiles()?.forEach { backupFile ->
            val prefsFile = File(prefsDir, backupFile.name)
            backupFile.copyTo(prefsFile, overwrite = true)
        }
    }
    
    private fun backupDatabase() {
        val dbPath = context.getDatabasePath("educam_database")
        if (dbPath.exists()) {
            val backupDbFile = File(backupDir, "educam_database.db")
            dbPath.copyTo(backupDbFile, overwrite = true)
        }
    }
    
    private fun restoreDatabase() {
        val backupDbFile = File(backupDir, "educam_database.db")
        if (backupDbFile.exists()) {
            val dbPath = context.getDatabasePath("educam_database")
            backupDbFile.copyTo(dbPath, overwrite = true)
        }
    }
    
    private fun saveBackupTimestamp() {
        val timestampFile = File(backupDir, "backup_timestamp.txt")
        timestampFile.writeText(System.currentTimeMillis().toString())
    }
    
    private fun clearNonCriticalPreferences() {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        prefsDir.listFiles()?.forEach { prefsFile ->
            // Ne pas supprimer emergency_fallback et user_sessions
            if (!prefsFile.name.contains("emergency") && 
                !prefsFile.name.contains("session")) {
                prefsFile.delete()
            }
        }
    }
    
    private fun clearDatabaseData() {
        val dbPath = context.getDatabasePath("educam_database")
        if (dbPath.exists()) {
            dbPath.delete()
        }
    }
    
    companion object {
        @Volatile
        private var instance: DataRecoveryManager? = null
        
        fun getInstance(context: Context): DataRecoveryManager {
            return instance ?: synchronized(this) {
                instance ?: DataRecoveryManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
