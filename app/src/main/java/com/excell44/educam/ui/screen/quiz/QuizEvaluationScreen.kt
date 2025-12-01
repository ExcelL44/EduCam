package com.excell44.educam.ui.screen.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.excell44.educam.ui.util.screenPadding

/**
 * Écran d'Évaluation et Analyse des résultats du Quiz
 * Affiche le score, les statistiques et l'analyse détaillée par question
 */
@Composable
fun QuizEvaluationScreen(
    questions: List<QuizQuestion>,
    userAnswers: List<Int>,
    timeSpentPerQuestion: List<Int>, // en secondes
    mode: QuizModeType,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val correctCount = questions.indices.count { i ->
        userAnswers.getOrNull(i) == questions[i].correctAnswerIndex
    }
    val scorePercentage = if (questions.isNotEmpty()) {
        (correctCount * 100) / questions.size
    } else 0
    
    val totalTimeSpent = timeSpentPerQuestion.sum()
    val averageTimePerQuestion = if (questions.isNotEmpty()) {
        totalTimeSpent / questions.size
    } else 0
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .screenPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête avec score global
        item {
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getScoreEmoji(scorePercentage),
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Score : $scorePercentage%",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    
                    Text(
                        text = "$correctCount / ${questions.size} réponses correctes",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        // Statistiques
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Statistiques",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    StatRow("Mode", if (mode == QuizModeType.FAST) "⚡ Rapide" else "🎯 Approfondi")
                    StatRow("Temps total", formatTime(totalTimeSpent))
                    StatRow("Temps moyen/question", "${averageTimePerQuestion}s")
                    StatRow("Bonnes réponses", "$correctCount")
                    StatRow("Mauvaises réponses", "${questions.size - correctCount}")
                }
            }
        }
        
        item { 
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "📝 Analyse détaillée",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Liste des questions avec résultats
        itemsIndexed(questions) { index, question ->
            QuestionResultCard(
                questionNumber = index + 1,
                question = question,
                userAnswerIndex = userAnswers.getOrNull(index),
                timeSpent = timeSpentPerQuestion.getOrNull(index) ?: 0
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Bouton Continuer
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuer")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Carte affichant le résultat d'une question
 */
@Composable
private fun QuestionResultCard(
    questionNumber: Int,
    question: QuizQuestion,
    userAnswerIndex: Int?,
    timeSpent: Int
) {
    val isCorrect = userAnswerIndex == question.correctAnswerIndex
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $questionNumber",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isCorrect) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${timeSpent}s",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = question.subject,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (!isCorrect) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bonne réponse",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = question.answers[question.correctAnswerIndex].text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ligne de statistique
 */
@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Retourne l'emoji approprié selon le score
 */
private fun getScoreEmoji(scorePercentage: Int): String {
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
 * Formate un temps en secondes en format lisible
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}min ${remainingSeconds}s"
    } else {
        "${seconds}s"
    }
}
