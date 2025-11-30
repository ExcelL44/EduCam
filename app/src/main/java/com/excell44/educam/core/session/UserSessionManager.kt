package com.excell44.educam.core.session

import android.content.Context
import com.excell44.educam.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestionnaire de session utilisateur singleton.
 * Gère la session active, le changement de compte, et les limites par appareil.
 */
class UserSessionManager private constructor(
    private val context: Context
) {
    
    private val prefs = context.getSharedPreferences("user_sessions", Context.MODE_PRIVATE)
    
    // Session active actuelle
    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()
    
    // Liste des comptes sur cet appareil
    private val _deviceAccounts = MutableStateFlow<List<DeviceAccount>>(emptyList())
    val deviceAccounts: StateFlow<List<DeviceAccount>> = _deviceAccounts.asStateFlow()
    
    companion object {
        const val MAX_ACCOUNTS_PER_DEVICE = 3
        
        @Volatile
        private var instance: UserSessionManager? = null
        
        fun getInstance(context: Context): UserSessionManager {
            return instance ?: synchronized(this) {
                instance ?: UserSessionManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    init {
        loadSavedSessions()
    }
    
    /**
     * Crée ou restaure une session utilisateur.
     */
    suspend fun createSession(user: User): Result<UserSession> {
        // Vérifier la limite d'appareils
        val accountCount = _deviceAccounts.value.size
        
        // Si l'utilisateur existe déjà, pas de problème
        val existingAccount = _deviceAccounts.value.find { it.userId == user.id }
        if (existingAccount == null && accountCount >= MAX_ACCOUNTS_PER_DEVICE) {
            return Result.failure(
                Exception("Limite de $MAX_ACCOUNTS_PER_DEVICE comptes atteinte sur cet appareil")
            )
        }
        
        val session = UserSession(
            userId = user.id,
            userName = user.name,
            userEmail = user.pseudo,
            isOfflineAccount = user.isOfflineAccount,
            sessionStartTime = System.currentTimeMillis(),
            lastActivityTime = System.currentTimeMillis()
        )
        
        _currentSession.value = session
        
        // Ajouter ou mettre à jour le compte sur l'appareil
        addOrUpdateDeviceAccount(user)
        
        // Sauvegarder
        saveCurrentSession(session)
        
        return Result.success(session)
    }
    
    /**
     * Change de compte (switch user).
     */
    suspend fun switchAccount(userId: String): Result<Unit> {
        val account = _deviceAccounts.value.find { it.userId == userId }
            ?: return Result.failure(Exception("Compte non trouvé sur cet appareil"))
        
        val session = UserSession(
            userId = account.userId,
            userName = account.userName,
            userEmail = account.userEmail,
            isOfflineAccount = account.isOfflineAccount,
            sessionStartTime = System.currentTimeMillis(),
            lastActivityTime = System.currentTimeMillis()
        )
        
        _currentSession.value = session
        saveCurrentSession(session)
        
        return Result.success(Unit)
    }
    
    /**
     * Termine la session actuelle.
     */
    fun endSession() {
        _currentSession.value?.let { session ->
            val duration = System.currentTimeMillis() - session.sessionStartTime
            println("Session terminée: ${session.userName} - Durée: ${duration / 1000}s")
        }
        
        _currentSession.value = null
        prefs.edit().remove("current_session").apply()
    }
    
    /**
     * Met à jour le temps de dernière activité.
     */
    fun updateActivity() {
        _currentSession.value?.let { session ->
            _currentSession.value = session.copy(
                lastActivityTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Supprime un compte de l'appareil.
     */
    suspend fun removeAccount(userId: String): Result<Unit> {
        // Ne pas supprimer si c'est le compte actif
        if (_currentSession.value?.userId == userId) {
            return Result.failure(Exception("Impossible de supprimer le compte actif"))
        }
        
        _deviceAccounts.value = _deviceAccounts.value.filter { it.userId != userId }
        saveDeviceAccounts()
        
        return Result.success(Unit)
    }
    
    /**
     * Vérifie si la limite de comptes est atteinte.
     */
    fun isAccountLimitReached(): Boolean {
        return _deviceAccounts.value.size >= MAX_ACCOUNTS_PER_DEVICE
    }
    
    /**
     * Obtient le nombre de comptes sur cet appareil.
     */
    fun getAccountCount(): Int = _deviceAccounts.value.size
    
    // === PRIVATE METHODS ===
    
    private fun addOrUpdateDeviceAccount(user: User) {
        val accounts = _deviceAccounts.value.toMutableList()
        val existingIndex = accounts.indexOfFirst { it.userId == user.id }
        
        val deviceAccount = DeviceAccount(
            userId = user.id,
            userName = user.name,
            userEmail = user.pseudo,
            isOfflineAccount = user.isOfflineAccount,
            addedAt = System.currentTimeMillis(),
            lastUsedAt = System.currentTimeMillis()
        )
        
        if (existingIndex >= 0) {
            accounts[existingIndex] = deviceAccount
        } else {
            accounts.add(deviceAccount)
        }
        
        _deviceAccounts.value = accounts
        saveDeviceAccounts()
    }
    
    private fun saveCurrentSession(session: UserSession) {
        prefs.edit()
            .putString("current_session_user_id", session.userId)
            .putString("current_session_user_name", session.userName)
            .putString("current_session_user_email", session.userEmail)
            .putBoolean("current_session_is_offline", session.isOfflineAccount)
            .putLong("current_session_start", session.sessionStartTime)
            .putLong("current_session_activity", session.lastActivityTime)
            .apply()
    }
    
    private fun saveDeviceAccounts() {
        val json = _deviceAccounts.value.joinToString("|") { account ->
            "${account.userId}::${account.userName}::${account.userEmail}::${account.isOfflineAccount}::${account.addedAt}::${account.lastUsedAt}"
        }
        prefs.edit().putString("device_accounts", json).apply()
    }
    
    private fun loadSavedSessions() {
        // Charger la session actuelle
        val userId = prefs.getString("current_session_user_id", null)
        if (userId != null) {
            _currentSession.value = UserSession(
                userId = userId,
                userName = prefs.getString("current_session_user_name", "") ?: "",
                userEmail = prefs.getString("current_session_user_email", "") ?: "",
                isOfflineAccount = prefs.getBoolean("current_session_is_offline", false),
                sessionStartTime = prefs.getLong("current_session_start", 0L),
                lastActivityTime = prefs.getLong("current_session_activity", 0L)
            )
        }
        
        // Charger les comptes de l'appareil
        val accountsJson = prefs.getString("device_accounts", "") ?: ""
        if (accountsJson.isNotEmpty()) {
            val accounts = accountsJson.split("|").mapNotNull { line ->
                val parts = line.split("::")
                if (parts.size == 6) {
                    DeviceAccount(
                        userId = parts[0],
                        userName = parts[1],
                        userEmail = parts[2],
                        isOfflineAccount = parts[3].toBoolean(),
                        addedAt = parts[4].toLongOrNull() ?: 0L,
                        lastUsedAt = parts[5].toLongOrNull() ?: 0L
                    )
                } else null
            }
            _deviceAccounts.value = accounts
        }
    }
}

/**
 * Représente une session utilisateur active.
 */
data class UserSession(
    val userId: String,
    val userName: String,
    val userEmail: String,
    val isOfflineAccount: Boolean,
    val sessionStartTime: Long,
    val lastActivityTime: Long
) {
    val sessionDuration: Long
        get() = System.currentTimeMillis() - sessionStartTime
    
    val isIdle: Boolean
        get() = System.currentTimeMillis() - lastActivityTime > 30 * 60 * 1000 // 30 minutes
}

/**
 * Représente un compte enregistré sur l'appareil.
 */
data class DeviceAccount(
    val userId: String,
    val userName: String,
    val userEmail: String,
    val isOfflineAccount: Boolean,
    val addedAt: Long,
    val lastUsedAt: Long
)
