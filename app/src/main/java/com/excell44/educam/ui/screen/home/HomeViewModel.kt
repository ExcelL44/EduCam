package com.excell44.educam.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.excell44.educam.ui.base.BaseViewModel
import com.excell44.educam.ui.navigation.NavCommand
import com.excell44.educam.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour HomeScreen - suit le pattern fail-safe
 * Émet des NavCommands qui sont traitées par NavigationCommandHandler
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel<HomeState, HomeAction>(HomeState()) {


    override fun handleAction(action: HomeAction) { // ✅ Pas suspend - BaseViewModel handleAction
        when (action) {
            HomeAction.NavigateToQuiz -> {
                viewModelScope.launch { // ✅ Wrapper dans coroutine
                    emitNavCommand(NavCommand.NavigateTo(Screen.Quiz.route))
                }
            }
            HomeAction.NavigateToSubjects -> {
                viewModelScope.launch {
                    emitNavCommand(NavCommand.NavigateTo(Screen.Subjects.route))
                }
            }
            HomeAction.NavigateToProblemSolver -> {
                viewModelScope.launch {
                    emitNavCommand(NavCommand.NavigateTo(Screen.ProblemSolver.route))
                }
            }
            HomeAction.NavigateToProfile -> {
                viewModelScope.launch {
                    emitNavCommand(NavCommand.NavigateTo(Screen.Profile.route))
                }
            }
            HomeAction.NavigateToAdmin -> {
                viewModelScope.launch {
                    emitNavCommand(NavCommand.NavigateTo(Screen.AdminMenu.route))
                }
            }
            HomeAction.Logout -> {
                viewModelScope.launch {
                    // Le logout est géré par AuthViewModel, mais on peut émettre une commande de navigation
                    emitNavCommand(NavCommand.NavigateAndClear(Screen.Login.route))
                }
            }
        }
    }
}
