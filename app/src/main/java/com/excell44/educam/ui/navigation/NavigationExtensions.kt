package com.excell44.educam.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder

/**
 * Extension pour navigation sécurisée avec debounce automatique.
 * Empêche les crashs dus aux clics multiples rapides sur les boutons de navigation.
 */
private var lastNavigationTime = 0L
private const val NAVIGATION_DEBOUNCE_MS = 500L

/**
 * Navigate avec protection anti-spam intégrée.
 * Ignore les navigations qui arrivent moins de 500ms après la précédente.
 * 
 * @param route La destination
 * @param builder Configuration optionnelle de navigation
 */
fun NavController.navigateSafe(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) {
    val now = System.currentTimeMillis()
    if (now - lastNavigationTime < NAVIGATION_DEBOUNCE_MS) {
        println("Navigation ignorée (debounce): $route")
        return
    }
    lastNavigationTime = now
    
    try {
        if (builder != null) {
            navigate(route, builder)
        } else {
            navigate(route)
        }
    } catch (e: Exception) {
        println("Erreur navigation vers $route: ${e.message}")
        // Ne pas crasher l'app, juste logger l'erreur
    }
}

/**
 * PopBackStack avec protection contre les crashs.
 */
fun NavController.popBackStackSafe(): Boolean {
    return try {
        popBackStack()
    } catch (e: Exception) {
        println("Erreur popBackStack: ${e.message}")
        false
    }
}
