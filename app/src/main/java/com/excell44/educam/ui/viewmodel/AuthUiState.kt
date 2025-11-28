package com.excell44.educam.ui.viewmodel

import com.excell44.educam.ui.base.UiState

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val guestAttemptsRemaining: Int = 3 // Default to 3
) : UiState
