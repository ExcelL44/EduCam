package com.excell44.educam.ui.screen.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.data.model.UserMode
import com.excell44.educam.ui.components.UserModeIndicator
import com.excell44.educam.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToBilan: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val accountType = viewModel.getAccountType()
    var selectedAvatar by remember { mutableStateOf(0) }
    var pseudo by remember { mutableStateOf("") }
    var userMode by remember { mutableStateOf<UserMode?>(null) }

    // Fetch user mode
    LaunchedEffect(Unit) {
        val userId = viewModel.uiState.value.let { 
            // Get userId from AuthStateManager via viewModel
            viewModel.getProfileJsonForCurrentUser()
        }
        // For now, we'll determine mode from accountType
        // In a real scenario, we'd fetch the User from repository
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Profil") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) { Text("Retour") }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Mode Indicator
        userMode?.let {
            UserModeIndicator(userMode = it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = "Photo de profil", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // 7 avatar choices shown as neutral colored circles
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            (1..7).forEach { i ->
                val color = when (i % 7) {
                    0 -> Color(0xFFBDBDBD)
                    1 -> Color(0xFFB3E5FC)
                    2 -> Color(0xFFC8E6C9)
                    3 -> Color(0xFFFFF9C4)
                    4 -> Color(0xFFFFCDD2)
                    5 -> Color(0xFFD1C4E9)
                    else -> Color(0xFFFFE0B2)
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable(enabled = !isReadOnly) { selectedAvatar = i },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedAvatar == i) {
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
                        containerColor = Color(0xFF800080) // Violet
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
            else -> {
                Text(text = "Statut du compte: $accountType", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        com.excell44.educam.ui.components.PrimaryButton(
            onClick = onNavigateToBilan,
            text = "Bilan des activités",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        if (!isReadOnly) {
            OutlinedButton(onClick = { /* Edit profile save logic could go here */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Enregistrer")
            }
        }
    }
}
