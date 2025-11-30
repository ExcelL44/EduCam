// ========== EXEMPLE D'UTILISATION DE isUserAllowedAccess() ==========
// Ce fichier montre comment utiliser la nouvelle méthode de vérification d'accès

package com.excell44.educam.examples

import com.excell44.educam.data.repository.AuthRepository
import kotlinx.coroutines.flow.first

/**
 * Exemple 1: Vérification au démarrage de l'app
 * À utiliser dans MainActivity ou SplashScreen
 */
suspend fun checkUserAccessOnStartup(authRepository: AuthRepository): String {
    val hasAccess = authRepository.isUserAllowedAccess()
    
    return if (hasAccess) {
        // User has valid access (OFFLINE or ONLINE)
        "home" // Navigate to home
    } else {
        // No valid access
        "login" // Navigate to login
    }
}

/**
 * Exemple 2: Vérifier l'accès avant une action sensible
 */
suspend fun performSensitiveAction(authRepository: AuthRepository) {
    if (!authRepository.isUserAllowedAccess()) {
        throw SecurityException("Accès non autorisé")
    }
    
    // Perform action...
}

/**
 * Exemple 3: Obtenir le user avec validation d'accès
 */
suspend fun getUserWithAccessCheck(authRepository: AuthRepository) {
    if (!authRepository.isUserAllowedAccess()) {
        println("Pas d'accès - rediriger vers login")
        return
    }
    
    // Get user data
    val result = authRepository.getUser()
    result.onSuccess { user ->
        println("User: ${user.name} (Mode: ${user.isOfflineAccount})")
    }
}

/**
 * Exemple 4: Vérifier le type d'accès (OFFLINE vs ONLINE)
 */
suspend fun checkAccessType(securePrefs: com.excell44.educam.data.local.SecurePrefs) {
    val authMode = securePrefs.getAuthMode()
    
    when (authMode) {
        com.excell44.educam.data.local.SecurePrefs.AuthMode.OFFLINE -> {
            println("⚠️ Mode OFFLINE - Fonctionnalités limitées")
            // Désactiver certaines features qui nécessitent le serveur
        }
        com.excell44.educam.data.local.SecurePrefs.AuthMode.ONLINE -> {
            println("✅ Mode ONLINE - Toutes les fonctionnalités disponibles")
            // Toutes les features activées
        }
        null -> {
            println("❌ Pas d'authentification")
            // Rediriger vers login
        }
    }
}

/**
 * Exemple 5: Synchroniser quand la connexion revient
 */
suspend fun autoSyncWhenOnline(
    authRepository: AuthRepository,
    securePrefs: com.excell44.educam.data.local.SecurePrefs,
    isOnline: Boolean
) {
    if (!isOnline) return
    
    val authMode = securePrefs.getAuthMode()
    if (authMode == com.excell44.educam.data.local.SecurePrefs.AuthMode.OFFLINE) {
        // Get saved credentials
        val credentials = securePrefs.getOfflineCredentials()
        if (credentials != null) {
            val (pseudo, hash) = credentials
            println("🔄 Synchronisation de $pseudo avec le serveur...")
            
            // TODO: Call API to sync
            // On success:
            securePrefs.saveAuthMode(com.excell44.educam.data.local.SecurePrefs.AuthMode.ONLINE)
            println("✅ Synchronisation réussie - mode ONLINE activé")
        }
    }
}
