package com.excell44.educam.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.excell44.educam.ui.components.UserModeIndicator
import com.excell44.educam.ui.viewmodel.AuthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToBilan: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val accountType = viewModel.getAccountType()
    var pseudo by remember { mutableStateOf("") }
    var userMode by remember { mutableStateOf<UserMode?>(null) }

    // Fetch user mode
    LaunchedEffect(Unit) {
        userMode = when (accountType) {
            "TRIAL" -> UserMode.TRIAL
            "ACTIVE" -> UserMode.ACTIVE
            "BETA" -> UserMode.BETA_T
            "ADMIN" -> UserMode.ADMIN
            else -> UserMode.GUEST
        }

        viewModel.getProfileJsonForCurrentUser()?.let { json ->
            val regex = "\"pseudo\":\"(.*?)\"".toRegex()
            val match = regex.find(json)
            if (match != null) pseudo = match.groupValues[1]
        }
    }

    val isReadOnly = userMode == UserMode.GUEST
    val context = LocalContext.current

    // Fetch trial and guest info from AuthStateManager
    val authStateManager = remember { com.excell44.educam.util.AuthStateManager(context) }
    val guestAttemptsRemaining = remember { mutableStateOf(authStateManager.getGuestAttemptsRemaining()) }
    val trialStartDate = remember { mutableStateOf(authStateManager.getTrialStartDate()) }

    // Calculate days remaining for trial
    val daysRemaining = if (trialStartDate.value > 0L) {
        val elapsed = System.currentTimeMillis() - trialStartDate.value
        val daysElapsed = elapsed / (24 * 60 * 60 * 1000L)
        (7 - daysElapsed).coerceAtLeast(0)
    } else 7L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        // User Mode Indicator
        userMode?.let {
            UserModeIndicator(userMode = it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = "Couleur du thème", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // Enhanced theme Material3 choices
        val availableThemes = com.excell44.educam.ui.theme.BacXThemes
        val prefs = remember { context.getSharedPreferences("bacx_prefs", android.content.Context.MODE_PRIVATE) }
        var selectedThemeIndex by remember {
            mutableStateOf(prefs.getInt("theme_index", 0))
        }
        var isAnimating by remember { mutableStateOf(false) }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Animated theme selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
            ) {
                availableThemes.forEachIndexed { index, theme ->
                    val isSelected = selectedThemeIndex == index
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
                                selectedThemeIndex = index
                                isAnimating = true
                                // Sauvegarder le thème sélectionné
                                prefs.edit().putInt("theme_index", index).apply()
                                // Force recompose by recreating activity (theme will update)
                                (context as? android.app.Activity)?.recreate()
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
            if (selectedThemeIndex < availableThemes.size) {
                val currentTheme = availableThemes[selectedThemeIndex]
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
        LaunchedEffect(selectedThemeIndex) {
            if (isAnimating) {
                kotlinx.coroutines.delay(500)
                isAnimating = false
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
            UserMode.GUEST -> {
                // Display attempts remaining
                Text(
                    text = "Essais restants: ${guestAttemptsRemaining.value}/3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (guestAttemptsRemaining.value == 0) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurface
                )
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
                Text(text = "Statut du compte: $accountType", style = MaterialTheme.typography.bodyMedium)
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
