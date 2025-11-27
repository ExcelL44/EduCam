package com.excell44.educam.domain.usecase

import com.excell44.educam.data.local.entity.QuizResultEntity
import com.excell44.educam.data.repository.AIRepository
import com.excell44.educam.domain.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubmitQuizWithAIFeedbackUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        quizId: Long,
        userAnswers: Map<Long, String>
    ): Result<QuizResultEntity> = withContext(Dispatchers.Default) {
        try {
            // 1. Récupérer les détails du quiz (questions + réponses correctes)
            val quizDetails = quizRepository.getQuizDetailsSync(quizId)
            
            // 2. Calculer le score
            var score = 0
            var maxScore = 0
            
            quizDetails.questions.forEach { questionWithAnswers ->
                val question = questionWithAnswers.question
                val correctAnswers = questionWithAnswers.answers.filter { it.isCorrect }.map { it.answerText }
                val userAnswer = userAnswers[question.id]
                
                maxScore += question.points
                
                // Vérification simple (exact match)
                if (userAnswer != null && correctAnswers.any { it.equals(userAnswer, ignoreCase = true) }) {
                    score += question.points
                }
            }
            
            // 3. Générer feedback IA
            val feedback = aiRepository.generateFeedback(score, maxScore)
            
            // 4. Sauvegarder le résultat
            // Conversion simple de la map en string pour le stockage (JSON-like)
            val answersJson = userAnswers.entries.joinToString(separator = ";") { "${it.key}:${it.value}" }
            
            val result = QuizResultEntity(
                quizId = quizId,
                score = score,
                maxScore = maxScore,
                completionTime = 0, // À implémenter si on track le temps
                userAnswers = answersJson,
                aiFeedback = feedback
            )
            
            quizRepository.saveQuizResult(result)
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
