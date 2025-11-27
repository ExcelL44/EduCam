package com.excell44.educam.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Utilitaires pour gérer les Flows de manière robuste.
 */

/**
 * Extension pour debouncer et filtrer les doublons sur un Flow.
 * Idéal pour les champs de recherche ou les saisies texte.
 *
 * @param timeoutMillis Le temps d'attente en ms (défaut 300ms)
 */
@OptIn(FlowPreview::class)
fun <T> Flow<T>.debounceInput(timeoutMillis: Long = 300L): Flow<T> {
    return this
        .debounce(timeoutMillis)
        .distinctUntilChanged()
}

/**
 * Classe utilitaire pour gérer un champ de saisie avec debounce intégré dans un ViewModel.
 * Évite de devoir configurer manuellement le Flow dans le ViewModel.
 *
 * @param initialValue La valeur initiale
 * @param debounceTime Le temps de debounce (défaut 500ms)
 * @param scope Le CoroutineScope du ViewModel
 * @param onDebouncedChange Callback appelé après le délai de debounce
 */
class DebouncedState<T>(
    initialValue: T,
    private val debounceTime: Long = 500L,
    scope: CoroutineScope,
    onDebouncedChange: suspend (T) -> Unit
) {
    private val _value = MutableStateFlow(initialValue)
    val value: StateFlow<T> = _value.asStateFlow()

    init {
        scope.launch {
            _value
                .debounceInput(debounceTime)
                .collect {
                    onDebouncedChange(it)
                }
        }
    }

    fun update(newValue: T) {
        _value.value = newValue
    }
}
