package com.excell44.educam.ui.navigation

import androidx.compose.runtime.Composable
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
    postSplashDestination: String = startDestination
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            // Lazy import to avoid cycles
            com.excell44.educam.ui.screen.splash.SplashScreen(
                postSplashDestination = postSplashDestination,
                onNavigate = { dest ->
                    navController.navigateSafe(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigateSafe(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                } },
                onNavigateToRegister = { navController.navigateSafe(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigateSafe(Screen.Home.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                } },
                onNavigateToLogin = { navController.popBackStackSafe() }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToQuiz = { navController.navigateSafe(Screen.Quiz.route) },
                onNavigateToSubjects = { navController.navigateSafe(Screen.Subjects.route) },
                onNavigateToProblemSolver = { navController.navigateSafe(Screen.ProblemSolver.route) },
                onNavigateToProfile = { navController.navigateSafe(Screen.Profile.route) },
                onNavigateToAdmin = { navController.navigateSafe(Screen.AdminMenu.route) },
                onLogout = { navController.navigateSafe(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                } }
            )
        }
        composable(Screen.Quiz.route) {
            com.excell44.educam.ui.screen.quiz.QuizFlow(
                onQuizComplete = { navController.popBackStackSafe() }
            )
        }
        composable(Screen.Subjects.route) {
            SubjectsScreen(onNavigateBack = { navController.popBackStackSafe() })
        }
        composable(Screen.ProblemSolver.route) {
            ProblemSolverScreen(onNavigateBack = { navController.popBackStackSafe() })
        }
        composable(Screen.Profile.route) {
            com.excell44.educam.ui.screen.profile.ProfileScreen(
                onNavigateToBilan = { navController.navigateSafe(Screen.Bilan.route) },
                onNavigateBack = { navController.popBackStackSafe() }
            )
        }
        composable(Screen.Bilan.route) {
            com.excell44.educam.ui.screen.profile.BilanScreen(onNavigateBack = { navController.popBackStackSafe() })
        }
        composable(Screen.AdminMenu.route) {
            com.excell44.educam.ui.screen.admin.AdminMenuScreen(
                onNavigateBack = { navController.popBackStackSafe() },
                onNavigateToRemoteDashboard = { navController.navigateSafe(Screen.RemoteDashboard.route) },
                onNavigateToLocalDatabase = { navController.navigateSafe(Screen.LocalDatabase.route) }
            )
        }
        composable(Screen.RemoteDashboard.route) {
            com.excell44.educam.ui.screen.admin.RemoteDashboardScreen(
                onNavigateBack = { navController.popBackStackSafe() }
            )
        }
        composable(Screen.LocalDatabase.route) {
            com.excell44.educam.ui.screen.admin.LocalDatabaseScreen(
                onNavigateBack = { navController.popBackStackSafe() }
            )
        }
    }
}
