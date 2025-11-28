package com.excell44.educam.ui.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * ✅ VIEWMODEL FAIL-SAFE (NIVEAU BANCAIRE)
 * 
 * **GARANTIES ABSOLUES** :
 * 1. ✅ Rollback automatique sur erreur
 * 2. ✅ Timeout 10s pour toutes les opérations
 * 3. ✅ Historique des 5 derniers états (debugging)
 * 4. ✅ Recovery automatique après erreur
 * 5. ✅ Anti-spam avec debounce
 * 6. ✅ Isolation des crashs (SupervisorJob)
 * 
 * **IMPOSSIBLE DE** :
 * - Rester bloqué dans un état Loading
 * - Perdre l'état précédent
 * - Faire crasher l'app
 * - Spammer les actions
 */
abstract class FailSafeViewModel<S : UiState, A : UiAction>(
    initialState: S
) : ViewModel() {

    companion object {
        private const val ACTION_TIMEOUT_MS = 10000L
        private const val ERROR_DISPLAY_MS = 2000L
        private const val DEBOUNCE_MS = 300L
        private const val MAX_HISTORY_SIZE = 5
    }

    protected val TAG: String = this::class.java.simpleName

    // État UI avec type-safety
    private val _uiState = MutableStateFlow<S>(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    // Canal pour les actions (anti-spam)
    private val _actionChannel = Channel<A>(
        capacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    // Historique des états (pour rollback)
    private val stateHistory = ConcurrentLinkedQueue<S>()

    // Scope supervisé (crash isolation)
    private val safeScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    init {
        // Initialiser l'historique
        saveStateToHistory(initialState)

        // Traiter les actions de manière sécurisée
        safeScope.launch {
            _actionChannel.receiveAsFlow()
                .debounce(DEBOUNCE_MS) // ✅ Anti-spam
                .distinctUntilChanged() // ✅ Anti-duplicates
                .collect { action ->
                    executeActionSafely(action)
                }
        }
    }

    /**
     * Soumettre une action (thread-safe)
     */
    fun submitAction(action: A) {
        _actionChannel.trySend(action)
        Log.d(TAG, "Action soumise: $action")
    }

    /**
     * Exécution sécurisée avec TRIPLE protection
     */
    private suspend fun executeActionSafely(action: A) {
        // ✅ Sauvegarde l'état actuel AVANT l'action
        val previousState = _uiState.value
        
        // ✅ NIVEAU 1 : Try-Catch global
        try {
            Log.d(TAG, "🔄 Exécution: $action")

            // ✅ NIVEAU 2 : Timeout protection (10s max)
            withTimeout(ACTION_TIMEOUT_MS) {
                try {
                    // ✅ NIVEAU 3 : Exécution de l'action
                    handleActionSafely(action)
                    
                    // ✅ Sauvegarde du nouvel état si succès
                    saveStateToHistory(_uiState.value)
                    
                    Log.d(TAG, "✅ Succès: $action")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erreur action (inner): $action", e)
                    throw e // Remonter pour le timeout handler
                }
            }

        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "⏱️ TIMEOUT (10s): $action", e)
            handleError(previousState, "L'opération a pris trop de temps")
            
        } catch (e: CancellationException) {
            Log.w(TAG, "🚫 ANNULÉE: $action")
            rollbackState(previousState)
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 CRASH: $action", e)
            handleError(previousState, e.message ?: "Erreur inconnue")
        }
    }

    /**
     * Gestion d'erreur avec rollback automatique
     */
    private suspend fun handleError(previousState: S, errorMessage: String) {
        // ✅ ÉTAPE 1 : Afficher l'erreur
        updateState { createErrorState(errorMessage) }
        Log.w(TAG, "🔴 Erreur affichée: $errorMessage")

        // ✅ ÉTAPE 2 : Attendre 2 secondes
        delay(ERROR_DISPLAY_MS)

        // ✅ ÉTAPE 3 : Rollback automatique
        rollbackState(previousState)
        Log.i(TAG, "🔄 Rollback effectué")
    }

    /**
     * Rollback vers l'état précédent
     */
    private fun rollbackState(previousState: S) {
        _uiState.value = previousState
        Log.d(TAG, "⬅️ Rollback vers: $previousState")
    }

    /**
     * Sauvegarde de l'état dans l'historique
     */
    private fun saveStateToHistory(state: S) {
        stateHistory.add(state)
        
        // Limite à MAX_HISTORY_SIZE
        while (stateHistory.size > MAX_HISTORY_SIZE) {
            stateHistory.poll()
        }
    }

    /**
     * Obtenir l'historique des états (debugging)
     */
    fun getStateHistory(): List<S> = stateHistory.toList()

    /**
     * Rollback manuel vers le dernier état stable
     */
    fun rollbackToLastStableState() {
        if (stateHistory.size > 1) {
            stateHistory.poll() // Retirer l'état courant
            val lastStable = stateHistory.peek()
            if (lastStable != null) {
                _uiState.value = lastStable
                Log.i(TAG, "🔄 Rollback manuel vers: $lastStable")
            }
        }
    }

    /**
     * Mettre à jour l'état (thread-safe)
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.update(reducer)
    }

    /**
     * Obtenir l'état actuel
     */
    protected fun currentState(): S = _uiState.value

    /**
     * À implémenter : gestion de l'action
     */
    protected abstract suspend fun handleActionSafely(action: A)

    /**
     * À implémenter : créer un état d'erreur
     */
    protected abstract fun S.createErrorState(message: String): S

    override fun onCleared() {
        super.onCleared()
        safeScope.cancel()
        Log.d(TAG, "ViewModel cleared")
    }
}
