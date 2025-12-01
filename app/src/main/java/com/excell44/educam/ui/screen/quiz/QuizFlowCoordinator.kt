package com.excell44.educam.ui.screen.quiz

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.excell44.educam.data.model.QuizMode
import java.util.UUID

enum class QuizStep {
    MENU,
    CONFIGURATION,
    EXECUTION,
    EVALUATION,
    RESULTS
}

/**
 * Coordinateur principal du flux Quiz
 * Gère la navigation entre les différentes étapes :
 * 1. Menu (Rapide/Lent)
 * 2. Configuration (Matière + Tempo)
 * 3. Exécution du Quiz
 * 4. Évaluation
 * 5. Résultats et Options
 */
@Composable
fun QuizFlowCoordinator(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var currentStep by remember { mutableStateOf(QuizStep.MENU) }
    var selectedMode by remember { mutableStateOf<QuizModeType?>(null) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var timePerQuestion by remember { mutableIntStateOf(30) }
    var quizSessionId by remember { mutableStateOf<String?>(null) }
    
    // Données pour l'évaluation (UI Model)
    var completedQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var userAnswers by remember { mutableStateOf<List<Int>>(emptyList()) }
    var timeSpentPerQuestion by remember { mutableStateOf<List<Int>>(emptyList()) }
    
    // Mapping des questions Data -> UI
    val uiQuestions = remember(uiState.questions) {
        uiState.questions.map { dataQuestion ->
            QuizQuestion(
                id = dataQuestion.id,
                content = """
                    <h3>${dataQuestion.topic}</h3>
                    <p>${dataQuestion.question}</p>
                """.trimIndent(),
                answers = dataQuestion.options.map { Answer(it) },
                correctAnswerIndex = dataQuestion.options.indexOf(dataQuestion.correctAnswer).let { if (it == -1) 0 else it },
                timeSpent = 0,
                subject = dataQuestion.subject,
                difficulty = dataQuestion.difficulty.name
            )
        }
    }

    when (currentStep) {
        QuizStep.MENU -> {
            QuizMenuScreen(
                onModeSelected = { mode ->
                    selectedMode = mode
                    // Convertir en QuizMode du data model
                    val dataMode = if (mode == QuizModeType.FAST) 
                        QuizMode.FAST 
                    else 
                        QuizMode.SLOW
                    viewModel.selectMode(dataMode)
                    currentStep = QuizStep.CONFIGURATION
                },
                onNavigateBack = onNavigateBack
            )
        }
        
        QuizStep.CONFIGURATION -> {
            LaunchedEffect(Unit) {
                viewModel.loadSubjects()
            }
            
            QuizConfigScreen(
                mode = selectedMode ?: QuizModeType.FAST,
                availableSubjects = uiState.availableSubjects,
                onStartQuiz = { subject, time ->
                    selectedSubject = subject
                    timePerQuestion = time
                    viewModel.selectSubject(subject)
                    
                    // Démarrer le quiz avec les paramètres
                    val totalDuration = if (selectedMode == QuizModeType.FAST) {
                        10 * time // 10 questions
                    } else {
                        20 * time // 20 questions
                    }
                    viewModel.startQuiz(
                        perQuestionTimerSeconds = time,
                        totalDurationSeconds = totalDuration
                    )
                    
                    quizSessionId = viewModel.getCurrentSessionId()
                    currentStep = QuizStep.EXECUTION
                },
                onNavigateBack = {
                    currentStep = QuizStep.MENU
                }
            )
        }
        
        QuizStep.EXECUTION -> {
            when {
                uiState.isQuizStarted && uiQuestions.isNotEmpty() -> {
                    QuizFlow(
                        questions = uiQuestions,
                        onQuizComplete = { answers ->
                            // Sauvegarder les données pour l'évaluation
                            completedQuestions = uiQuestions
                            userAnswers = answers
                            
                            // Calculer le temps passé par question (simulé pour l'instant)
                            timeSpentPerQuestion = List(uiQuestions.size) { timePerQuestion }
                            
                            // Enregistrer les résultats en base de données
                            val answerDetails = uiQuestions.mapIndexed { index, question ->
                                val selectedIndex = answers.getOrNull(index) ?: -1
                                val selectedText = if (selectedIndex >= 0 && selectedIndex < question.answers.size) 
                                    question.answers[selectedIndex].text 
                                else null
                                val isCorrect = selectedIndex == question.correctAnswerIndex
                                
                                AnswerDetail(
                                    questionId = question.id,
                                    selectedAnswer = selectedText,
                                    isCorrect = isCorrect,
                                    timeRemaining = 0 // Non suivi dans ce flux simplifié
                                )
                            }
                            
                            val correctCount = answerDetails.count { it.isCorrect }
                            viewModel.submitFinalResults(correctCount, uiQuestions.size, answerDetails)
                            
                            currentStep = QuizStep.EVALUATION
                        },
                        onCancelQuiz = {
                            viewModel.cancelQuiz()
                            onNavigateBack()
                        },
                        viewModel = viewModel
                    )
                }
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Chargement des questions...")
                        }
                    }
                }
                uiState.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "Erreur",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { currentStep = QuizStep.MENU }) {
                                Text("Retour au menu")
                            }
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Initialisation...")
                        }
                    }
                }
            }
        }
        
        QuizStep.EVALUATION -> {
            QuizEvaluationScreen(
                questions = completedQuestions,
                userAnswers = userAnswers,
                timeSpentPerQuestion = timeSpentPerQuestion,
                mode = selectedMode ?: QuizModeType.FAST,
                onContinue = {
                    currentStep = QuizStep.RESULTS
                }
            )
        }
        
        QuizStep.RESULTS -> {
            val correctCount = completedQuestions.indices.count { i ->
                userAnswers.getOrNull(i) == completedQuestions[i].correctAnswerIndex
            }
            
            QuizResultsScreen(
                score = correctCount,
                totalQuestions = completedQuestions.size,
                sessionId = quizSessionId ?: UUID.randomUUID().toString(),
                onViewDetailedAnalysis = {
                    // Retour à l'évaluation
                    currentStep = QuizStep.EVALUATION
                },
                onRetakeQuiz = {
                    // Refaire le même quiz avec les mêmes paramètres
                    currentStep = QuizStep.EXECUTION
                    viewModel.restartQuiz()
                },
                onNewQuiz = {
                    // Recommencer depuis le menu
                    currentStep = QuizStep.MENU
                    selectedMode = null
                    selectedSubject = null
                    completedQuestions = emptyList()
                    userAnswers = emptyList()
                    timeSpentPerQuestion = emptyList()
                },
                onBackToHome = {
                    onNavigateBack()
                }
            )
        }
    }
}
