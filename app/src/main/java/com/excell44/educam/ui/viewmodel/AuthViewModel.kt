package com.excell44.educam.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.repository.AuthRepository
import com.excell44.educam.ui.base.BaseViewModel
import com.excell44.educam.ui.base.UiAction
import com.excell44.educam.ui.base.UiState
import com.excell44.educam.util.AuthStateManager
import com.excell44.educam.util.StateRollbackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val guestAttemptsRemaining: Int = 3 // Default to 3
) : UiState

sealed interface AuthAction : UiAction {
    data class Login(val email: String, val pass: String) : AuthAction
    data class Register(val email: String, val pass: String, val name: String, val grade: String) : AuthAction
    data class RegisterFull(
        val pseudo: String,
        val pass: String,
        val fullName: String,
        val gradeLevel: String,
        val school: String,
        val city: String,
        val neighborhood: String,
        val parentName: String?,
        val parentPhone: String?,
        val relation: String?,
        val promoCode: String?
    ) : AuthAction
    data class RegisterOffline(
        val pseudo: String,
        val pass: String,
        val fullName: String,
        val gradeLevel: String
    ) : AuthAction
    object Logout : AuthAction
    object GuestMode : AuthAction
    object ClearError : AuthAction
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authStateManager: AuthStateManager
) : BaseViewModel<AuthUiState, AuthAction>(
    AuthUiState(
        isLoggedIn = authStateManager.isLoggedIn(),
        guestAttemptsRemaining = authStateManager.getGuestAttemptsRemaining()
    )
) {

    private val rollbackManager = StateRollbackManager<AuthUiState>()

    init {
        // Initial check
        val loggedIn = authStateManager.isLoggedIn()
        val attempts = authStateManager.getGuestAttemptsRemaining()
        android.util.Log.d("AuthViewModel", "Init: isLoggedIn=$loggedIn, guestAttempts=$attempts")
        
        updateState { 
            copy(
                isLoggedIn = loggedIn,
                guestAttemptsRemaining = attempts
            ) 
        }
    }

    override fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.Login -> login(action)
            is AuthAction.Register -> register(action)
            is AuthAction.RegisterFull -> registerFull(action)
            is AuthAction.RegisterOffline -> registerOffline(action)
            is AuthAction.Logout -> logout()
            is AuthAction.GuestMode -> setGuestMode()
            is AuthAction.ClearError -> clearError()
        }
    }

    private val loginMutex = kotlinx.coroutines.sync.Mutex()
    
    private fun login(action: AuthAction.Login) {
        viewModelScope.launch {
            // Prevent concurrent login attempts
            if (!loginMutex.tryLock()) {
                return@launch
            }
            
            try {
                saveStateForRollback()
                updateState { copy(isLoading = true, errorMessage = null) }
                
                authRepository.login(action.email, action.pass)
                    .onSuccess { user ->
                        authStateManager.saveUserId(user.id)
                        updateState { 
                            copy(isLoading = false, isLoggedIn = true, errorMessage = null) 
                        }
                    }
                    .onFailure { exception ->
                        updateState { 
                            copy(isLoading = false, errorMessage = exception.message) 
                        }
                    }
            } catch (e: Exception) {
                // Safety net: ensure state is always updated
                updateState { 
                    copy(isLoading = false, errorMessage = e.message ?: "Erreur inconnue") 
                }
            } finally {
                loginMutex.unlock()
            }
        }
    }

    private fun register(action: AuthAction.Register) {
        viewModelScope.launch {
            saveStateForRollback()
            updateState { copy(isLoading = true, errorMessage = null) }
            
            authRepository.register(action.email, action.pass, action.name, action.grade)
                .onSuccess { user ->
                    authStateManager.saveUserId(user.id)
                    updateState { 
                        copy(isLoading = false, isLoggedIn = true, errorMessage = null) 
                    }
                }
                .onFailure { exception ->
                    updateState { 
                        copy(isLoading = false, errorMessage = exception.message) 
                    }
                }
        }
    }

    private fun registerFull(action: AuthAction.RegisterFull) {
        viewModelScope.launch {
            saveStateForRollback()
            updateState { copy(isLoading = true, errorMessage = null) }
            
            // Check phone account limit
            if (!action.parentPhone.isNullOrBlank()) {
                val count = authStateManager.getAccountsForPhone(action.parentPhone)
                if (count >= 3) {
                    updateState { 
                        copy(isLoading = false, errorMessage = "Ce numéro de téléphone a déjà 3 comptes. Contactez le support pour autorisation.") 
                    }
                    return@launch
                }
            }

            authRepository.registerFull(
                pseudo = action.pseudo,
                password = action.pass,
                fullName = action.fullName,
                gradeLevel = action.gradeLevel,
                school = action.school,
                city = action.city,
                neighborhood = action.neighborhood,
                parentName = action.parentName,
                parentPhone = action.parentPhone,
                relation = action.relation,
                promoCode = action.promoCode
            ).onSuccess { user ->
                // save account mapping for phone
                action.parentPhone?.let { authStateManager.incAccountsForPhone(it) }
                // Save user id and mark account active
                authStateManager.saveUserId(user.id)
                authStateManager.saveAccountType("ACTIVE")
                // minimal profile storage
                val profileJson = "{\"pseudo\":\"${action.pseudo}\",\"school\":\"${action.school}\",\"city\":\"${action.city}\"}"
                authStateManager.saveProfileJson(user.id, profileJson)

                updateState { 
                    copy(isLoading = false, isLoggedIn = true, errorMessage = null) 
                }
            }.onFailure { exception ->
                updateState { 
                    copy(isLoading = false, errorMessage = exception.message) 
                }
            }
        }
    }

    private fun registerOffline(action: AuthAction.RegisterOffline) {
        viewModelScope.launch {
            saveStateForRollback()
            updateState { copy(isLoading = true, errorMessage = null) }
            
            authRepository.registerOffline(action.pseudo, action.pass, action.fullName, action.gradeLevel)
                .onSuccess { user ->
                    authStateManager.saveUserId(user.id)
                    authStateManager.saveAccountType("TRIAL") // 7-day trial
                    
                    // minimal profile storage
                    val profileJson = "{\"pseudo\":\"${action.pseudo}\",\"isOffline\":true}"
                    authStateManager.saveProfileJson(user.id, profileJson)

                    updateState { 
                        copy(isLoading = false, isLoggedIn = true, errorMessage = null) 
                    }
                }
                .onFailure { exception ->
                    updateState { 
                        copy(isLoading = false, errorMessage = exception.message) 
                    }
                }
        }
    }

    private fun logout() {
        authStateManager.clearUserId()
        // ✅ Reset account type to GUEST to allow guest mode fallback
        authStateManager.saveAccountType("GUEST") 
        updateState { copy(isLoggedIn = false) }
    }

    private fun setGuestMode() {
        authStateManager.setGuestMode()
        updateState { copy(isLoggedIn = true) }
    }
    
    private fun clearError() {
        updateState { copy(errorMessage = null) }
    }

    private fun saveStateForRollback() {
        rollbackManager.saveState(uiState.value)
    }
    
    fun restorePreviousState() {
        rollbackManager.rollback()?.let { oldState ->
            updateState { oldState }
        }
    }

    // Expose small helpers for UI
    fun getAccountType(): String = authStateManager.getAccountType()

    fun getProfileJsonForCurrentUser(): String? {
        val id = authStateManager.getUserId() ?: return null
        return authStateManager.getProfileJson(id)
    }
}

