package com.excell44.educam.ui.screen.example

import com.excell44.educam.data.repository.FailSafeRepositoryHelper
import com.excell44.educam.ui.base.FailSafeViewModel
import com.excell44.educam.ui.base.UiAction
import com.excell44.educam.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ✅ EXEMPLE DE VIEWMODEL FAIL-SAFE
 * 
 * **Montre** :
 * - Comment hériter de FailSafeViewModel
 * - Comment gérer les actions de manière sécurisée
 * - Comment créer un état d'erreur
 * - Comment utiliser le repository fail-safe
 * 
 * **GARANTIES** :
 * - ✅ Rollback automatique sur erreur
 * - ✅ Timeout 10s maximum
 * - ✅ Jamais d'état bloqué
 * - ✅ Logs complets pour debugging
 */

// 1️⃣ Définir l'état UI
data class ExampleUiState(
    val isLoading: Boolean = false,
    val data: String? = null,
    val error: String? = null,
    val itemsList: List<String> = emptyList()
) : UiState

// 2️⃣ Définir les actions possibles
sealed class ExampleAction : UiAction {
    object LoadData : ExampleAction()
    data class DeleteItem(val itemId: String) : ExampleAction()
    object Refresh : ExampleAction()
}

// 3️⃣ Implémenter le ViewModel
@HiltViewModel
class ExampleViewModel @Inject constructor(
    // Inject les dépendances nécessaires
    private val failSafe: FailSafeRepositoryHelper
    // private val repository: ExampleRepository
) : FailSafeViewModel<ExampleUiState, ExampleAction>(
    initialState = ExampleUiState()
) {

    /**
     * 4️⃣ Implémenter handleActionSafely
     * 
     * ⚠️ Cette fonction est DÉJÀ protégée par :
     * - Try-catch triple niveau
     * - Timeout 10s
     * - Rollback automatique sur erreur
     * 
     * Tu n'as PAS besoin d'ajouter try-catch ici !
     */
    override suspend fun handleActionSafely(action: ExampleAction) {
        when (action) {
            is ExampleAction.LoadData -> loadData()
            is ExampleAction.DeleteItem -> deleteItem(action.itemId)
            is ExampleAction.Refresh -> refreshData()
        }
    }

    /**
     * 5️⃣ Implémenter createErrorState
     * 
     * Définit comment créer un état d'erreur à partir de l'état actuel
     */
    override fun ExampleUiState.createErrorState(message: String): ExampleUiState {
        return copy(
            isLoading = false,
            error = message
        )
    }

    // 6️⃣ Fonctions privées pour chaque action
    private suspend fun loadData() {
        // ✅ Mettre à jour l'état : Loading
        updateState { copy(isLoading = true, error = null) }

        // ✅ Utiliser failSafe pour l'opération
        val result = failSafe.executeSafely(
            operationName = "loadData",
            requiresMutex = false, // Pas besoin de mutex
            retries = 2 // Retry 2 fois en cas d'échec
        ) {
            // Simuler un appel API
            // val data = repository.getData()
            "Hello from fail-safe API!"
        }

        // ✅ Gérer le résultat
        result
            .onSuccess { data ->
                updateState { 
                    copy(
                        isLoading = false,
                        data = data,
                        error = null
                    ) 
                }
            }
            .onFailure { error ->
                // L'erreur sera gérée automatiquement par FailSafeViewModel
                // Mais on peut aussi log ou faire des traitements custom
                throw error
            }
    }

    private suspend fun deleteItem(itemId: String) {
        // ✅ Cette opération est critique → utiliser mutex
        val result = failSafe.executeSafely(
            operationName = "deleteItem_$itemId",
            requiresMutex = true, // ✅ Une seule suppression à la fois
            retries = 0 // Pas de retry pour une suppression
        ) {
            // Simuler suppression
            // repository.deleteItem(itemId)
            true
        }

        result.onSuccess {
            // Retirer l'item de la liste
            updateState { 
                copy(itemsList = itemsList.filter { it != itemId }) 
            }
        }
    }

    private suspend fun refreshData() {
        // ✅ Même pattern que loadData
        updateState { copy(isLoading = true) }

        failSafe.executeSafely("refreshData", retries = 1) {
            listOf("Item 1", "Item 2", "Item 3")
        }.onSuccess { items ->
            updateState { 
                copy(
                    isLoading = false,
                    itemsList = items
                ) 
            }
        }
    }

    /**
     * 7️⃣ Fonctions publiques pour le UI
     */
    fun clearError() {
        updateState { copy(error = null) }
    }
}

/**
 * 8️⃣ USAGE DANS LE COMPOSABLE
 * 
 * ```kotlin
 * @Composable
 * fun ExampleScreen(
 *     viewModel: ExampleViewModel = hiltViewModel()
 * ) {
 *     val state by viewModel.uiState.collectAsState()
 *     
 *     // ✅ UI réagit à l'état
 *     when {
 *         state.isLoading -> LoadingIndicator()
 *         state.error != null -> ErrorMessage(state.error)
 *         else -> DataDisplay(state.data)
 *     }
 *     
 *     // ✅ Soumettre des actions
 *     Button(onClick = { viewModel.submitAction(ExampleAction.LoadData) }) {
 *         Text("Charger les données")
 *     }
 * }
 * ```
 */
