package com.excell44.educam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.repository.AuthRepository
import com.excell44.educam.util.AuthStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(isLoggedIn = authStateManager.isLoggedIn()))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _uiState.value = _uiState.value.copy(isLoggedIn = authStateManager.isLoggedIn())
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.login(email, password)
                .onSuccess { user ->
                    authStateManager.saveUserId(user.id)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    fun register(email: String, password: String, name: String, gradeLevel: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.register(email, password, name, gradeLevel)
                .onSuccess { user ->
                    authStateManager.saveUserId(user.id)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    // Extended registration flow used by the in-app form
    fun registerFull(
        pseudo: String,
        password: String,
        fullName: String,
        gradeLevel: String,
        school: String,
        city: String,
        neighborhood: String,
        parentName: String?,
        parentPhone: String?,
        relation: String?,
        promoCode: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            // Check phone account limit
            if (!parentPhone.isNullOrBlank()) {
                val count = authStateManager.getAccountsForPhone(parentPhone)
                if (count >= 3) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Ce numéro de téléphone a déjà 3 comptes. Contactez le support pour autorisation."
                    )
                    return@launch
                }
            }

            authRepository.registerFull(
                pseudo = pseudo,
                password = password,
                fullName = fullName,
                gradeLevel = gradeLevel,
                school = school,
                city = city,
                neighborhood = neighborhood,
                parentName = parentName,
                parentPhone = parentPhone,
                relation = relation,
                promoCode = promoCode
            ).onSuccess { user ->
                // save account mapping for phone
                parentPhone?.let { authStateManager.incAccountsForPhone(it) }
                // Save user id and mark account active
                authStateManager.saveUserId(user.id)
                authStateManager.saveAccountType("ACTIVE")
                // minimal profile storage
                val profileJson = "{\"pseudo\":\"$pseudo\",\"school\":\"$school\",\"city\":\"$city\"}"
                authStateManager.saveProfileJson(user.id, profileJson)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    fun logout() {
        authStateManager.clearUserId()
        _uiState.value = _uiState.value.copy(isLoggedIn = false)
    }

    fun setGuestMode() {
        authStateManager.setGuestMode()
        _uiState.value = _uiState.value.copy(isLoggedIn = true)
    }

    // Expose small helpers for UI
    fun getAccountType(): String = authStateManager.getAccountType()

    fun getProfileJsonForCurrentUser(): String? {
        val id = authStateManager.getUserId() ?: return null
        return authStateManager.getProfileJson(id)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

