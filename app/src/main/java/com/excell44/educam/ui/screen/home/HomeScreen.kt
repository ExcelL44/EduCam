package com.excell44.educam.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.MenuBook
import com.excell44.educam.ui.components.FeatureCard
import com.excell44.educam.ui.components.NavigationCommandHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.ui.viewmodel.AuthViewModel
import com.excell44.educam.ui.viewmodel.AuthAction

import com.excell44.educam.ui.base.UiAction
import com.excell44.educam.ui.base.UiState

// États et Actions pour HomeScreen
data class HomeState(val isLoading: Boolean = false) : UiState
sealed class HomeAction : UiAction {
    object NavigateToQuiz : HomeAction()
    object NavigateToSubjects : HomeAction()
    object NavigateToProblemSolver : HomeAction()
    object NavigateToProfile : HomeAction()
    object NavigateToAdmin : HomeAction()
    object Logout : HomeAction()
}

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navigationViewModel: com.excell44.educam.ui.navigation.NavigationViewModel = hiltViewModel()  // ✅ Added parameter
 ) {
    // Gestionnaire de commandes de navigation
    NavigationCommandHandler(homeViewModel, navigationViewModel)  // ✅ Pass navigationViewModel

    val homeState by homeViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? com.excell44.educam.domain.model.AuthState.Authenticated)?.user

    // ✅ UPDATED: Profile-based access control
    val userMode = user?.getUserMode()
    val isAdmin = user?.role == "ADMIN"
    val isTrial = userMode == com.excell44.educam.data.model.UserMode.TRIAL
    val isActive = userMode == com.excell44.educam.data.model.UserMode.ACTIVE
    val isBetaT = userMode == com.excell44.educam.data.model.UserMode.BETA_T
    var showLockedDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bienvenue",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Prêt à réviser ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { homeViewModel.submitAction(HomeAction.NavigateToProfile) }) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    // Réinitialiser le thème au défaut
                    context.getSharedPreferences("bacx_prefs", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putInt("theme_index", 0)
                        .apply()

                    authViewModel.logout()
                    homeViewModel.submitAction(HomeAction.Logout)
                }) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Déconnexion",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Accueil: message amélioré
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Prépare ton examen avec Bac-X_237 AI en un clin d'œil ! 😉",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Quiz Card
        FeatureCard(
            title = "Quiz Adaptatif",
            description = "Testez vos connaissances avec des quiz personnalisés",
            icon = Icons.Outlined.Quiz,
            onClick = { homeViewModel.submitAction(HomeAction.NavigateToQuiz) },
            modifier = Modifier.fillMaxWidth(),
            enabled = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subjects Card - DISABLED for TRIAL users
        FeatureCard(
            title = "Banque de Sujets",
            description = "Accédez à une collection de sujets corrigés",
            icon = Icons.Outlined.MenuBook,
            onClick = { homeViewModel.submitAction(HomeAction.NavigateToSubjects) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isTrial, // TRIAL users cannot access
            onLockedClick = { showLockedDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Problem Solver Card (Smarty) - DISABLED for TRIAL users
        FeatureCard(
            title = "Smarty IA",
            description = "Résout vos exercices avec l'IA en un clin d'œil",
            icon = Icons.Default.CameraAlt,
            onClick = { homeViewModel.submitAction(HomeAction.NavigateToProblemSolver) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isTrial, // TRIAL users cannot access
            onLockedClick = { showLockedDialog = true }
        )

        // Admin Management Card (visible only for admins)
        if (isAdmin) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { homeViewModel.submitAction(HomeAction.NavigateToAdmin) }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Gérer",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gérer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Administration et contrôle système",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }

    if (showLockedDialog) {
        AlertDialog(
            onDismissRequest = { showLockedDialog = false },
            title = { Text("Fonctionnalité Premium 🌟") },
            text = {
                Text(
                    if (isTrial) {
                        "Cette fonctionnalité n'est disponible que pour les utilisateurs Premium.\n\n" +
                        "💎 Passez Premium pour seulement 2500 FCFA/mois et débloquez :\n" +
                        "• Smarty IA - Résolution d'exercices\n" +
                        "• Banque de sujets corrigés\n" +
                        "• Quiz illimités\n" +
                        "• Support prioritaire"
                    } else {
                        "Cette fonctionnalité est réservée aux utilisateurs premium. Voulez-vous passer à un abonnement ?"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLockedDialog = false
                    // ✅ Track conversion attempt
                    com.excell44.educam.util.Logger.i(
                        "HomeScreen", 
                        "User clicked upgrade button (TRIAL -> Premium conversion attempt)"
                    )
                    // Navigate to profile for upgrade options
                    homeViewModel.submitAction(HomeAction.NavigateToProfile)
                }) {
                    Text(if (isTrial) "Passer Premium (2500 FCFA/mois)" else "Voir les options")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showLockedDialog = false
                    // ✅ Track dismissal
                    com.excell44.educam.util.Logger.d("HomeScreen", "User dismissed premium upgrade dialog")
                }) { 
                    Text("Plus tard") 
                }
            }
        )
    }
}


// FeatureCard is now provided by `com.excell44.educam.ui.components.FeatureCard`
