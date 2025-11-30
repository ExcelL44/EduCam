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
    private val networkObserver: NetworkObserver
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Observe network changes to trigger sync or update state
        viewModelScope.launch {
            networkObserver.networkStatus.collect { isOnline ->
                Logger.d("AuthViewModel", "Network status changed: Online=$isOnline")
                // If we are authenticated, we might want to update the offline flag
                val currentState = _authState.value
                if (currentState is AuthState.Authenticated) {
                    _authState.value = currentState.copy(isOffline = !isOnline)
                }
                // Potentially trigger sync if coming online
                if (isOnline) {
                    // TODO: Trigger sync if needed
                }
            }
        }
        initialize()
    }
    
    private fun initialize() {
        viewModelScope.launch(Dispatchers.IO) {
            Logger.d("AuthViewModel", "Initializing auth state...")
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
    
    fun login(email: String, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            Logger.d("AuthViewModel", "Attempting login for $email")
            Logger.logUserAction("LoginAttempt", mapOf("email" to email))
            
            authRepository.login(email, code)
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

    fun register(email: String, code: String, name: String, gradeLevel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            Logger.d("AuthViewModel", "Attempting registration for $email")
            Logger.logUserAction("RegisterAttempt", mapOf("email" to email, "grade" to gradeLevel))
            
            authRepository.register(email, code, name, gradeLevel)
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

    fun loginAsGuest() {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            Logger.d("AuthViewModel", "Attempting guest login...")
            Logger.logUserAction("GuestLoginAttempt")
            
            authRepository.loginAnonymous()
                .onSuccess { user ->
                    Logger.i("AuthViewModel", "Guest login success: ${user.id}")
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { e ->
                    Logger.e("AuthViewModel", "Guest login failed", e)
                    _authState.value = AuthState.Error(
                        message = e.message ?: "Erreur de connexion invité",
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
            // TODO: Implement proper logout in Repository (clear prefs, firebase signOut)
            _authState.value = AuthState.Unauthenticated()
        }
    }
}
