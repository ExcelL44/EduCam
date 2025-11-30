package com.excell44.educam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.core.network.NetworkObserver
import com.excell44.educam.data.repository.AuthRepository
import com.excell44.educam.domain.model.AuthState
import com.excell44.educam.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val networkObserver: NetworkObserver,
    private val securePrefs: com.excell44.educam.data.local.SecurePrefs
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
            Logger.d("AuthViewModel", "Attempting login for $pseudo")
            Logger.logUserAction("LoginAttempt", mapOf("pseudo" to pseudo))

            authRepository.login(pseudo, code)
                .onSuccess { user ->
                    Logger.i("AuthViewModel", "Login success: ${user.id}")
                    _authState.value = AuthState.Authenticated(user, !networkObserver.isOnline())
                }
                .onFailure { e ->
                    Logger.w("AuthViewModel", "Login failed: ${e.message}")
                    _authState.value = AuthState.Error(
                        message = e.message ?: "Échec de connexion",
                        canRetry = true
                    )
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
                    _authState.value = AuthState.Authenticated(user, !networkObserver.isOnline())
                }
                .onFailure { e ->
                    Logger.w("AuthViewModel", "Registration failed: ${e.message}")
                    _authState.value = AuthState.Error(
                        message = e.message ?: "Échec d'inscription",
                        canRetry = true
                    )
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
                    _authState.value = AuthState.Authenticated(user, isOffline = true)
                }
                .onFailure { e ->
                    Logger.w("AuthViewModel", "Offline registration failed: ${e.message}")
                    _authState.value = AuthState.Error(
                        message = e.message ?: "Échec d'inscription hors ligne",
                        canRetry = true
                    )
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
                
                // ✅ CRITICAL: Clear secure session
                securePrefs.clearUserId()
                Logger.d("AuthViewModel", "SecurePrefs cleared")
                
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
                android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 Creating fake admin user object")
                // Create fake admin user
                val adminUser = com.excell44.educam.data.model.User(
                    id = "admin_test_123",
                    pseudo = "Sup_Admin",
                    passwordHash = "", // No password for admin bypass
                    salt = "",
                    name = "Super Administrateur",
                    gradeLevel = "Admin",
                    role = "ADMIN",
                    isOfflineAccount = false,
                    syncStatus = "SYNCED",
                    lastSyncTimestamp = System.currentTimeMillis()
                )
                android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 Admin user created: ${adminUser.pseudo} (${adminUser.role})")

                // ✅ CRITICAL: Save to database AND SecurePrefs to persist session
                // This ensures initialize() can find the user
                try {
                    authRepository.registerOffline(
                        pseudo = adminUser.pseudo,
                        password = "0000", // Dummy code
                        fullName = adminUser.name,
                        gradeLevel = adminUser.gradeLevel
                    ).onSuccess { createdUser ->
                        // Update with ADMIN role
                        val adminUserWithId = createdUser.copy(role = "ADMIN", syncStatus = "SYNCED")
                        // Save ID to prefs
                        securePrefs.saveUserId(adminUserWithId.id)
                        
                        android.util.Log.d("🟡 AUTH_VIEWMODEL", "✅ Admin user saved to DB and SecurePrefs")
                        
                        val isOffline = !networkObserver.isOnline()
                        
                        // Set authenticated state
                        _authState.value = AuthState.Authenticated(
                            user = adminUserWithId,
                            isOffline = isOffline
                        )
                        
                        Logger.i("AuthViewModel", "Force admin login successful - TEST ONLY")
                        android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 forceAdminLogin() COMPLETED SUCCESSFULLY")
                    }.onFailure { error ->
                        android.util.Log.e("🟡 AUTH_VIEWMODEL", "Failed to save admin to DB: ${error.message}")
                        
                        // Fallback: Just save to prefs (in-memory only)
                        securePrefs.saveUserId(adminUser.id)
                        android.util.Log.d("🟡 AUTH_VIEWMODEL", "✅ Admin user ID saved to SecurePrefs (fallback)")
                        
                        val isOffline = !networkObserver.isOnline()
                        
                        _authState.value = AuthState.Authenticated(
                            user = adminUser,
                            isOffline = isOffline
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("🟡 AUTH_VIEWMODEL", "Exception saving admin: ${e.message}", e)
                    
                    // Fallback: Just save to prefs
                    securePrefs.saveUserId(adminUser.id)
                    
                    val isOffline = !networkObserver.isOnline()
                    
                    _authState.value = AuthState.Authenticated(
                        user = adminUser,
                        isOffline = isOffline
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("🟡 AUTH_VIEWMODEL", "🚨 forceAdminLogin() FAILED with exception: ${e.message}", e)
                Logger.e("AuthViewModel", "Force admin login failed", e)
                _authState.value = AuthState.Error(
                    message = "Erreur de connexion admin de test",
                    canRetry = true
                )
                android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 Auth state set to Error")
            }
        }
        android.util.Log.d("🟡 AUTH_VIEWMODEL", "🚨 forceAdminLogin() method exited (coroutine launched)")
    }
}
