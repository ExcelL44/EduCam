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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
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

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.startQuiz() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedSubject != null && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Commencer le quiz")
                    }
                }
            } else {
                // Affichage de la question
                uiState.currentQuestion?.let { question ->
                    QuestionCard(
                        question = question,
                        selectedAnswer = uiState.selectedAnswer,
                        onAnswerSelected = { viewModel.selectAnswer(it) },
                        onNext = { viewModel.nextQuestion() },
                        isLastQuestion = uiState.isLastQuestion,
                        questionNumber = uiState.currentQuestionIndex + 1,
                        totalQuestions = uiState.totalQuestions
                    )
                }
            }

            // Résultats
            if (uiState.showResults) {
                Spacer(modifier = Modifier.height(24.dp))
                ResultsCard(
                    score = uiState.score,
                    totalQuestions = uiState.totalQuestions,
                    onRestart = { viewModel.restartQuiz() },
                    onBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: QuizQuestion,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit,
    onNext: () -> Unit,
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
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null
            ) {
                Text(if (isLastQuestion) "Terminer" else "Suivant")
            }
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
                Button(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Recommencer")
                }
            }
        }
    }
}

