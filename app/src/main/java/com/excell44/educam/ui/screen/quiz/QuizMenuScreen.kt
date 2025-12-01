package com.excell44.educam.ui.screen.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.excell44.educam.ui.util.screenPadding

/**
 * Écran de Menu Principal du Quiz
 * Permet de choisir entre mode Rapide ou Lent
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizMenuScreen(
    onModeSelected: (QuizModeType) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .screenPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Choisissez votre mode de quiz",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Mode Rapide
            QuizModeCard(
                title = "Quiz Rapide ⚡",
                description = "10 questions · Temps limité par question",
                icon = Icons.Default.Speed,
                onClick = { onModeSelected(QuizModeType.FAST) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mode Lent
            QuizModeCard(
                title = "Quiz Approfondi 🎯",
                description = "20 questions · Plus de temps pour réfléchir",
                icon = Icons.Default.AccessTime,
                onClick = { onModeSelected(QuizModeType.SLOW) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Carte pour un mode de quiz
 */
@Composable
private fun QuizModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Type de mode de quiz (pour distinction UI)
 */
enum class QuizModeType {
    FAST,
    SLOW
}
