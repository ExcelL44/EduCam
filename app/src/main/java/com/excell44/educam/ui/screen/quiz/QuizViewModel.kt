package com.excell44.educam.ui.screen.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuizMode
import com.excell44.educam.data.model.QuizQuestion
import com.excell44.educam.data.repository.QuizRepository
import com.excell44.educam.util.AuthStateManager
import com.excell44.educam.util.Logger
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
    ,
    val isPaused: Boolean = false,
    // Rapid-mode settings
    val perQuestionTimerSeconds: Int = 30,
    val totalDurationSeconds: Int = 180,
    val accumulatedTimeRemainingSeconds: Long = 0L,
    val estimatedScoreOutOf20: Double? = null
) {
    val totalQuestions: Int get() = questions.size
    val isLastQuestion: Boolean get() = currentQuestionIndex == totalQuestions - 1
}

data class AnswerDetail(
    val questionId: String,
    val selectedAnswer: String?,
    val isCorrect: Boolean,
    val timeRemaining: Int
)


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Keep reference to the currently active session so we can update it when quiz completes
    private var currentSession: com.excell44.educam.data.model.QuizSession? = null
    // accumulate per-question details during the quiz
    private val answerDetails = mutableListOf<AnswerDetail>()

    // hint usage tracking (limit for guests)
    private var hintsUsed = 0
    private val guestHintLimit = 3

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

    fun startQuiz(perQuestionTimerSeconds: Int = 30, totalDurationSeconds: Int = 180) {
        val state = _uiState.value
        
        // Check Guest mode restrictions
        if (authStateManager.getAccountType() == "GUEST") {
            val remaining = authStateManager.getGuestAttemptsRemaining()
            if (remaining <= 0) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Mode Invité: Vous avez épuisé vos 3 essais. Créez un compte pour continuer."
                )
                return
            }
        }
        
        // Check Passive mode (Trial) restrictions
        if (authStateManager.getAccountType() == "TRIAL") {
            if (authStateManager.isTrialExpired()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Mode Passif: Votre période d'essai de 7 jours a expiré. Activez votre compte."
                )
                return
            }
        }
        
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
                currentSession = session
                // reset per-quiz accumulators
                answerDetails.clear()
                hintsUsed = 0

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
                        isLoading = false,
                        perQuestionTimerSeconds = perQuestionTimerSeconds,
                        totalDurationSeconds = totalDurationSeconds,
                        accumulatedTimeRemainingSeconds = 0L,
                        estimatedScoreOutOf20 = null
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
    
    /**
     * Submit the currently selected answer and advance to the next question.
     * Pass the remaining seconds for this question (used in scoring for rapid mode).
     */
    fun submitAnswerAndNext(timeRemainingSeconds: Int) {
        val state = _uiState.value
        val currentIndex = state.currentQuestionIndex
        val selectedAnswer = state.selectedAnswer ?: ""

        val currentQuestion = state.currentQuestion
        val isCorrect = currentQuestion?.correctAnswer == selectedAnswer
        val newScoreCount = if (isCorrect) state.score + 1 else state.score
        val newAccumulated = state.accumulatedTimeRemainingSeconds + timeRemainingSeconds

        // record detail for this question
        currentQuestion?.let { q ->
            answerDetails.add(
                AnswerDetail(
                    questionId = q.id,
                    selectedAnswer = if (selectedAnswer.isBlank()) null else selectedAnswer,
                    isCorrect = isCorrect,
                    timeRemaining = timeRemainingSeconds
                )
            )
        }

        if (state.isLastQuestion) {
            // compute estimated score out of 20 using correctness and average time remaining
            val totalQ = state.totalQuestions.coerceAtLeast(1)
            val fractionCorrect = newScoreCount.toDouble() / totalQ
            val perQTimer = state.perQuestionTimerSeconds.coerceAtLeast(1)
            val maxTimePool = totalQ * perQTimer.toDouble()
            val avgTimeRemainingRatio = (newAccumulated.toDouble() / maxTimePool).coerceIn(0.0, 1.0)

            val finalScore = ((fractionCorrect * 0.8) + (avgTimeRemainingRatio * 0.2)) * 20.0

            _uiState.value = _uiState.value.copy(
                showResults = true,
                score = newScoreCount,
                accumulatedTimeRemainingSeconds = newAccumulated,
                estimatedScoreOutOf20 = String.format("%.1f", finalScore).toDoubleOrNull() ?: finalScore
            )

            // Persist session results and detailsJson (best-effort)
            viewModelScope.launch {
                try {
                    currentSession?.let { sess ->
                        // build details JSON
                        val arr = org.json.JSONArray()
                        for (d in answerDetails) {
                            val obj = org.json.JSONObject()
                            obj.put("questionId", d.questionId)
                            obj.put("selectedAnswer", d.selectedAnswer)
                            obj.put("isCorrect", d.isCorrect)
                            obj.put("timeRemaining", d.timeRemaining)
                            arr.put(obj)
                        }
                        val updated = sess.copy(
                            score = newScoreCount,
                            totalQuestions = totalQ,
                            endTime = System.currentTimeMillis(),
                            isCompleted = true,
                            detailsJson = arr.toString()
                        )
                        quizRepository.updateSession(updated)
                    }
                } catch (_: Exception) {
                }
            }

            if (authStateManager.getAccountType() == "GUEST") {
                authStateManager.decrementGuestAttempts()
            }
        } else {
            val nextIndex = currentIndex + 1
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = nextIndex,
                currentQuestion = state.questions[nextIndex],
                selectedAnswer = null,
                score = newScoreCount,
                accumulatedTimeRemainingSeconds = newAccumulated
            )
        }
    }

    // Backwards-compatible nextQuestion (no timing info)
    fun nextQuestion() {
        submitAnswerAndNext(0)
    }
    fun selectAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(selectedAnswer = answer)
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

    fun canShowHint(): Boolean {
        return if (isGuestMode()) hintsUsed < guestHintLimit else true
    }

    fun recordHintUsed() {
        hintsUsed++
    }

    /**
     * Pause the current quiz: persist partial progress into session.detailsJson
     */
    fun pauseQuiz() {
        viewModelScope.launch {
            try {
                currentSession?.let { sess ->
                    val state = _uiState.value
                    val wrapper = org.json.JSONObject()
                    wrapper.put("paused", true)
                    wrapper.put("currentIndex", state.currentQuestionIndex)
                    wrapper.put("accumulatedTimeRemainingSeconds", state.accumulatedTimeRemainingSeconds)
                    wrapper.put("perQuestionTimerSeconds", state.perQuestionTimerSeconds)
                    val arr = org.json.JSONArray()
                    for (d in answerDetails) {
                        val obj = org.json.JSONObject()
                        obj.put("questionId", d.questionId)
                        obj.put("selectedAnswer", d.selectedAnswer)
                        obj.put("isCorrect", d.isCorrect)
                        obj.put("timeRemaining", d.timeRemaining)
                        arr.put(obj)
                    }
                    wrapper.put("answers", arr)
                    val updated = sess.copy(
                        detailsJson = wrapper.toString(),
                        isCompleted = false
                    )
                    quizRepository.updateSession(updated)
                    // update UI state
                    _uiState.value = _uiState.value.copy(isQuizStarted = false, isPaused = true, isLoading = false)
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Resume a paused session by id (restore answers list and current index)
     */
    fun resumeSession(sessionId: String) {
        viewModelScope.launch {
            try {
                val sess = quizRepository.getSessionById(sessionId) ?: return@launch
                currentSession = sess
                // attempt to parse details
                val details = sess.detailsJson
                var resumeIndex = 0
                answerDetails.clear()
                if (!details.isNullOrBlank()) {
                    try {
                        val root = org.json.JSONObject(details)
                        if (root.optBoolean("paused", false)) {
                            resumeIndex = root.optInt("currentIndex", 0)
                            val arr = root.optJSONArray("answers") ?: org.json.JSONArray()
                            for (i in 0 until arr.length()) {
                                val o = arr.getJSONObject(i)
                                answerDetails.add(
                                    AnswerDetail(
                                        questionId = o.optString("questionId"),
                                        selectedAnswer = if (o.has("selectedAnswer") && !o.isNull("selectedAnswer")) o.optString("selectedAnswer") else null,
                                        isCorrect = o.optBoolean("isCorrect", false),
                                        timeRemaining = o.optInt("timeRemaining", 0)
                                    )
                                )
                            }
                        } else {
                            // previously saved as array only (completed session); fallback no resume
                        }
                    } catch (e: Exception) {
                        // ignore parse errors
                    }
                }

                // reload questions for the session
                val questionCount = if (sess.mode == com.excell44.educam.data.model.QuizMode.FAST) 10 else 20
                val questions = quizRepository.getQuestions(
                    subject = sess.subject ?: "",
                    gradeLevel = "Terminale",
                    count = questionCount
                )
                if (questions.isNotEmpty()) {
                    val idx = resumeIndex.coerceIn(0, questions.size - 1)
                    _uiState.value = _uiState.value.copy(
                        isQuizStarted = true,
                        isPaused = false,
                        questions = questions,
                        currentQuestionIndex = idx,
                        currentQuestion = questions[idx],
                        selectedAnswer = null,
                        score = answerDetails.count { it.isCorrect },
                        accumulatedTimeRemainingSeconds = 0L
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    fun getCurrentSessionId(): String? = currentSession?.id

    /**
     * Annule le quiz en cours et nettoie l'état.
     * Persiste la session comme annulée avec les données partielles.
     */
    fun cancelQuiz() {
        viewModelScope.launch {
            try {
                currentSession?.let { session ->
                    val state = _uiState.value
                    val wrapper = org.json.JSONObject()

                    // Marquer comme annulé
                    wrapper.put("cancelled", true)
                    wrapper.put("cancelledAt", System.currentTimeMillis())
                    wrapper.put("currentIndex", state.currentQuestionIndex)
                    wrapper.put("score", state.score)
                    wrapper.put("totalQuestions", state.totalQuestions)

                    // Sauvegarder les réponses déjà données
                    val arr = org.json.JSONArray()
                    for (d in answerDetails) {
                        val obj = org.json.JSONObject()
                        obj.put("questionId", d.questionId)
                        obj.put("selectedAnswer", d.selectedAnswer)
                        obj.put("isCorrect", d.isCorrect)
                        obj.put("timeRemaining", d.timeRemaining)
                        arr.put(obj)
                    }
                    wrapper.put("answers", arr)

                    // Mettre à jour la session comme annulée
                    val updated = session.copy(
                        detailsJson = wrapper.toString(),
                        isCompleted = false,
                        endTime = System.currentTimeMillis(),
                        score = state.score
                    )
                    quizRepository.updateSession(updated)

                    Logger.i("QuizViewModel", "Quiz cancelled - session ${session.id} saved with partial progress")
                }
            } catch (e: Exception) {
                Logger.e("QuizViewModel", "Error cancelling quiz", e)
            } finally {
                // Nettoyer l'état du quiz
                currentSession = null
                answerDetails.clear()
                _uiState.value = QuizUiState(
                    selectedMode = _uiState.value.selectedMode,
                    selectedSubject = _uiState.value.selectedSubject,
                    availableSubjects = _uiState.value.availableSubjects
                )
                Logger.d("QuizViewModel", "Quiz state cleaned after cancellation")
            }
        }
    }
}
