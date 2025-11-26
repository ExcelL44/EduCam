package com.excell44.educam.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.data.model.UserMode
import com.excell44.educam.ui.components.UserModeIndicator
import com.excell44.educam.ui.viewmodel.AuthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment

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
            "TRIAL" -> UserMode.PASSIVE
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Profil") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Mode Indicator
        userMode?.let {
            UserModeIndicator(userMode = it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = "Couleur du thème", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // 7 theme color choices (from ThemeManager)
        val themeManager = remember { com.excell44.educam.util.ThemeManager(context) }
        val themeColors = com.excell44.educam.util.ThemeManager.ThemeColor.values()
        var selectedThemeIndex by remember { mutableStateOf(themeManager.getThemeColorIndex()) }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            themeColors.forEachIndexed { index, themeColor ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(themeColor.color)
                        .clickable(enabled = !isReadOnly) { 
                            selectedThemeIndex = index
                            themeManager.saveThemeColor(index)
                            // Force recompose by recreating activity (theme will update)
                            (context as? android.app.Activity)?.recreate()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedThemeIndex == index) {
                        Box(modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White))
                    }
                }
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
                Button(
                    onClick = { /* Navigate to BetaT registration */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // Green
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "BetaTesteur",
                        color = Color(0xFFFFD700) // Gold
                    )
                }
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
            UserMode.PASSIVE -> {
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
                    Button(
                        onClick = { /* TODO: Implement admin panel */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF0000) // Red for admin
                        )
                    ) {
                        Text("Panneau d'administration")
                    }
                }
            }
            else -> {
                Text(text = "Statut du compte: $accountType", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigateToBilan,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Filled.Assessment, contentDescription = "Bilan des activités")
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (!isReadOnly) {
            OutlinedButton(onClick = { /* Edit profile save logic could go here */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Enregistrer")
            }
        }
    }
}
