package com.excell44.educam.ui.navigation

/**
 * Sealed class représentant toutes les commandes de navigation possibles.
 * Garantit que toute navigation passe par un canal unique et thread-safe.
 */
sealed class NavCommand {
    /**
     * Naviguer vers une route spécifique
     */
    data class NavigateTo(
        val route: String,
        val popUpTo: String? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = true
    ) : NavCommand()

    /**
     * Naviguer en arrière
     */
    object PopBack : NavCommand()

    /**
     * Naviguer en arrière vers une route spécifique
     */
    data class PopBackTo(
        val route: String,
        val inclusive: Boolean = false
    ) : NavCommand()

    /**
     * Réinitialiser le backstack et naviguer
     */
    data class NavigateAndClear(
        val route: String
    ) : NavCommand()
}

/**
 * État de la navigation pour gérer les race conditions
 */
enum class NavigationState {
    IDLE,        // ✅ Peut naviguer
    NAVIGATING,  // ⏳ Navigation en cours (verrouillé)
    ERROR        // ❌ Erreur de navigation
}
