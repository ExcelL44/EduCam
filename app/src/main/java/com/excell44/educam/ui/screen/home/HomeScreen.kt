package com.excell44.educam.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

// États et Actions pour HomeScreen
data class HomeState(val isGuest: Boolean = false)
sealed class HomeAction {
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
    authViewModel: AuthViewModel = hiltViewModel()
 ) {
    // Gestionnaire de commandes de navigation
    NavigationCommandHandler(homeViewModel)

    val homeState by homeViewModel.uiState.collectAsState()
    val isGuest = authViewModel.getAccountType() == "GUEST"
    val isAdmin = authViewModel.getAccountType() == "ADMIN"
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    // Réinitialiser le thème au défaut
                    context.getSharedPreferences("educam_prefs", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putInt("theme_index", 0)
                        .apply()

                    authViewModel.submitAction(AuthAction.Logout)
                    homeViewModel.submitAction(HomeAction.Logout)
                }) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Déconnexion",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Accueil: message amélioré
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Prépare ton examen avec EduCam AI en un clin d'œil",
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

        // Subjects Card
        FeatureCard(
            title = "Banque de Sujets",
            description = "Accédez à une collection de sujets corrigés",
            icon = Icons.Outlined.MenuBook,
            onClick = { homeViewModel.submitAction(HomeAction.NavigateToSubjects) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGuest,
            onLockedClick = { showLockedDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Problem Solver Card (Smarty)
        FeatureCard(
            title = "Smarty IA",
            description = "Résout vos exercices avec l'IA en un clin d'œil",
            icon = Icons.Default.CameraAlt,
            onClick = { homeViewModel.submitAction(HomeAction.NavigateToProblemSolver) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGuest,
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
            title = { Text("Fonctionnalité réservée") },
            text = { Text("Cette fonctionnalité est réservée aux utilisateurs inscrits. Voulez-vous créer un compte ?") },
            confirmButton = {
                TextButton(onClick = {
                    showLockedDialog = false
                    // navigate to register
                    // using nav from params isn't available here, so call onNavigateToProfile as a conservative path
                    homeViewModel.submitAction(HomeAction.NavigateToProfile)
                }) { Text("S'inscrire") }
            },
            dismissButton = {
                TextButton(onClick = { showLockedDialog = false }) { Text("Fermer") }
            }
        )
    }
}


// FeatureCard is now provided by `com.excell44.educam.ui.components.FeatureCard`
