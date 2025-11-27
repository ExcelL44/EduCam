package com.excell44.educam.domain.repository

import com.excell44.educam.data.local.entity.QuizEntity
import com.excell44.educam.data.local.entity.QuizResultEntity
import com.excell44.educam.data.local.entity.QuizWithQuestions
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    fun getQuizzes(): Flow<List<QuizEntity>>
    fun getQuizDetails(id: Long): Flow<QuizWithQuestions>
    suspend fun getQuizDetailsSync(id: Long): QuizWithQuestions
    suspend fun saveQuizResult(result: QuizResultEntity)
    fun getResultsForQuiz(quizId: Long): Flow<List<QuizResultEntity>>
}
