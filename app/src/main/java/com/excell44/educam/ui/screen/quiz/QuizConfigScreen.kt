package com.excell44.educam.ui.screen.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.excell44.educam.ui.util.screenPadding

/**
 * Écran de Paramétrage du Quiz
 * Permet de sélectionner la matière et configurer le temps
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigScreen(
    mode: QuizModeType,
    availableSubjects: List<com.excell44.educam.ui.screen.quiz.SubjectInfo>,
    onStartQuiz: (subject: String, timePerQuestion: Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var timePerQuestion by remember { 
        mutableIntStateOf(if (mode == QuizModeType.FAST) 30 else 60) 
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuration du Quiz") },
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
                .screenPadding()
        ) {
            // Affichage du mode sélectionné
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Mode sélectionné",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (mode == QuizModeType.FAST) "⚡ Quiz Rapide" else "🎯 Quiz Approfondi",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (mode == QuizModeType.FAST) "10 questions" else "20 questions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sélection de la matière
            Text(
                text = "Choisissez une matière",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableSubjects) { subjectInfo ->
                    SubjectSelectionCard(
                        subjectInfo = subjectInfo,
                        isSelected = selectedSubject == subjectInfo.name,
                        onClick = {
                            if (subjectInfo.isAvailable) {
                                selectedSubject = subjectInfo.name
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Configuration du temps par question
            Text(
                text = "Temps par question",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$timePerQuestion secondes",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Row {
                    FilledIconButton(
                        onClick = { if (timePerQuestion > 10) timePerQuestion -= 10 },
                        enabled = timePerQuestion > 10
                    ) {
                        Text("-10s")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FilledIconButton(
                        onClick = { if (timePerQuestion < 180) timePerQuestion += 10 },
                        enabled = timePerQuestion < 180
                    ) {
                        Text("+10s")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bouton Démarrer
            Button(
                onClick = { 
                    selectedSubject?.let { subject ->
                        onStartQuiz(subject, timePerQuestion)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSubject != null
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Démarrer le Quiz")
            }
        }
    }
}

/**
 * Carte de sélection de matière
 */
@Composable
private fun SubjectSelectionCard(
    subjectInfo: com.excell44.educam.ui.screen.quiz.SubjectInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !subjectInfo.isAvailable -> MaterialTheme.colorScheme.surfaceVariant
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subjectInfo.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        !subjectInfo.isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (!subjectInfo.isAvailable) {
                    Text(
                        text = "Bientôt disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                isSelected -> Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                    contentDescription = "Sélectionné",
                    tint = MaterialTheme.colorScheme.primary
                )
                !subjectInfo.isAvailable -> Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Verrouillé",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Retourne l'emoji associé à une matière
 */
private fun getSubjectEmoji(subject: String): String {
    return when (subject.lowercase()) {
        "mathématiques", "maths" -> "📐"
        "physique" -> "⚛️"
        "chimie" -> "🧪"
        "biologie" -> "🧬"
        "histoire" -> "📚"
        "géographie" -> "🌍"
        "français" -> "✍️"
        "anglais" -> "🇬🇧"
        "philosophie" -> "🤔"
        else -> "📖"
    }
}
