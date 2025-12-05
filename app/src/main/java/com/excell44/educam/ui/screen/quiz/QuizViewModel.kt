package com.excell44.educam.ui.screen.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.data.dao.QuizQuestionDao
import com.excell44.educam.data.dao.QuizSessionDao
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuizMode
import com.excell44.educam.data.model.QuizQuestion
import com.excell44.educam.data.model.QuizSession
import com.excell44.educam.util.AuthStateManager
import com.excell44.educam.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class QuizUiState(
    val selectedMode: QuizMode? = null,
    val selectedSubject: String? = null,
    val availableSubjects: List<SubjectInfo> = listOf(
        SubjectInfo("Mathématiques", "📐 Mathématiques", true),
        SubjectInfo("Physique", "⚛️ Physique", true),
        SubjectInfo("Chimie", "🧪 Chimie", true),
        SubjectInfo("Biologie", "🧬 Biologie", false),
        SubjectInfo("Histoire", "📚 Histoire", false),
        SubjectInfo("Géographie", "🌍 Géographie", false),
        SubjectInfo("Français", "✍️ Français", false),
        SubjectInfo("Anglais", "🇬🇧 Anglais", false),
        SubjectInfo("Philosophie", "🤔 Philosophie", false)
    ),
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

data class SubjectInfo(
    val name: String,
    val displayName: String,
    val isAvailable: Boolean
)

data class AnswerDetail(
    val questionId: String,
    val selectedAnswer: String?,
    val isCorrect: Boolean,
    val timeRemaining: Int
)


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizQuestionDao: QuizQuestionDao,
    private val quizSessionDao: QuizSessionDao,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Keep reference to the currently active session so we can update it when quiz completes
    private var currentSession: com.excell44.educam.data.model.QuizSession? = null
    // accumulate per-question details during the quiz
    private val answerDetails = mutableListOf<AnswerDetail>()

    init {
        // Vérifier et peupler la base de données si nécessaire (BUGFIX: quiz loading indefinitely)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mathCount = quizQuestionDao.getRandomQuestions("Mathématiques", "Terminale", 1).size
                val physCount = quizQuestionDao.getRandomQuestions("Physique", "Terminale", 1).size
                val chimCount = quizQuestionDao.getRandomQuestions("Chimie", "Terminale", 1).size
                
                if (mathCount == 0 || physCount == 0 || chimCount == 0) {
                    Logger.w("QuizViewModel", "Database empty or incomplete! Seeding questions... (Math: $mathCount, Phys: $physCount, Chim: $chimCount)")
                    seedDatabaseQuestions()
                } else {
                    Logger.i("QuizViewModel", "Database OK: Math=$mathCount, Phys=$physCount, Chim=$chimCount")
                }
            } catch (e: Exception) {
                Logger.e("QuizViewModel", "Error checking database state", e)
            }
        }
    }

    private suspend fun seedDatabaseQuestions() {
        try {
            val questions = mutableListOf<com.excell44.educam.data.model.QuizQuestion>()
            
            // Questions Mathématiques
            questions.add(com.excell44.educam.data.model.QuizQuestion(
                id = "math_seed_001",
                subject = "Mathématiques",
                topic = "Équations du premier degré",
                question = "Résoudre l'équation: 2x + 5 = 17",
                questionType = com.excell44.educam.data.model.QuestionType.MULTIPLE_CHOICE,
                options = listOf("x = 16", "x = 6", "x = 11", "x = 2"),
                correctAnswer = "x = 6",
                explanation = "Soustraire 5 des deux membres: 2x = 12, puis diviser par 2: x = 6",
                difficulty = Difficulty.EASY,
                gradeLevel = "Terminale"
            ))
            questions.add(com.excell44.educam.data.model.QuizQuestion(
                id = "math_seed_002",
                subject = "Mathématiques",
                topic = "Théorème de Pythagore",
                question = "Dans un triangle rectangle, si les cathètes mesurent 3 cm et 4 cm, l'hypoténuse mesure:",
                questionType = com.excell44.educam.data.model.QuestionType.MULTIPLE_CHOICE,
                options = listOf("5 cm", "7 cm", "1 cm", "12 cm"),
                correctAnswer = "5 cm",
                explanation = "a² + b² = c² donc 3² + 4² = c² => 9 + 16 = c² => c² = 25 => c = 5",
                difficulty = Difficulty.EASY,
                gradeLevel = "Terminale"
            ))
            questions.add(com.excell44.educam.data.model.QuizQuestion(
                id = "math_seed_003",
                subject = "Mathématiques",
                topic = "Dérivation",
                question = "La dérivée de f(x) = x² + 3x + 2 est:",
                questionType = com.excell44.educam.data.model.QuestionType.MULTIPLE_CHOICE,
                options = listOf("f'(x) = 2x + 3", "f'(x) = x² + 3", "f'(x) = 2x + 2", "f'(x) = x² + 3x"),
                correctAnswer = "f'(x) = 2x + 3",
                explanation = "Dérivée de x² = 2x, dérivée de 3x = 3, dérivée de 2 = 0",
                difficulty = Difficulty.MEDIUM,
                gradeLevel = "Terminale"
            ))
            
            // Questions Physique
            questions.add(com.excell44.educam.data.model.QuizQuestion(
                id = "phys_seed_001",
                subject = "Physique",
                topic = "Électricité",
                question = "Quelle est l'unité d'intensité électrique?",
                questionType = com.excell44.educam.data.model.QuestionType.MULTIPLE_CHOICE,
                options = listOf("Volt", "Ampère", "Watt", "Ohm"),
                correctAnswer = "Ampère",
                explanation = "L'unité d'intensité électrique est l'Ampère (A)",
                difficulty = Difficulty.EASY,
                gradeLevel = "Terminale"
            ))
            questions.add(com.excell44.educam.data.model.QuizQuestion(
                id = "phys_seed_002",
                subject = "Physique",
                topic = "Électromagnétisme",
                question = "Selon la loi de Coulomb, la force électrique entre deux charges est proportionnelle à:",
                questionType = com.excell44.educam.data.model.QuestionType.MULTIPLE_CHOICE,
                options = listOf("Le produit des charges divisé par le carré de la distance", "La somme des charges divisée par la distance", "Le carré des charges multiplié par la distance", "La différence des charges multipliée par la distance"),
                correctAnswer = "Le produit des charges divisé par le carré de la distance",
                explanation = "F = k × q₁ × q₂ / r²",
                difficulty = Difficulty.MEDIUM,
                gradeLevel = "Terminale"
            ))
            
            // Questions Chimie
            questions.add(com.excell44.educam.data.model.QuizQuestion(
                id = "chim_seed_001",
                subject = "Chimie",
                topic = "Structure atomique",
                question = "Combien y a-t-il d'électrons dans un atome de carbone neutre?",
                questionType = com.excell44.educam.data.model.QuestionType.MULTIPLE_CHOICE,
                options = listOf("4", "6", "8", "12"),
                correctAnswer = "6",
                explanation = "Le numéro atomique du carbone est 6, donc il a 6 électrons",
                difficulty = Difficulty.EASY,
                gradeLevel = "Terminale"
            ))
            questions.add(com.excell44.educam.data.model.QuizQuestion(
                id = "chim_seed_002",
                subject = "Chimie",
                topic = "Configuration électronique",
                question = "Quelle est la configuration électronique de l'oxygène?",
                questionType = com.excell44.educam.data.model.QuestionType.MULTIPLE_CHOICE,
                options = listOf("1s² 2s² 2p⁴", "1s² 2s² 2p⁶", "1s² 2s² 2p²", "1s² 2s⁴ 2p²"),
                correctAnswer = "1s² 2s² 2p⁴",
                explanation = "Numéro atomique 8: 1s² 2s² 2p⁴",
                difficulty = Difficulty.MEDIUM,
                gradeLevel = "Terminale"
            ))
            
            quizQuestionDao.insertQuestions(questions)
            Logger.i("QuizViewModel", "Successfully seeded ${questions.size} questions to database")
        } catch (e: Exception) {
            Logger.e("QuizViewModel", "Failed to seed database", e)
        }
    }

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
        
        // ✅ TRIAL users: Limited to 3 quizzes per day (freemium strategy)
        val accountType = authStateManager.getAccountType()
        val effectiveUserId: String? = authStateManager.getUserId()
        if (effectiveUserId == null) return
        
        viewModelScope.launch {
            // Check TRIAL limits before starting quiz
            if (accountType == "PASSIVE") {
                try {
                    val quizzesToday = getQuizCountToday(effectiveUserId)
                    if (quizzesToday >= 3) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Limite de 3 quiz atteinte aujourd'hui. Passez Premium pour un accès illimité ! 🚀"
                        )
                        Logger.w("QuizViewModel", "TRIAL user hit daily quiz limit: $effectiveUserId ($quizzesToday/3)")
                        return@launch
                    }
                    Logger.d("QuizViewModel", "TRIAL user: $quizzesToday/3 quizzes today")
                } catch (e: Exception) {
                    Logger.e("QuizViewModel", "Error checking quiz limit", e)
                    // En cas d'erreur, autoriser (graceful degradation)
                }
            }
            
            val subject = state.selectedSubject ?: return@launch
            val mode = state.selectedMode ?: QuizMode.FAST

            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Créer une session
                val session = QuizSession(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = effectiveUserId,
                    mode = mode,
                    subject = subject
                )
                quizSessionDao.insertSession(session)
                currentSession = session
                // reset per-quiz accumulators
                answerDetails.clear()
                // Reset quiz state

                // Charger les questions (adaptatif selon le mode)
                val questionCount = if (mode == QuizMode.FAST) 10 else 20
                val questions = quizQuestionDao.getRandomQuestions(subject, "Terminale", questionCount)

                Logger.d("QuizViewModel", "Loaded ${questions.size} questions for subject=$subject, mode=$mode (requested=$questionCount)")

                if (questions.isNotEmpty()) {
                    Logger.i("QuizViewModel", "Quiz started successfully: ${questions.size} questions loaded")
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
                    Logger.w("QuizViewModel", "No questions found for subject=$subject, gradeLevel=Terminale")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Aucune question disponible pour cette matière"
                    )
                }
            } catch (e: Exception) {
                Logger.e("QuizViewModel", "Error starting quiz", e)
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
                    quizSessionDao.updateSession(updated)
                    }
                } catch (_: Exception) {
                }
            }

            // ✅ REMOVED: No more attempt counting for any user type
            // All users (ADMIN, BETA_T, ACTIVE, TRIAL) have unlimited access
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

    // ✅ REMOVED: All GUEST-related functions (guestAttemptsRemaining, isGuestMode, hint limits)
    // All users now have unlimited access and hints

    fun canShowHint(): Boolean {
        // ✅ SIMPLIFIED: All users have unlimited hints
        return true
    }

    fun recordHintUsed() {
        // ✅ REMOVED: No more hint counting for any user type
        // hintsUsed++ // No longer needed
    }

    /**
     * Get quiz count for today (TRIAL limit enforcement)
     */
    private suspend fun getQuizCountToday(userId: String): Int {
        return withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val today = java.time.LocalDate.now()
                val startOfDay = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                val sessions = quizSessionDao.getSessionsByUser(userId).first()
                val completedToday = sessions.count { session ->
                    session.startTime >= startOfDay && session.isCompleted
                }

                completedToday
            } catch (e: Exception) {
                Logger.e("QuizViewModel", "Error counting quizzes for today", e)
                0 // En cas d'erreur, autoriser (graceful degradation)
            }
        }
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
                    quizSessionDao.updateSession(updated)
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
                val sess = quizSessionDao.getSessionById(sessionId) ?: return@launch
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
                val questions = quizQuestionDao.getRandomQuestions(
                    sess.subject ?: "",
                    "Terminale",
                    questionCount
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
                    quizSessionDao.updateSession(updated)

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
    /**
     * Soumet les résultats finaux du quiz (pour le mode découplé QuizFlowCoordinator)
     */
    fun submitFinalResults(score: Int, totalQuestions: Int, answers: List<AnswerDetail>) {
        viewModelScope.launch {
            try {
                currentSession?.let { sess ->
                    // build details JSON
                    val arr = org.json.JSONArray()
                    for (d in answers) {
                        val obj = org.json.JSONObject()
                        obj.put("questionId", d.questionId)
                        obj.put("selectedAnswer", d.selectedAnswer)
                        obj.put("isCorrect", d.isCorrect)
                        obj.put("timeRemaining", d.timeRemaining)
                        arr.put(obj)
                    }
                    
                    val updated = sess.copy(
                        score = score,
                        totalQuestions = totalQuestions,
                        endTime = System.currentTimeMillis(),
                        isCompleted = true,
                        detailsJson = arr.toString()
                    )
                    quizSessionDao.updateSession(updated)
                    Logger.i("QuizViewModel", "Quiz results submitted successfully: $score/$totalQuestions")
                }
            } catch (e: Exception) {
                Logger.e("QuizViewModel", "Error submitting quiz results", e)
            }
        }
    }
}
