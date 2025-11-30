package com.excell44.educam.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.ui.base.BaseViewModel
import com.excell44.educam.ui.base.UiAction
import com.excell44.educam.ui.base.UiState
import com.excell44.educam.ui.navigation.NavigationViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ✅ NAVIGATION COMMAND HANDLER
 * 
 * Composant réutilisable qui écoute les commandes de navigation émises
 * par un BaseViewModel et les transmet au NavigationViewModel pour exécution.
 * 
 * **ARCHITECTURE**:
 * ```
 * HomeViewModel.emitNavCommand() 
 *   → navigationCommands Flow
 *   → NavigationCommandHandler (ce composant)
 *   → NavigationViewModel.navigate()
 *   → NavController (thread-safe)
 * ```
 * 
 * **USAGE**:
 * ```kotlin
 * @Composable
 * fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
 *     NavigationCommandHandler(viewModel)
 *     // ... reste du UI
 * }
 * ```
 * 
 * @param viewModel Le ViewModel qui émet des NavCommands via emitNavCommand()
 * @param navigationViewModel Le ViewModel qui gère la navigation transactionnelle (injecté automatiquement)
 */
@Composable
fun <S : UiState, A : UiAction> NavigationCommandHandler(
    viewModel: BaseViewModel<S, A>,
    navigationViewModel: NavigationViewModel  // ✅ REMOVED default value - MUST be passed from parent
) {
    LaunchedEffect(viewModel) {
        viewModel.navigationCommands.collectLatest { command ->
            navigationViewModel.navigate(command)
        }
    }
}
