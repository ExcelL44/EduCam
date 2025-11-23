package com.excell44.educam.data.repository

import com.excell44.educam.data.dao.QuizQuestionDao
import com.excell44.educam.data.dao.QuizSessionDao
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuizMode
import com.excell44.educam.data.model.QuizQuestion
import com.excell44.educam.data.model.QuizSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val quizQuestionDao: QuizQuestionDao,
    private val quizSessionDao: QuizSessionDao
) {
    suspend fun getQuestions(
        subject: String,
        gradeLevel: String,
        count: Int,
        difficulty: Difficulty? = null
    ): List<QuizQuestion> {
        return if (difficulty != null) {
            quizQuestionDao.getQuestionsByDifficulty(subject, gradeLevel, difficulty, count)
        } else {
            quizQuestionDao.getRandomQuestions(subject, gradeLevel, count)
        }
    }

    suspend fun createSession(
        userId: String,
        mode: QuizMode,
        subject: String? = null
    ): QuizSession {
        val session = QuizSession(
            id = UUID.randomUUID().toString(),
            userId = userId,
            mode = mode,
            subject = subject
        )
        quizSessionDao.insertSession(session)
        return session
    }

    suspend fun updateSession(session: QuizSession) {
        quizSessionDao.updateSession(session)
    }

    fun getSessionsByUser(userId: String): Flow<List<QuizSession>> {
        return quizSessionDao.getSessionsByUser(userId)
    }

    suspend fun getSessionById(sessionId: String): QuizSession? {
        return quizSessionDao.getSessionById(sessionId)
    }
}

