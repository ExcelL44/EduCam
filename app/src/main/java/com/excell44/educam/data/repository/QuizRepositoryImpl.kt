package com.excell44.educam.data.repository

import com.excell44.educam.data.local.dao.QuestionDao
import com.excell44.educam.data.local.dao.QuizDao
import com.excell44.educam.data.local.dao.QuizResultDao
import com.excell44.educam.data.local.entity.QuizEntity
import com.excell44.educam.data.local.entity.QuizResultEntity
import com.excell44.educam.data.local.entity.QuizWithQuestions
import com.excell44.educam.domain.repository.QuizRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val quizDao: QuizDao,
    private val questionDao: QuestionDao,
    private val resultDao: QuizResultDao,
    private val aiRepository: AIRepository
) : QuizRepository {
    
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun getQuizzes(): Flow<List<QuizEntity>> {
        return quizDao.getAllQuizzes()
            .flowOn(ioDispatcher)
    }
    
    override fun getQuizDetails(id: Long): Flow<QuizWithQuestions> {
        return quizDao.getQuizWithQuestions(id)
            .flowOn(ioDispatcher)
    }

    override suspend fun getQuizDetailsSync(id: Long): QuizWithQuestions = withContext(ioDispatcher) {
        quizDao.getQuizWithQuestionsSync(id)
    }
    
    override suspend fun saveQuizResult(result: QuizResultEntity) = withContext(ioDispatcher) {
        resultDao.insertResult(result)
        Unit
    }

    override fun getResultsForQuiz(quizId: Long): Flow<List<QuizResultEntity>> {
        return resultDao.getResultsForQuiz(quizId)
            .flowOn(ioDispatcher)
    }
}
