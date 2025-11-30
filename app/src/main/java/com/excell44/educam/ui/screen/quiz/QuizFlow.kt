package com.excell44.educam.ui.screen.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Exemple de question pour démonstration
 */
val sampleQuestions = listOf(
    QuizQuestion(
        id = "math_001",
        content = """
            <h3>Résoudre l'équation suivante :</h3>
            <p>Soit l'équation : $$2x^2 + 5x - 3 = 0$$</p>
            <p>Quelle est la solution correcte ?</p>
        """.trimIndent(),
        answers = listOf(
            Answer("x = 1 ou x = -3"),
            Answer("x = 0,5 ou x = -3"),
            Answer("x = -1 ou x = 3"),
            Answer("x = -0,5 ou x = 3")
        ),
        correctAnswerIndex = 1,
        timeSpent = 120,
        subject = "Mathématiques",
        difficulty = "Moyen"
    ),
    QuizQuestion(
        id = "phys_001",
        content = """
            <h3>Mécanique - Force et Mouvement</h3>
            <p>Un corps de masse m = 5 kg est soumis à une force horizontale F = 20 N.</p>
            <p>Quelle est son accélération ? (g = 10 m/s²)</p>
            <img src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEwMCI+PHJlY3QgeD0iNTAiIHk9IjQwIiB3aWR0aD0iODAiIGhlaWdodD0iNDAiIGZpbGw9IiM0Q0FGNTAiLz48dGV4dCB4PSI5MCIgeT0iNjUiIGZpbGw9IndoaXRlIj5tPC90ZXh0Pjwvc3ZnPg==" />
        """.trimIndent(),
        answers = listOf(
            Answer("a = 2 m/s²"),
            Answer("a = 4 m/s²"),
            Answer("a = 5 m/s²"),
            Answer("a = 10 m/s²")
        ),
        correctAnswerIndex = 1,
        timeSpent = 45,
        subject = "Physique",
        difficulty = "Facile"
    ),
    QuizQuestion(
        id = "chem_001",
        content = """
            <h3>Chimie Organique - Nomenclature</h3>
            <p>Quel est le nom IUPAC du composé suivant ?</p>
            <p>CH₃-CH₂-CH₂-CH₂-OH</p>
        """.trimIndent(),
        answers = listOf(
            Answer("Butanol"),
            Answer("Butan-1-ol"),
            Answer("Propanol"),
            Answer("Pentan-1-ol")
        ),
        correctAnswerIndex = 1,
        timeSpent = 200,
        subject = "Chimie",
        difficulty = "Difficile"
    )
)

/**
 * Conteneur pour gérer la navigation entre plusieurs questions
 */
@Composable
fun QuizFlow(
    questions: List<QuizQuestion> = sampleQuestions,
    onQuizComplete: () -> Unit = {},
    onCancelQuiz: () -> Unit = {},
    viewModel: QuizViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var userAnswers by remember { mutableStateOf(mutableListOf<Int>()) }

    if (currentQuestionIndex < questions.size) {
        QuizScreen(
            question = questions[currentQuestionIndex],
            onAnswerSelected = { answerIndex ->
                if (userAnswers.size > currentQuestionIndex) {
                    userAnswers[currentQuestionIndex] = answerIndex
                } else {
                    userAnswers.add(answerIndex)
                }
            },
            onNextClicked = {
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                } else {
                    onQuizComplete()
                }
            },
            onCancelQuiz = {
                // Appeler la méthode du ViewModel pour annuler proprement
                viewModel.cancelQuiz()
                onCancelQuiz()
            }
        )
    } else {
        // Quiz terminé - Afficher résultats
        QuizResultsScreen(
            questions = questions,
            userAnswers = userAnswers,
            onRetry = { currentQuestionIndex = 0; userAnswers.clear() }
        )
    }
}

/**
 * Écran de résultats du quiz
 */
@Composable
fun QuizResultsScreen(
    questions: List<QuizQuestion>,
    userAnswers: List<Int>,
    onRetry: () -> Unit
) {
    val correctCount = questions.indices.count { i ->
        userAnswers.getOrNull(i) == questions[i].correctAnswerIndex
    }
    val scorePercentage = (correctCount * 100) / questions.size
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Terminé ! 🎉",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Score: $correctCount / ${questions.size}",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "$scorePercentage%",
            style = MaterialTheme.typography.displayLarge,
            color = when {
                scorePercentage >= 80 -> MaterialTheme.colorScheme.primary
                scorePercentage >= 50 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LinearProgressIndicator(
            progress = { scorePercentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        com.excell44.educam.ui.components.PrimaryButton(
            onClick = onRetry,
            text = "🔄 Recommencer",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizScreenPreview() {
    com.excell44.educam.ui.theme.BacXTheme {
        QuizFlow()
    }
}
