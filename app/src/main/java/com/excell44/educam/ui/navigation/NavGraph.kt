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
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object Subjects : Screen("subjects")
    object ProblemSolver : Screen("problem_solver")
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = Screen.Login.route) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
    }
}

