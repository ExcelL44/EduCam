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
import com.excell44.educam.ui.screen.chat.ChatScreen
import com.excell44.educam.ui.screen.home.HomeScreen
import com.excell44.educam.ui.screen.problemsolver.ProblemSolverScreen
import com.excell44.educam.ui.screen.quiz.QuizFlow
import com.excell44.educam.ui.screen.subjects.SubjectsScreen
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object Subjects : Screen("subjects")
    object ProblemSolver : Screen("problem_solver")
    object Chat : Screen("chat")
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
    authViewModel: com.excell44.educam.ui.viewmodel.AuthViewModel = hiltViewModel(), // ✅ Injecter AuthViewModel
    mainViewModel: com.excell44.educam.ui.viewmodel.MainViewModel = hiltViewModel() // ✅ Injecter MainViewModel pour Thèmes
) {
    // ⚠️ NavController is now attached in MainActivity via SideEffect
    // This removes the race condition where NavGraph children try to navigate before LaunchedEffect runs
    
    // Observe navigation state (optional, for debugging)
    val navState by navigationViewModel.navigationState.collectAsState()

    // ✅ PHASE 2.3: SINGLE SOURCE OF TRUTH - Navigation Auth Centralisée
    val authState by authViewModel.authState.collectAsState()

    val isLoggedIn = authState is com.excell44.educam.domain.model.AuthState.Authenticated
    val user = (authState as? com.excell44.educam.domain.model.AuthState.Authenticated)?.user
    val userMode = user?.getUserMode()
    val isTrial = userMode == com.excell44.educam.data.model.UserMode.TRIAL
    val isAdmin = user?.role == "ADMIN"

    // ✅ CORRECTIF P1: Vérifier expiration trial PASSIVE
    var showTrialExpiredDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(authState) {
        if (authState is com.excell44.educam.domain.model.AuthState.Authenticated) {
            val currentUser = authState.user
            
            // Vérifier si user est PASSIVE avec trial expiré
            if (currentUser.role == "PASSIVE" && 
                currentUser.trialExpiresAt != null && 
                currentUser.trialExpiresAt < System.currentTimeMillis()) {
                
                android.util.Log.w("NavGraph", "⚠️ TRIAL EXPIRED for user: ${currentUser.pseudo}")
                showTrialExpiredDialog = true
            }
        }
    }
    
    // Dialog expiration trial
    if (showTrialExpiredDialog) {
        AlertDialog(
            onDismissRequest = { /* Empêcher fermeture */ },
            icon = {
                Icon(
                    androidx.compose.material.icons.Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Période d'essai terminée",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Votre essai gratuit de 7 jours est terminé.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Passez Premium pour continuer à utiliser toutes les fonctionnalités de Bac-X_237 ! 🚀",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        // TODO: Navigate to payment screen
                        android.util.Log.d("NavGraph", "User clicked 'Passer Premium'")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("🌟 Passer Premium")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTrialExpiredDialog = false
                        authViewModel.logout()
                    }
                ) {
                    Text("Se déconnecter")
                }
            }
        )
    }

    android.util.Log.d("🟢 NAV_GRAPH", "📊 Auth state updated: isLoggedIn=$isLoggedIn, userMode=$userMode, userRole=${user?.role}")
    
    // ✅ NAVIGATION IMMEDIATE sur changement d'état d'authentification
    LaunchedEffect(isLoggedIn) {
        val currentRoute = navController.currentDestination?.route
        android.util.Log.d("NavGraph", "🔥 Auth state changed: isLoggedIn=$isLoggedIn, currentRoute=$currentRoute, authState=${authState::class.simpleName}")

        // ⚠️ CRITICAL: Attendre que NavController soit prêt avant de naviguer
        // Mais pas de delay arbitraire - vérifier l'état réel
        if (navController.currentBackStackEntry == null) {
            android.util.Log.w("NavGraph", "⚠️ NavController not ready yet - waiting for next auth state change")
            return@LaunchedEffect
        }

        // Si user se connecte depuis n'importe quel écran d'auth → Aller à Home
        if (isLoggedIn && currentRoute in listOf(Screen.Login.route, Screen.Register.route, Screen.Splash.route)) {
            android.util.Log.d("NavGraph", "✅ Navigating to Home after login (immediate)")
            navigationViewModel.navigate(
                NavCommand.NavigateTo(
                    route = Screen.Home.route,
                    popUpTo = Screen.Login.route, // ✅ FIX: Pop jusqu'au Login au lieu de Splash
                    inclusive = true,
                    singleTop = true // ✅ FIX: Empêcher de multiples instances de Home
                )
            )
        }
        // Si user se déconnecte depuis n'importe quel écran → Aller à Login
        else if (!isLoggedIn && currentRoute !in listOf(Screen.Login.route, Screen.Register.route, Screen.Splash.route)) {
            android.util.Log.d("NavGraph", "🔄 Navigating back to Login after logout")
            navigationViewModel.navigate(
                NavCommand.NavigateAndClear(Screen.Login.route)
            )
        } else {
            android.util.Log.d("NavGraph", "⏭️ No navigation action needed: isLoggedIn=$isLoggedIn, route=$currentRoute")
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
                onLoginSuccess = {
                    // ✅ FIX: Navigation explicite après login réussi
                    navigationViewModel.navigate(
                        NavCommand.NavigateTo(
                            route = Screen.Home.route,
                            popUpTo = Screen.Login.route,
                            inclusive = true,
                            singleTop = true
                        )
                    )
                },
                onNavigateToRegister = {
                    navigationViewModel.navigate(NavCommand.NavigateTo(Screen.Register.route))
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // ✅ FIX: Navigation explicite après inscription réussie
                    navigationViewModel.navigate(
                        NavCommand.NavigateTo(
                            route = Screen.Home.route,
                            popUpTo = Screen.Login.route,
                            inclusive = true,
                            singleTop = true
                        )
                    )
                },
                onNavigateToLogin = {
                    navigationViewModel.navigate(NavCommand.PopBack)
                }
            )
        }
        composable(Screen.Home.route) {
            com.excell44.educam.ui.screen.home.HomeScreen(
                navigationViewModel = navigationViewModel,  // ✅ Pass shared instance
                mainViewModel = mainViewModel  // ✅ Pass MainViewModel for network observer
            )
        }
        composable(Screen.Quiz.route) {
            com.excell44.educam.ui.screen.quiz.QuizFlowCoordinator(
                onNavigateBack = {
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
                },
                navigationViewModel = navigationViewModel
            )
        }
        composable(Screen.Chat.route) {
            com.excell44.educam.ui.screen.chat.ChatScreen(
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
                },
                viewModel = authViewModel, // ✅ Pass shared AuthViewModel
                mainViewModel = mainViewModel // ✅ Pass shared MainViewModel
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
