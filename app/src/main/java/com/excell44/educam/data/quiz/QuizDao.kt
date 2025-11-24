package com.excell44.educam.data.quiz

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(q: Question): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<OptionEntity>)

    @Transaction
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int): List<QuestionWithOptions>

    @Transaction
    @Query("SELECT * FROM questions WHERE subject = :subject ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsBySubject(subject: String, limit: Int): List<QuestionWithOptions>

    @Query("SELECT * FROM options WHERE questionId = :questionId ORDER BY `index` ASC")
    suspend fun getOptionsForQuestion(questionId: Long): List<OptionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun countQuestions(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizSession(session: QuizSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserAnalytics(analytics: UserAnalytics)

}
