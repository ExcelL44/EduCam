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
import com.excell44.educam.ui.viewmodel.AuthViewModel

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

    // read profile json if present (simple parse not implemented here)
    LaunchedEffect(Unit) {
        viewModel.getProfileJsonForCurrentUser()?.let { json ->
            // naive extraction of pseudo from the minimal profileJson we stored earlier
            val regex = "\"pseudo\":\"(.*?)\"".toRegex()
            val match = regex.find(json)
            if (match != null) pseudo = match.groupValues[1]
        }
    }

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
                        .clickable { selectedAvatar = i },
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
            onValueChange = { if (it.length <= 15) pseudo = it },
            label = { Text("Pseudo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Statut du compte: $accountType", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))
        com.excell44.educam.ui.components.PrimaryButton(
            onClick = onNavigateToBilan,
            text = "Bilan des activités",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = { /* Edit profile save logic could go here */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Enregistrer")
        }
    }
}
