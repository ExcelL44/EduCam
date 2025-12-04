package com.excell44.educam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.core.network.NetworkObserver
import com.excell44.educam.data.repository.AuthRepository
import com.excell44.educam.domain.model.AuthState
import com.excell44.educam.util.AuthStateManager
import com.excell44.educam.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val networkObserver: NetworkObserver,
    private val securePrefs: com.excell44.educam.data.local.SecurePrefs,
    private val authStateManager: AuthStateManager
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Connection state for graceful offline degradation
    private val _connectionState = MutableStateFlow<com.excell44.educam.domain.model.ConnectionState>(
        com.excell44.educam.domain.model.ConnectionState.Offline
    )
    val connectionState: StateFlow<com.excell44.educam.domain.model.ConnectionState> = _connectionState.asStateFlow()
    
    init {
        // Observe network changes to update connection state
        viewModelScope.launch {
            networkObserver.networkStatus.collect { isOnline ->
                Logger.d("AuthViewModel", "Network status changed: Online=$isOnline")
                
                // Update connection state
                _connectionState.value = if (isOnline) {
                    com.excell44.educam.domain.model.ConnectionState.Online
                } else {
                    com.excell44.educam.domain.model.ConnectionState.Offline
                }
                
                // If we are authenticated, update the offline flag
                val currentState = _authState.value
                if (currentState is AuthState.Authenticated) {
                    _authState.value = currentState.copy(isOffline = !isOnline)
                }
            }
        }
        initialize()
    }
    
    private fun initialize() {
        viewModelScope.launch(Dispatchers.IO) {
            Logger.d("AuthViewModel", "Initializing auth state...")
            
            // Clean up expired offline accounts (>24h, unsynced)
            // ⚠️ HYGIÈNE ONLY: Real enforcement is server-side
            // If cleanup fails, DON'T block app startup
            launch {
                try {
                    val cleanedCount = authRepository.cleanExpiredOfflineAccounts()
                    if (cleanedCount > 0) {
                        Logger.i("AuthViewModel", "Startup cleanup: removed $cleanedCount expired account(s)")
                    }
                } catch (e: Exception) {
                    // SURVIE: Log but continue app startup
                    Logger.e("AuthViewModel", "Cleanup failed (non-critical)", e)
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                        .log("Cleanup error: ${e.message}")
                }
            }
            
            try {
                val result = authRepository.getUser()
                result.onSuccess { user ->
                    val isOffline = !networkObserver.isOnline()
                    Logger.i("AuthViewModel", "User found: ${user.id} (Offline: $isOffline)")
                    _authState.value = AuthState.Authenticated(
                        user = user,
                        isOffline = isOffline
                    )
                }.onFailure { e ->
                    Logger.w("AuthViewModel", "No user found or error: ${e.message}")
                    _authState.value = AuthState.Unauthenticated(
                        reason = e.message
                    )
                }
            } catch (e: Exception) {
                Logger.e("AuthViewModel", "Critical error during init", e)
                _authState.value = AuthState.Error(
                    message = e.message ?: "Erreur inconnue",
                    canRetry = true
                )
            }
        }
    }
    
    fun login(pseudo: String, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            android.util.Log.d("🔴 DEBUG_AUTH", "🚀 AuthViewModel.login() called for pseudo: '$pseudo', code: '$code'")
            Logger.d("AuthViewModel", "Attempting login for $pseudo")
            Logger.logUserAction("LoginAttempt", mapOf("pseudo" to pseudo))

            authRepository.login(pseudo, code)
                .onSuccess { user ->
                    Logger.i("AuthViewModel", "Login success: ${user.id}")
                    
                    // 🔍 VERIFICATION: Check if token is persisted
                    val savedId = securePrefs.getUserId()
                    android.util.Log.d("🔴 DEBUG_AUTH", "💾 Persistence check: Saved ID = $savedId")

                    // ✅ FIX: Update AuthState on Main thread to trigger immediate recomposition
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Authenticated(user, !networkObserver.isOnline())
                        android.util.Log.d("🔴 DEBUG_AUTH", "✅ AuthState updated to Authenticated on MAIN thread")
                    }
                }
                .onFailure { e ->
                    Logger.w("AuthViewModel", "Login failed: ${e.message}")
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Error(
                            message = e.message ?: "Échec de connexion",
                            canRetry = true
                        )
                    }
                }
        }
    }

    fun register(pseudo: String, code: String, name: String, gradeLevel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            Logger.d("AuthViewModel", "Attempting registration for $pseudo")
            Logger.logUserAction("RegisterAttempt", mapOf("pseudo" to pseudo, "grade" to gradeLevel))

            authRepository.register(pseudo, code, name, gradeLevel)
                .onSuccess { user ->
                    Logger.i("AuthViewModel", "Registration success: ${user.id}")
                    // ✅ FIX: Update on Main thread
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Authenticated(user, !networkObserver.isOnline())
                    }
                }
                .onFailure { e ->
                    Logger.w("AuthViewModel", "Registration failed: ${e.message}")
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Error(
                            message = e.message ?: "Échec d'inscription",
                            canRetry = true
                        )
                    }
                }
        }
    }

    /**
     * Register user offline (24h trial, PASSIVE role).
     * Used when payment succeeds but device is offline.
     */
    fun registerOffline(pseudo: String, code: String, name: String, gradeLevel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            Logger.d("AuthViewModel", "Attempting offline registration for $pseudo")
            Logger.logUserAction("RegisterOfflineAttempt", mapOf("pseudo" to pseudo, "grade" to gradeLevel))
            
            authRepository.registerOffline(pseudo, code, name, gradeLevel)
                .onSuccess { user ->
                    Logger.i("AuthViewModel", "Offline registration success: ${user.id}")
                    // ✅ FIX: Update on Main thread
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Authenticated(user, isOffline = true)
                    }
                }
                .onFailure { e ->
                    Logger.w("AuthViewModel", "Offline registration failed: ${e.message}")
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Error(
                            message = e.message ?: "Échec d'inscription hors ligne",
                            canRetry = true
                        )
                    }
                }
        }
    }

    fun retry() {
        initialize()
    }
    
    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Logger.i("AuthViewModel", "Logout initiated")
                
                // ✅ CRITICAL: Clear ALL secure session data (credentials + auth mode)
                securePrefs.clearAllAuthData()
                Logger.d("AuthViewModel", "SecurePrefs cleared (all auth data)")
                
                // Set state to unauthenticated
                _authState.value = AuthState.Unauthenticated(reason = "Déconnexion utilisateur")
                
                Logger.i("AuthViewModel", "Logout completed successfully")
                
            } catch (e: Exception) {
                Logger.e("AuthViewModel", "Error during logout", e)
                // Even on error, logout (security measure)
                _authState.value = AuthState.Unauthenticated(reason = "Déconnexion (avec erreur)")
            }
        }
    }

    /**
     * TEST ONLY: Force admin login bypass for testing purposes.
     * This method should be REMOVED before production release.
     */
    fun forceAdminLogin() {
        android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 forceAdminLogin() STARTED - Creating admin user")
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 Launching coroutine for admin login")
            Logger.w("AuthViewModel", "🚨 FORCE ADMIN LOGIN USED - REMOVE IN PRODUCTION")
            Logger.logUserAction("ForceAdminLogin", mapOf("warning" to "test_only"))

            try {
                val adminPseudo = "Sup_Admin"
                android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 Checking if admin user already exists: $adminPseudo")

                // 1. Get or Create User
                var targetUser = authRepository.getUserByPseudo(adminPseudo)
                
                if (targetUser == null) {
                    android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 Creating new admin user")
                    // Create if not exists
                    val result = authRepository.registerOffline(
                        pseudo = adminPseudo,
                        password = "0000",
                        fullName = "Super Administrateur",
                        gradeLevel = "Admin"
                    )
                    targetUser = result.getOrNull()
                } else {
                    android.util.Log.d("🟡 AUTH_VIEWMODEL", "✅ Admin user found: ${targetUser.id}")
                }

                // 2. Force Upgrade to ADMIN and Save
                if (targetUser != null) {
                    android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 Upgrading user to ADMIN/ACTIVE")
                    
                    val upgradedUser = targetUser.copy(
                        role = "ADMIN",
                        syncStatus = "SYNCED",
                        isOfflineAccount = false, // CRITICAL: Treat as Online/Active
                        trialExpiresAt = null // Remove trial limit
                    )
                    
                    // Save to DB (Force Update)
                    authRepository.saveUser(upgradedUser)
                    android.util.Log.d("🟡 AUTH_VIEWMODEL", "✅ User updated in DB")
                    
                    // Save to Prefs
                    securePrefs.saveUserId(upgradedUser.id)
                    securePrefs.saveAuthMode(com.excell44.educam.data.local.SecurePrefs.AuthMode.ONLINE) // Treat as Online
                    authStateManager.saveAccountType("ADMIN")
                    
                    // Update State on Main Thread
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Authenticated(
                            user = upgradedUser,
                            isOffline = !networkObserver.isOnline()
                        )
                        android.util.Log.d("🟡 AUTH_VIEWMODEL", "✅ AuthState updated to Authenticated (ADMIN)")
                    }
                    
                    Logger.i("AuthViewModel", "Force admin login successful - User upgraded to ADMIN/ACTIVE")
                } else {
                    android.util.Log.e("🟡 AUTH_VIEWMODEL", "❌ Failed to create or retrieve admin user")
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Error("Impossible de créer le compte Admin", true)
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("🟡 AUTH_VIEWMODEL", "🚨 forceAdminLogin() FAILED with exception: ${e.message}", e)
                Logger.e("AuthViewModel", "Force admin login failed", e)
                withContext(Dispatchers.Main) {
                    _authState.value = AuthState.Error(
                        message = "Erreur de connexion admin de test",
                        canRetry = true
                    )
                }
            }
        }
        android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 forceAdminLogin() method exited (coroutine launched)")
    }
}
