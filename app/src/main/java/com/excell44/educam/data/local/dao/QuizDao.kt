package com.excell44.educam.data.local.dao

import androidx.room.*
import com.excell44.educam.data.local.entity.QuizEntity
import com.excell44.educam.data.local.entity.QuizWithQuestions
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>
    
    @Query("SELECT * FROM quizzes WHERE isAIEnhanced = 1")
    fun getAIEnhancedQuizzes(): Flow<List<QuizEntity>>
    
    @Transaction
    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    fun getQuizWithQuestions(quizId: Long): Flow<QuizWithQuestions>

    @Transaction
    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    suspend fun getQuizWithQuestionsSync(quizId: Long): QuizWithQuestions
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long
    
    @Update
    suspend fun updateQuiz(quiz: QuizEntity)
    
    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuiz(quizId: Long)
}
