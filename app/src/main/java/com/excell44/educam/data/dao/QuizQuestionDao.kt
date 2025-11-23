package com.excell44.educam.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuizQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizQuestionDao {
    @Query("SELECT * FROM quiz_questions WHERE subject = :subject AND gradeLevel = :gradeLevel ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(subject: String, gradeLevel: String, limit: Int): List<QuizQuestion>

    @Query("SELECT * FROM quiz_questions WHERE subject = :subject AND gradeLevel = :gradeLevel AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByDifficulty(
        subject: String,
        gradeLevel: String,
        difficulty: Difficulty,
        limit: Int
    ): List<QuizQuestion>

    @Query("SELECT * FROM quiz_questions WHERE id = :id")
    suspend fun getQuestionById(id: String): QuizQuestion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuizQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestion>)
}

