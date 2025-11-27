package com.excell44.educam.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel de base pour l'architecture MVI (Model-View-Intent).
 * Gère l'état UI (StateFlow) et les actions utilisateur (Channel).
 * Intègre un mécanisme anti-spam (debounce) pour les actions.
 *
 * @param S Le type de l'état UI (doit implémenter UiState)
 * @param A Le type des actions UI (doit implémenter UiAction)
 */
@OptIn(FlowPreview::class)
abstract class BaseViewModel<S : UiState, A : UiAction>(initialState: S) : ViewModel() {

    // État UI mutable interne
    private val _uiState = MutableStateFlow(initialState)
    // État UI public immutable
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    // Canal pour les actions utilisateur (Intentions)
    private val actionChannel = Channel<A>(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            actionChannel.receiveAsFlow()
                // Debounce de 300ms pour éviter le spam d'actions (double-clics, etc.)
                // ATTENTION : Cela ajoute une latence de 300ms à toutes les actions traitées via ce canal.
                // Pour des actions immédiates (ex: navigation retour), on pourra bypasser ce canal ou réduire le délai.
                .debounce(300)
                .collect { action ->
                    handleAction(action)
                }
        }
    }

    /**
     * Soumettre une action utilisateur au ViewModel.
     */
    fun submitAction(action: A) {
        viewModelScope.launch {
            actionChannel.send(action)
        }
    }

    /**
     * Traiter l'action après le debounce.
     * À implémenter par les sous-classes.
     */
    protected abstract fun handleAction(action: A)

    /**
     * Mettre à jour l'état UI de manière thread-safe.
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.update(reducer)
    }
    
    /**
     * Obtenir l'état courant.
     */
    protected fun currentState(): S = _uiState.value
}
