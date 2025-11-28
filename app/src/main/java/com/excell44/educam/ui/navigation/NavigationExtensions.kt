package com.excell44.educam.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Extension pour navigation sécurisée.
 * 
 * **PROTECTION**:
 * - Try-catch pour éviter les crashs
 * - Pas de blocage (Mutex retiré pour éviter les deadlocks/drops)
 */

/**
 * Navigate avec protection try-catch.
 * 
 * @param route La destination
 * @param builder Configuration optionnelle de navigation
 */
fun NavController.navigateSafe(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) {
    try {
        println("🧭 Navigation vers: $route")
        if (builder != null) {
            navigate(route, builder)
        } else {
            navigate(route)
        }
    } catch (e: Exception) {
        println("❌ Erreur navigation vers $route: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * PopBackStack avec protection try-catch.
 * 
 * @return true si le pop a réussi, false sinon
 */
fun NavController.popBackStackSafe(): Boolean {
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
    }
}

/**
 * PopBackStack vers une route spécifique avec protection try-catch.
 * 
 * @param route La route de destination
 * @param inclusive Si true, la route de destination est aussi retirée du backstack
 * @return true si le pop a réussi, false sinon
 */
fun NavController.popBackStackToSafe(route: String, inclusive: Boolean = false): Boolean {
    return try {
        println("⬅️ PopBackStack to: $route (inclusive=$inclusive)")
        popBackStack(route, inclusive)
    } catch (e: Exception) {
        println("❌ Erreur popBackStack to $route: ${e.message}")
        e.printStackTrace()
        false
    }
}
