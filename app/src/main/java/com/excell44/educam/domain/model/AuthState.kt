package com.excell44.educam.domain.model

import com.excell44.educam.data.model.User

sealed class AuthState {
    object Loading : AuthState()
    
    data class Authenticated(
        val user: User,
        val isOffline: Boolean = false
    ) : AuthState()
    
    data class Unauthenticated(
        val reason: String? = null
    ) : AuthState()
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : AuthState()
    
    object NeedsRegistration : AuthState()
    
    // Offline-First States
    data class NeedsSync(
        val user: User, 
        val pendingChanges: Int
    ) : AuthState()
    
    data class Passive(
        val user: User, 
        val reason: String
    ) : AuthState()
    
    data class OfflineTrial(
        val user: User, 
        val expiresAt: Long
    ) : AuthState()
}
