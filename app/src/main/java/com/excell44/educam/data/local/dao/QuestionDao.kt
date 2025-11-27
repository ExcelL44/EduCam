package com.excell44.educam.data.local.dao

import androidx.room.*
import com.excell44.educam.data.local.entity.QuestionEntity
import com.excell44.educam.data.local.entity.QuestionWithAnswers
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Transaction
    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    fun getQuestionsWithAnswers(quizId: Long): Flow<List<QuestionWithAnswers>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long
}
