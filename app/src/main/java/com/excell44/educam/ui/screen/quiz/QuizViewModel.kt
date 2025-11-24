package com.excell44.educam.ui.screen.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuizMode
import com.excell44.educam.data.model.QuizQuestion
import com.excell44.educam.data.repository.QuizRepository
import com.excell44.educam.util.AuthStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val selectedMode: QuizMode? = null,
    val selectedSubject: String? = null,
    val availableSubjects: List<String> = listOf("Math", "Physics", "Chemistry"),
    val isQuizStarted: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val currentQuestion: QuizQuestion? = null,
    val selectedAnswer: String? = null,
    val score: Int = 0,
    val showResults: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val totalQuestions: Int get() = questions.size
    val isLastQuestion: Boolean get() = currentQuestionIndex == totalQuestions - 1
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun loadSubjects() {
        // Les sujets sont hardcodés pour l'instant
        // Peut être amélioré avec un repository
    }

    fun selectMode(mode: QuizMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun selectSubject(subject: String) {
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
    }

    fun startQuiz() {
        val state = _uiState.value
        // determine effective user id (allow guest mode)
        val effectiveUserId: String? = authStateManager.getUserId() ?: run {
            if (authStateManager.getAccountType() == "GUEST") {
                authStateManager.setGuestMode()
                authStateManager.getUserId()
            } else null
        }
        if (effectiveUserId == null) return
        val subject = state.selectedSubject ?: return
        val mode = state.selectedMode ?: QuizMode.FAST

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Créer une session
                val session = quizRepository.createSession(effectiveUserId, mode, subject)

                // Charger les questions (adaptatif selon le mode)
                val questionCount = if (mode == QuizMode.FAST) 10 else 20
                val questions = quizRepository.getQuestions(
                    subject = subject,
                    gradeLevel = "Terminale", // À récupérer depuis le profil utilisateur
                    count = questionCount
                )

                if (questions.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isQuizStarted = true,
                        questions = questions,
                        currentQuestionIndex = 0,
                        currentQuestion = questions[0],
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Aucune question disponible pour cette matière"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun selectAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(selectedAnswer = answer)
    }

    fun nextQuestion() {
        val state = _uiState.value
        val currentIndex = state.currentQuestionIndex
        val selectedAnswer = state.selectedAnswer ?: return

        // Vérifier la réponse
        val currentQuestion = state.currentQuestion
        val isCorrect = currentQuestion?.correctAnswer == selectedAnswer
        val newScore = if (isCorrect) state.score + 1 else state.score

        if (state.isLastQuestion) {
            // Afficher les résultats
            _uiState.value = _uiState.value.copy(
                showResults = true,
                score = newScore
            )
            // If guest, decrement attempts remaining
            if (authStateManager.getAccountType() == "GUEST") {
                authStateManager.decrementGuestAttempts()
            }
        } else {
            // Passer à la question suivante
            val nextIndex = currentIndex + 1
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = nextIndex,
                currentQuestion = state.questions[nextIndex],
                selectedAnswer = null,
                score = newScore
            )
        }
    }

    fun restartQuiz() {
        _uiState.value = QuizUiState(
            selectedMode = _uiState.value.selectedMode,
            selectedSubject = _uiState.value.selectedSubject,
            availableSubjects = _uiState.value.availableSubjects
        )
    }

    fun guestAttemptsRemaining(): Int {
        return authStateManager.getGuestAttemptsRemaining()
    }

    fun isGuestMode(): Boolean = authStateManager.getAccountType() == "GUEST"
}

