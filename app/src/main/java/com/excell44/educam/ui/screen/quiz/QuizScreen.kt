@file:OptIn(ExperimentalMaterial3Api::class)

package com.excell44.educam.ui.screen.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuizMode
import com.excell44.educam.data.model.QuizQuestion

@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSubjects()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Adaptatif") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!uiState.isQuizStarted) {
                // Sélection du mode et du sujet
                Text(
                    text = "Choisissez votre mode de quiz",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // If there's a paused session, offer resume
                if (uiState.isPaused) {
                    Spacer(modifier = Modifier.height(8.dp))
                    com.excell44.educam.ui.components.PrimaryButton(
                        onClick = {
                            val id = viewModel.getCurrentSessionId()
                            if (id != null) viewModel.resumeSession(id)
                        },
                        text = "Reprendre la session en pause",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Sélection du mode
                Text(
                    text = "Mode",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedMode == QuizMode.FAST,
                        onClick = { viewModel.selectMode(QuizMode.FAST) },
                        label = { Text("Rapide") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.selectedMode == QuizMode.SLOW,
                        onClick = { viewModel.selectMode(QuizMode.SLOW) },
                        label = { Text("Lent") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sélection du sujet
                Text(
                    text = "Matière",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                uiState.availableSubjects.forEach { subject ->
                    FilterChip(
                        selected = uiState.selectedSubject == subject,
                        onClick = { viewModel.selectSubject(subject) },
                        label = { Text(subject) },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }

                // Rapid mode options
                var selectedTotalDuration by remember { mutableStateOf(if (uiState.totalQuestions > 0) uiState.totalDurationSeconds else 180) }
                var selectedPerQuestionTimer by remember { mutableStateOf(uiState.perQuestionTimerSeconds.coerceAtLeast(10)) }

                if (uiState.selectedMode == QuizMode.FAST) {
                    Text(text = "Durée totale", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = selectedTotalDuration == 180, onClick = { selectedTotalDuration = 180 }, label = { Text("3 min") })
                            FilterChip(selected = selectedTotalDuration == 300, onClick = { selectedTotalDuration = 300 }, label = { Text("5 min") })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Timer question", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = selectedPerQuestionTimer == 10, onClick = { selectedPerQuestionTimer = 10 }, label = { Text("10s") })
                        FilterChip(selected = selectedPerQuestionTimer == 30, onClick = { selectedPerQuestionTimer = 30 }, label = { Text("30s") })
                        FilterChip(selected = selectedPerQuestionTimer == 60, onClick = { selectedPerQuestionTimer = 60 }, label = { Text("60s") })
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    com.excell44.educam.ui.components.PrimaryButton(
                        onClick = { viewModel.startQuiz(perQuestionTimerSeconds = selectedPerQuestionTimer, totalDurationSeconds = selectedTotalDuration) },
                        text = if (uiState.isLoading) "..." else "Commencer le quiz",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedSubject != null && !uiState.isLoading && (viewModel.isGuestMode().not() || viewModel.guestAttemptsRemaining() > 0)
                    )
                } else {
                    com.excell44.educam.ui.components.PrimaryButton(
                        onClick = { viewModel.startQuiz() },
                        text = if (uiState.isLoading) "..." else "Commencer le quiz",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedSubject != null && !uiState.isLoading && (viewModel.isGuestMode().not() || viewModel.guestAttemptsRemaining() > 0)
                    )
                }
            } else {
                // Affichage de la question
                uiState.currentQuestion?.let { question ->
                    QuestionCard(
                        question = question,
                        selectedAnswer = uiState.selectedAnswer,
                        onAnswerSelected = { viewModel.selectAnswer(it) },
                        onNextWithTimeLeft = { timeLeft -> viewModel.submitAnswerAndNext(timeLeft) },
                        onHintRequested = { viewModel.recordHintUsed() },
                        canShowHint = viewModel.canShowHint(),
                        perQuestionTimerSeconds = uiState.perQuestionTimerSeconds,
                        isLastQuestion = uiState.isLastQuestion,
                        questionNumber = uiState.currentQuestionIndex + 1,
                        totalQuestions = uiState.totalQuestions
                    )
                }
            }

            // Résultats
            if (uiState.showResults) {
                Spacer(modifier = Modifier.height(24.dp))
                val remaining = if (viewModel.isGuestMode()) viewModel.guestAttemptsRemaining() else null
                ResultsCard(
                    score = uiState.score,
                    totalQuestions = uiState.totalQuestions,
                    remainingAttempts = remaining,
                    onRestart = { viewModel.restartQuiz() },
                    onBack = onNavigateBack
                )

                uiState.estimatedScoreOutOf20?.let { est ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Note estimative (sur 20)", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "${String.format("%.1f", est)} / 20", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: QuizQuestion,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit,
    onNextWithTimeLeft: (Int) -> Unit,
    onHintRequested: () -> Unit,
    canShowHint: Boolean,
    perQuestionTimerSeconds: Int,
    isLastQuestion: Boolean,
    questionNumber: Int,
    totalQuestions: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Question $questionNumber / $totalQuestions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            // per-question timer and hint with visual progress and sound on expiry
            var timeLeft by remember { mutableStateOf(perQuestionTimerSeconds) }
            var showHint by remember { mutableStateOf(false) }

            // play small beep when timer hits zero
            val toneGen = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }

            LaunchedEffect(key1 = question.id) {
                timeLeft = perQuestionTimerSeconds
                while (timeLeft > 0) {
                    kotlinx.coroutines.delay(1000)
                    timeLeft -= 1
                }
                // play beep and auto submit with 0 remaining
                try {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
                } catch (_: Exception) {
                }
                onNextWithTimeLeft(0)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // circular progress indicator showing remaining time
                val progress = (timeLeft.toFloat() / perQuestionTimerSeconds.toFloat()).coerceIn(0f, 1f)
                CircularProgressIndicator(progress = progress, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${timeLeft}s", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    if (canShowHint) {
                        showHint = !showHint
                        if (showHint) onHintRequested()
                    }
                }, enabled = canShowHint) {
                    Text(if (canShowHint) "Indice" else "Indice (limité)")
                }
            }

            if (showHint) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(text = question.explanation.take(160), modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (question.questionType == com.excell44.educam.data.model.QuestionType.MULTIPLE_CHOICE) {
                question.options.forEach { option ->
                    AnswerOption(
                        text = option,
                        isSelected = selectedAnswer == option,
                        onClick = { onAnswerSelected(option) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            com.excell44.educam.ui.components.PrimaryButton(
                onClick = { onNextWithTimeLeft(timeLeft) },
                text = if (isLastQuestion) "Terminer" else "Suivant",
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null
            )
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ResultsCard(
    score: Int,
    totalQuestions: Int,
    remainingAttempts: Int? = null,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Résultats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$score / $totalQuestions",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(score * 100 / totalQuestions)}% de réussite",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            remainingAttempts?.let { rem ->
                val msg = if (rem > 0) "Essais restants (invité): $rem" else "Vous avez épuisé vos essais gratuits."
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retour")
                    }
                    com.excell44.educam.ui.components.PrimaryButton(
                        onClick = onRestart,
                        text = "Recommencer",
                        modifier = Modifier.weight(1f)
                    )
            }
        }
    }
}

