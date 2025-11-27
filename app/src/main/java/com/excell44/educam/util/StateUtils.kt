package com.excell44.educam.util

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Utilitaires pour State Hoisting et gestion d'état robuste.
 */

/**
 * Crée un state qui survit aux recompositions ET aux changements de configuration.
 * Utilise SavedStateHandle sous le capot.
 * 
 * @param initialValue Valeur initiale
 * @param key Clé unique pour sauvegarder l'état (optionnel)
 */
@Composable
fun <T : Any> rememberSaveableState(
    initialValue: T,
    key: String? = null,
    saver: Saver<T, out Any> = autoSaver()
): MutableState<T> {
    return rememberSaveable(
        key = key,
        saver = saver,
        init = { mutableStateOf(initialValue) }
    )
}

/**
 * Crée un saver automatique pour les types basiques.
 */
@Suppress("UNCHECKED_CAST")
private fun <T : Any> autoSaver(): Saver<T, Any> = Saver(
    save = { it },
    restore = { it as T }
)

/**
 * Pattern pour State Hoisting propre.
 * Sépare la logique d'état de la présentation.
 */
data class UiState<T>(
    val value: T,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isSuccess: Boolean get() = !isLoading && error == null
    val isError: Boolean get() = error != null
}

/**
 * Helper pour créer un state hoisted.
 */
@Composable
fun <T> rememberUiState(
    initialValue: T
): MutableState<UiState<T>> {
    return remember { mutableStateOf(UiState(initialValue)) }
}

/**
 * Extension pour mettre à jour un UiState facilement.
 */
fun <T> MutableState<UiState<T>>.updateValue(newValue: T) {
    value = value.copy(value = newValue, isLoading = false, error = null)
}

fun <T> MutableState<UiState<T>>.setLoading() {
    value = value.copy(isLoading = true, error = null)
}

fun <T> MutableState<UiState<T>>.setError(error: String) {
    value = value.copy(isLoading = false, error = error)
}
