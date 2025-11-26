package com.excell44.educam.ui.screen.quiz

import android.webkit.WebView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Écran principal du quiz
 * Affiche une question avec WebView (formules, images, HTML) et grille de réponses 2x2
 */
@Composable
fun QuizScreen(
    question: QuizQuestion,
    onAnswerSelected: (Int) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showFeedback by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Timer en haut (10min max)
        TimerBar(
            totalSeconds = 600,
            currentSeconds = question.timeSpent,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contenu question (WebView pour formules, images, HTML)
        WebViewContent(
            htmlContent = question.content,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Boutons réponses (GRILLE 2x2 responsive)
        AnswerGrid(
            answers = question.answers,
            selectedIndex = selectedAnswer,
            showFeedback = showFeedback,
            correctIndex = question.correctAnswerIndex,
            onAnswerClick = { index ->
                selectedAnswer = index
                showFeedback = true
                onAnswerSelected(index)
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Bouton Suivant élégant (synchronisé au thème)
        com.excell44.educam.ui.components.PrimaryButton(
            onClick = {
                if (selectedAnswer != -1) {
                    onNextClicked()
                    selectedAnswer = -1
                    showFeedback = false
                }
            },
            text = "➡️ Question suivante",
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedAnswer != -1
        )
    }
}

/**
 * WebView pour afficher le contenu riche (HTML, formules MathJax, images)
 */
@Composable
fun WebViewContent(
    htmlContent: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                webViewClient = android.webkit.WebViewClient() // Empêche l'ouverture dans un navigateur externe
                loadDataWithBaseURL(null, wrapHTMLContent(htmlContent), "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    )
}

// ... (wrapHTMLContent reste inchangé)

/**
 * Grille de réponses 2x2 responsive (Implémentation manuelle stable)
 */
@Composable
fun AnswerGrid(
    answers: List<Answer>,
    selectedIndex: Int,
    showFeedback: Boolean,
    correctIndex: Int,
    onAnswerClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val rows = answers.chunked(2)
        rows.forEachIndexed { rowIndex, rowAnswers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowAnswers.forEachIndexed { colIndex, answer ->
                    val globalIndex = rowIndex * 2 + colIndex
                    Box(modifier = Modifier.weight(1f)) {
                        AnswerButton(
                            text = answer.text,
                            isSelected = selectedIndex == globalIndex,
                            showFeedback = showFeedback,
                            isCorrect = globalIndex == correctIndex,
                            onClick = { onAnswerClick(globalIndex) }
                        )
                    }
                }
                // Si la dernière ligne n'a qu'un élément, ajouter un espace vide pour l'alignement
                if (rowAnswers.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Bouton de réponse avec feedback visuel animé
 */
@Composable
fun AnswerButton(
    text: String,
    isSelected: Boolean,
    showFeedback: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            showFeedback && isCorrect -> Color(0xFF4CAF50) // Vert validation
            showFeedback && isSelected && !isCorrect -> Color(0xFFF44336) // Rouge erreur
            isSelected -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = spring(stiffness = 300f),
        label = "answer_background"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .semantics { role = Role.Button },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null, // Handled by Card
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    showFeedback && (isCorrect || (isSelected && !isCorrect)) -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * Barre de progression du timer (10 min max)
 */
@Composable
fun TimerBar(
    totalSeconds: Int,
    currentSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = currentSeconds.toFloat() / totalSeconds.toFloat()
    
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
        color = if (progress > 0.7f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/**
 * Modèle de données pour une question de quiz
 */
data class QuizQuestion(
    val id: String,
    val content: String, // HTML content avec formules, images
    val answers: List<Answer>,
    val correctAnswerIndex: Int,
    val timeSpent: Int = 0,
    val subject: String = "Mathématiques",
    val difficulty: String = "Moyen"
)

/**
 * Modèle de données pour une réponse
 */
data class Answer(
    val text: String,
    val id: String = ""
)
