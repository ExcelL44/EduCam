package com.excell44.educam.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.data.model.UserMode
import com.excell44.educam.ui.components.BetaReferralWidget
import com.excell44.educam.ui.components.UserModeIndicator
import com.excell44.educam.ui.viewmodel.AuthViewModel
import com.excell44.educam.ui.viewmodel.BetaReferralViewModel
import com.excell44.educam.ui.viewmodel.UiState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import com.excell44.educam.ui.util.screenPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToBilan: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel, // ✅ Shared instance passed from NavGraph
    mainViewModel: com.excell44.educam.ui.viewmodel.MainViewModel, // ✅ Shared instance for Theme
    betaReferralViewModel: BetaReferralViewModel = hiltViewModel() // Local instance OK for specific feature
) {
    val authState by viewModel.authState.collectAsState()
    val user = (authState as? com.excell44.educam.domain.model.AuthState.Authenticated)?.user
    val referralState by betaReferralViewModel.referralState.collectAsState()
    
    // Theme state from MainViewModel
    val currentThemeIndex by mainViewModel.themeIndex.collectAsState()
    
    var pseudo by remember { mutableStateOf("") }
    var userMode by remember { mutableStateOf<UserMode?>(null) }

    // Fetch user mode and data
    LaunchedEffect(user) {
        if (user != null) {
            userMode = user.getUserMode()
            // Derive pseudo from pseudo field (contains email format)
            pseudo = user.pseudo.substringBefore("@")
        } else {
            userMode = UserMode.TRIAL  // Default fallback since GUEST is removed
            pseudo = "Utilisateur"
        }
    }

    val isTrial = userMode == UserMode.TRIAL
    val isReadOnly = isTrial // Trial users have limited access
    val context = LocalContext.current

    // Fetch trial and guest info from AuthStateManager
    val authStateManager = remember { com.excell44.educam.util.AuthStateManager(context) }

    // Calculate days remaining for trial from User.trialExpiresAt
    val daysRemaining = user?.trialExpiresAt?.let { expiresAt ->
        val remaining = expiresAt - System.currentTimeMillis()
        val daysLeft = (remaining / (24 * 60 * 60 * 1000L)).coerceAtLeast(0)
        daysLeft
    } ?: 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Retour",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // ✅ Make Profile scrollable too
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        // ✅ CORRECTIF P1: Badge Mode Passif pour utilisateurs TRIAL/PASSIVE
        if (user?.role == "PASSIVE" || userMode == UserMode.TRIAL) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            androidx.compose.material.icons.Icons.Filled.Timer,
                            contentDescription = "Trial",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mode Passif (Essai)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (user?.trialExpiresAt != null) {
                                val remaining = user.trialExpiresAt - System.currentTimeMillis()
                                val daysLeft = (remaining / (24 * 60 * 60 * 1000L)).coerceAtLeast(0)
                                val hoursLeft = ((remaining % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)).coerceAtLeast(0)
                                
                                Text(
                                    text = if (daysLeft > 0) {
                                        "$daysLeft jour${if (daysLeft > 1) "s" else ""} restant${if (daysLeft > 1) "s" else ""}"
                                    } else if (hoursLeft > 0) {
                                        "$hoursLeft heure${if (hoursLeft > 1) "s" else ""} restant${if (hoursLeft > 1) "s" else ""}"
                                    } else {
                                        "Expire bientôt"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (daysLeft <= 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    
                    // Badge "3 Quiz/jour"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "3 Quiz/jour",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // User Mode Indicator
        userMode?.let {
            UserModeIndicator(userMode = it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = "Couleur du thème", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // Enhanced theme Material3 choices
        val availableThemes = com.excell44.educam.ui.theme.BacXThemes
        var isAnimating by remember { mutableStateOf(false) }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Animated theme selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
            ) {
                availableThemes.forEachIndexed { index, theme ->
                    val isSelected = currentThemeIndex == index
                    // Créer un gradient simple pour visualisation du thème
                    val themeGradient = Brush.horizontalGradient(
                        colors = listOf(
                            theme.colors.primary,
                            theme.colors.secondary
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 64.dp else 56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(themeGradient)
                            .clickable(enabled = !isReadOnly) {
                                if (currentThemeIndex != index) {
                                    isAnimating = true
                                    // ✅ Use MainViewModel to update theme reactively
                                    mainViewModel.updateTheme(index)
                                }
                            }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Subtle animation overlay
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .alpha(0.3f)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.6f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Theme description
            if (currentThemeIndex < availableThemes.size) {
                val currentTheme = availableThemes[currentThemeIndex]
                Text(
                    text = currentTheme.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.alpha(0.8f)
                )
                
                Text(
                    text = currentTheme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Reset animation state after theme change
        LaunchedEffect(currentThemeIndex) {
            if (isAnimating) {
                kotlinx.coroutines.delay(500)
                isAnimating = false
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Beta Referral Widget (NOUVEAU)
        when (val state = referralState) {
            is UiState.Loading -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Erreur: ${state.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is UiState.Success -> {
                BetaReferralWidget(
                    status = state.data,
                    onGiftClick = betaReferralViewModel::onGiftButtonClicked
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = pseudo,
            onValueChange = { if (it.length <= 15 && !isReadOnly) pseudo = it },
            label = { Text("Pseudo") },
            singleLine = true,
            enabled = !isReadOnly,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mode-specific UI
        when (userMode) {
            UserMode.ACTIVE -> {
                com.excell44.educam.ui.components.PrimaryButton(
                    onClick = { /* Navigate to BetaT registration */ },
                    text = "🌟 Devenir Beta Testeur",
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Contribue au développement de l'application et devient Beta Testeur !!!",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            UserMode.BETA_T -> {
                // Promo code progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Code Promo: BETA123", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.3f, // TODO: Fetch from backend
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("3/10 inscriptions", style = MaterialTheme.typography.bodySmall)
                }
            }
            UserMode.TRIAL -> {
                // Display trial period countdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Période d'essai: $daysRemaining jours restants",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (daysRemaining <= 2) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurface
                    )
                    LinearProgressIndicator(
                        progress = (daysRemaining / 7f),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
            UserMode.ADMIN -> {
                // Admin capabilities
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Mode Super User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Connexion à distance sur tous les comptes",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Gestion des utilisateurs et contenus",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Accès aux statistiques globales",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    com.excell44.educam.ui.components.PrimaryButton(
                        onClick = { /* TODO: Implement admin panel */ },
                        text = "⚙️ Panneau d'administration",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            else -> {
                Text(text = "Statut du compte: ${user?.role ?: "UNKNOWN"}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        com.excell44.educam.ui.components.PrimaryButton(
            onClick = onNavigateToBilan,
            text = "📊 Bilan des activités",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        if (!isReadOnly) {
            com.excell44.educam.ui.components.SecondaryButton(
                onClick = { /* Edit profile save logic could go here */ },
                text = "💾 Enregistrer",
                modifier = Modifier.fillMaxWidth()
            )
        }
        }
    }
}
