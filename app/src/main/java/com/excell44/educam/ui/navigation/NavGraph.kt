package com.excell44.educam.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.excell44.educam.ui.screen.auth.LoginScreen
import com.excell44.educam.ui.screen.auth.RegisterScreen
import com.excell44.educam.ui.screen.home.HomeScreen
import com.excell44.educam.ui.screen.problemsolver.ProblemSolverScreen
import com.excell44.educam.ui.screen.quiz.QuizScreen
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
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToQuiz = { navController.navigate(Screen.Quiz.route) },
                onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) },
                onNavigateToProblemSolver = { navController.navigate(Screen.ProblemSolver.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = { navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                } }
            )
        }
        composable(Screen.Quiz.route) {
            QuizScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Subjects.route) {
            SubjectsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.ProblemSolver.route) {
            ProblemSolverScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Profile.route) {
            com.excell44.educam.ui.screen.profile.ProfileScreen(
                onNavigateToBilan = { navController.navigate(Screen.Bilan.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Bilan.route) {
            com.excell44.educam.ui.screen.profile.BilanScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

