package com.excell44.educam.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.ui.base.BaseViewModel
import com.excell44.educam.ui.base.UiAction
import com.excell44.educam.ui.base.UiState
import com.excell44.educam.ui.navigation.NavigationViewModel

/**
 * Composable helper qui connecte automatiquement les commandes de navigation
 * d'un ViewModel au NavigationViewModel.
 * 
 * **USAGE** :
 * ```kotlin
 * @Composable
 * fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
 *     NavigationCommandHandler(viewModel)
 *     
 *     // Reste du composable...
 * }
 * ```
 * 
 * @param viewModel Le ViewModel qui émet des NavCommands
 * @param navigationViewModel Le NavigationViewModel qui exécute les commandes
 */
@Composable
fun <S : UiState, A : UiAction> NavigationCommandHandler(
    viewModel: BaseViewModel<S, A>,
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.navigationCommands.collect { command ->
            navigationViewModel.navigate(command)
        }
    }
}

/**
 * Extension pour faciliter la vérification de l'état de navigation
 * 
 * **USAGE** :
 * ```kotlin
 * val navigationViewModel: NavigationViewModel = hiltViewModel()
 * val isNavigating by navigationViewModel.isNavigating()
 * 
 * Button(
 *     enabled = !isNavigating,
 *     onClick = { ... }
 * )
 * ```
 */
@Composable
fun NavigationViewModel.isNavigating(): Boolean {
    val state by navigationState.collectAsState()
    return state == com.excell44.educam.ui.navigation.NavigationState.NAVIGATING
}

/**
 * Extension pour faciliter la vérification de l'état IDLE
 */
@Composable
fun NavigationViewModel.isIdle(): Boolean {
    val state by navigationState.collectAsState()
    return state == com.excell44.educam.ui.navigation.NavigationState.IDLE
}

/**
 * Extension pour faciliter la vérification de l'état ERROR
 */
@Composable
fun NavigationViewModel.isError(): Boolean {
    val state by navigationState.collectAsState()
    return state == com.excell44.educam.ui.navigation.NavigationState.ERROR
}
