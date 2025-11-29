package com.excell44.educam.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.excell44.educam.ui.screen.auth.LoginScreen
import com.excell44.educam.ui.screen.auth.RegisterScreen
import com.excell44.educam.ui.screen.home.HomeScreen
import com.excell44.educam.ui.screen.problemsolver.ProblemSolverScreen
import com.excell44.educam.ui.screen.quiz.QuizFlow
import com.excell44.educam.ui.screen.subjects.SubjectsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object Subjects : Screen("subjects")
    object ProblemSolver : Screen("problem_solver")
    object Profile : Screen("profile")
    object Bilan : Screen("bilan")
    object AdminMenu : Screen("admin_menu")
    object RemoteDashboard : Screen("remote_dashboard")
    object LocalDatabase : Screen("local_database")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    postSplashDestination: String = startDestination,
    navigationViewModel: NavigationViewModel = hiltViewModel(),
    authViewModel: com.excell44.educam.ui.viewmodel.AuthViewModel = hiltViewModel() // ✅ Injecter AuthViewModel
) {
    // Attach NavController to NavigationViewModel
    LaunchedEffect(navController) {
        navigationViewModel.setNavController(navController)
    }

    // Observe navigation state (optional, for debugging)
    val navState by navigationViewModel.navigationState.collectAsState()

    // ✅ PHASE 2.3: SINGLE SOURCE OF TRUTH - Navigation Auth Centralisée
    val authState by authViewModel.uiState.collectAsState()
    
    LaunchedEffect(authState.isLoggedIn) {
        val currentRoute = navController.currentDestination?.route
        android.util.Log.d("NavGraph", "Auth changed: isLoggedIn=${authState.isLoggedIn}, currentRoute=$currentRoute")
        
        // Si user se connecte depuis Login/Register → Aller à Home
        if (authState.isLoggedIn && currentRoute in listOf(Screen.Login.route, Screen.Register.route)) {
            navigationViewModel.navigate(
                NavCommand.NavigateTo(
                    route = Screen.Home.route,
                    popUpTo = Screen.Login.route,
                    inclusive = true
                )
            )
        }
        // Si user se déconnecte depuis n'importe quel écran → Aller à Login
        else if (!authState.isLoggedIn && currentRoute !in listOf(Screen.Login.route, Screen.Splash.route)) {
            navigationViewModel.navigate(
                NavCommand.NavigateAndClear(Screen.Login.route)
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            // Lazy import to avoid cycles
            com.excell44.educam.ui.screen.splash.SplashScreen(
                postSplashDestination = postSplashDestination,
                onNavigate = { dest ->
                    navigationViewModel.navigate(
                        NavCommand.NavigateTo(
                            route = dest,
                            popUpTo = Screen.Splash.route,
                            inclusive = true
                        )
                    )
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {}, // ✅ No-op: Navigation gérée par LaunchedEffect ci-dessus
                onNavigateToRegister = {
                    navigationViewModel.navigate(NavCommand.NavigateTo(Screen.Register.route))
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {}, // ✅ No-op: Navigation gérée par LaunchedEffect ci-dessus
                onNavigateToLogin = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
        composable(Screen.Home.route) {
            com.excell44.educam.ui.screen.home.HomeScreen()
        }
        composable(Screen.Quiz.route) {
            com.excell44.educam.ui.screen.quiz.QuizFlow(
                onQuizComplete = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
        composable(Screen.Subjects.route) {
            SubjectsScreen(
                onNavigateBack = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
        composable(Screen.ProblemSolver.route) {
            ProblemSolverScreen(
                onNavigateBack = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
        composable(Screen.Profile.route) {
            com.excell44.educam.ui.screen.profile.ProfileScreen(
                onNavigateToBilan = {
                    navigationViewModel.navigate(NavCommand.NavigateTo(Screen.Bilan.route))
                },
                onNavigateBack = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
        composable(Screen.Bilan.route) {
            com.excell44.educam.ui.screen.profile.BilanScreen(
                onNavigateBack = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
        composable(Screen.AdminMenu.route) {
            com.excell44.educam.ui.screen.admin.AdminMenuScreen(
                onNavigateBack = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                },
                onNavigateToRemoteDashboard = {
                    navigationViewModel.navigate(NavCommand.NavigateTo(Screen.RemoteDashboard.route))
                },
                onNavigateToLocalDatabase = {
                    navigationViewModel.navigate(NavCommand.NavigateTo(Screen.LocalDatabase.route))
                }
            )
        }
        composable(Screen.RemoteDashboard.route) {
            com.excell44.educam.ui.screen.admin.RemoteDashboardScreen(
                onNavigateBack = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
        composable(Screen.LocalDatabase.route) {
            com.excell44.educam.ui.screen.admin.LocalDatabaseScreen(
                onNavigateBack = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
    }
}
