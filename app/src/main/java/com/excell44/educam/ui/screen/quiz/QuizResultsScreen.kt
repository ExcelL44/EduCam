package com.excell44.educam.ui.screen.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.excell44.educam.ui.util.screenPadding

/**
 * Écran de Résultats Finaux avec Dialog
 * Propose de revoir le bilan, reprendre un quiz ou refaire le même quiz
 */
@Composable
fun QuizResultsDialog(
    score: Int,
    totalQuestions: Int,
    onViewBilan: () -> Unit,
    onRetakeQuiz: () -> Unit,
    onNewQuiz: () -> Unit,
    onDismiss: () -> Unit
) {
    val scorePercentage = if (totalQuestions > 0) {
        (score * 100) / totalQuestions
    } else 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = getResultEmoji(scorePercentage),
                style = MaterialTheme.typography.displayLarge
            )
        },
        title = {
            Text(
                text = "Quiz Terminé !",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Score",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "$scorePercentage%",
                    style = MaterialTheme.typography.displayMedium,
                    color = when {
                        scorePercentage >= 80 -> MaterialTheme.colorScheme.primary
                        scorePercentage >= 50 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                
                Text(
                    text = "$score / $totalQuestions réponses correctes",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = getEncouragementMessage(scorePercentage),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bouton Voir le bilan complet
                Button(
                    onClick = onViewBilan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voir le bilan détaillé")
                }
                
                // Bouton Refaire ce quiz
                OutlinedButton(
                    onClick = onRetakeQuiz,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refaire ce quiz")
                }
                
                // Bouton Nouveau quiz
                OutlinedButton(
                    onClick = onNewQuiz,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nouveau quiz")
                }
                
                // Bouton Fermer
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fermer")
                }
            }
        }
    )
}

/**
 * Écran de Résultats avec enregistrement
 * Affiche les résultats et permet la navigation vers différentes actions
 */
@Composable
fun QuizResultsScreen(
    score: Int,
    totalQuestions: Int,
    sessionId: String,
    onViewDetailedAnalysis: () -> Unit,
    onRetakeQuiz: () -> Unit,
    onNewQuiz: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(true) }
    
    if (showDialog) {
        QuizResultsDialog(
            score = score,
            totalQuestions = totalQuestions,
            onViewBilan = {
                showDialog = false
                onViewDetailedAnalysis()
            },
            onRetakeQuiz = {
                showDialog = false
                onRetakeQuiz()
            },
            onNewQuiz = {
                showDialog = false
                onNewQuiz()
            },
            onDismiss = {
                showDialog = false
                onBackToHome()
            }
        )
    }
    
    // Écran de fond avec résumé
    Column(
        modifier = modifier
            .fillMaxSize()
            .screenPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val scorePercentage = if (totalQuestions > 0) {
            (score * 100) / totalQuestions
        } else 0
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    scorePercentage >= 80 -> MaterialTheme.colorScheme.primaryContainer
                    scorePercentage >= 50 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getResultEmoji(scorePercentage),
                    style = MaterialTheme.typography.displayLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Quiz Terminé !",
                    style = MaterialTheme.typography.headlineLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$scorePercentage%",
                    style = MaterialTheme.typography.displayLarge,
                    color = when {
                        scorePercentage >= 80 -> MaterialTheme.colorScheme.primary
                        scorePercentage >= 50 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                
                Text(
                    text = "$score / $totalQuestions réponses correctes",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { scorePercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Session ID: ${sessionId.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Les résultats ont été enregistrés",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Retourne l'emoji approprié selon le résultat
 */
private fun getResultEmoji(scorePercentage: Int): String {
    return when {
        scorePercentage >= 90 -> "🏆"
        scorePercentage >= 80 -> "🎉"
        scorePercentage >= 70 -> "👍"
        scorePercentage >= 50 -> "😊"
        scorePercentage >= 30 -> "😐"
        else -> "💪"
    }
}

/**
 * Retourne un message d'encouragement selon le score
 */
private fun getEncouragementMessage(scorePercentage: Int): String {
    return when {
        scorePercentage >= 90 -> "Excellent ! Vous maîtrisez parfaitement ce sujet !"
        scorePercentage >= 80 -> "Très bon travail ! Continuez comme ça !"
        scorePercentage >= 70 -> "Bon résultat ! Quelques révisions et ce sera parfait !"
        scorePercentage >= 50 -> "Pas mal ! Il y a de la marge de progression !"
        scorePercentage >= 30 -> "Continuez à vous entraîner, la réussite viendra !"
        else -> "Ne vous découragez pas ! Chaque tentative vous rapproche du succès !"
    }
}
