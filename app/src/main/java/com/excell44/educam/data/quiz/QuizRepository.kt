package com.excell44.educam.data.quiz

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuizRepository private constructor(private val db: AppDatabase) {
    private val dao = db.quizDao()

    companion object {
        @Volatile
        private var INSTANCE: QuizRepository? = null

        fun getInstance(context: Context): QuizRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = QuizRepository(AppDatabase.getInstance(context))
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun getQuestionsForQuickMode(count: Int = 10): List<QuestionWithOptions> = withContext(Dispatchers.IO) {
        dao.getRandomQuestions(count)
    }

    suspend fun getQuestionsForSubject(subject: String, count: Int = 10): List<QuestionWithOptions> = withContext(Dispatchers.IO) {
        dao.getRandomQuestionsBySubject(subject, count)
    }

    /**
     * A simple adaptive selection stub: returns a mix of random and subject-specific questions.
     * Detailed adaptive logic will use UserAnalytics and question metadata later.
     */
    suspend fun getQuestionsForAdaptiveMode(userId: String?, subject: String?, count: Int): List<QuestionWithOptions> = withContext(Dispatchers.IO) {
        // 20% random, 40% by strengths, 40% by weaknesses - for now we fallback to random/subset
        val rand = (count * 0.2).toInt().coerceAtLeast(1)
        val bySubject = (count * 0.8).toInt().coerceAtLeast(1)

        val a = dao.getRandomQuestions(rand)
        val b = if (!subject.isNullOrBlank()) dao.getRandomQuestionsBySubject(subject, bySubject) else dao.getRandomQuestions(bySubject)

        (a + b).take(count)
    }

    suspend fun insertQuestionWithOptions(q: Question, options: List<OptionEntity>) = withContext(Dispatchers.IO) {
        val qId = dao.insertQuestion(q)
        val opts = options.map { it.copy(questionId = qId) }
        dao.insertOptions(opts)
        qId
    }

    suspend fun countQuestions(): Int = withContext(Dispatchers.IO) {
        dao.countQuestions()
    }

    suspend fun insertSampleQuestions(samples: List<Pair<Question, List<OptionEntity>>>) = withContext(Dispatchers.IO) {
        for ((q, options) in samples) {
            insertQuestionWithOptions(q, options)
        }
    }

}
