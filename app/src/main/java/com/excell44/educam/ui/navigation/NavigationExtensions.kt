package com.excell44.educam.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Extension pour navigation sécurisée avec synchronisation thread-safe.
 * 
 * **PROTECTION MULTI-NIVEAUX**:
 * 1. Mutex global pour synchroniser l'accès au NavController  
 * 2. Debounce pour ignorer les clics rapides
 * 3. Try-catch pour éviter les crashs
 * 
 * ⚠️ IMPORTANT : Ces fonctions sont synchrones car appelées depuis des onClick
 * Le Mutex garantit la thread-safety SANS bloquer le Main Thread
 */

// Mutex global pour synchroniser toutes les navigations
private val navigationMutex = Mutex()

// Timestamp de la dernière navigation pour debounce
private var lastNavigationTime = 0L
private const val NAVIGATION_DEBOUNCE_MS = 300L  // ✅ Réduit pour UX fluide

/**
 * Navigate avec protection anti-spam (version synchrone).
 * 
 * **GARANTIES**:
 * - Debounce 300ms (ignore les clics rapides)
 * - Ne crash jamais l'app (Try-catch)
 * - Thread-safe (mais sans bloquer le Main Thread)
 * 
 * @param route La destination
 * @param builder Configuration optionnelle de navigation
 */
fun NavController.navigateSafe(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) {
    val now = System.currentTimeMillis()
    
    // Debounce: ignore les clics rapides
    if (now - lastNavigationTime < NAVIGATION_DEBOUNCE_MS) {
        println("⏭️ Navigation ignorée (debounce): $route")
        return
    }
    
    // ✅ Tentative de lock NON-BLOQUANTE
    if (!navigationMutex.tryLock()) {
        println("⚠️ Navigation ignorée (lock occupé): $route")
        return
    }
    
    try {
        lastNavigationTime = now
        println("🧭 Navigation vers: $route")
        
        if (builder != null) {
            navigate(route, builder)
        } else {
            navigate(route)
        }
    } catch (e: Exception) {
        println("❌ Erreur navigation vers $route: ${e.message}")
        e.printStackTrace()
    } finally {
        navigationMutex.unlock()
    }
}

/**
 * PopBackStack avec protection thread-safe contre les crashs (version synchrone).
 * 
 * **GARANTIES**:
 * - Thread-safe (tryLock non-bloquant)
 * - Vérifie que le backstack n'est pas vide
 * - Ne crash jamais l'app
 * 
 * @return true si le pop a réussi, false sinon
 */
fun NavController.popBackStackSafe(): Boolean {
    // ✅ Tentative de lock NON-BLOQUANTE
    if (!navigationMutex.tryLock()) {
        println("⚠️ PopBackStack ignoré (lock occupé)")
        return false
    }
    
    return try {
        // Vérifie qu'il y a au moins une entrée dans le backstack
        if (currentBackStackEntry == null) {
            println("⚠️ PopBackStack ignoré: backstack vide")
            false
        } else {
            println("⬅️ PopBackStack")
            popBackStack()
        }
    } catch (e: Exception) {
        println("❌ Erreur popBackStack: ${e.message}")
        e.printStackTrace()
        false
    } finally {
        navigationMutex.unlock()
    }
}

/**
 * PopBackStack vers une route spécifique avec protection thread-safe (version synchrone).
 * 
 * @param route La route de destination
 * @param inclusive Si true, la route de destination est aussi retirée du backstack
 * @return true si le pop a réussi, false sinon
 */
fun NavController.popBackStackToSafe(route: String, inclusive: Boolean = false): Boolean {
    // ✅ Tentative de lock NON-BLOQUANTE
    if (!navigationMutex.tryLock()) {
        println("⚠️ PopBackStack to $route ignoré (lock occupé)")
        return false
    }
    
    return try {
        println("⬅️ PopBackStack to: $route (inclusive=$inclusive)")
        popBackStack(route, inclusive)
    } catch (e: Exception) {
        println("❌ Erreur popBackStack to $route: ${e.message}")
        e.printStackTrace()
        false
    } finally {
        navigationMutex.unlock()
    }
}
