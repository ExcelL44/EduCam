package com.excell44.educam.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.excell44.educam.data.model.QuizSession
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizSessionDao {
    @Query("SELECT * FROM quiz_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getSessionsByUser(userId: String): Flow<List<QuizSession>>

    @Query("SELECT * FROM quiz_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): QuizSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: QuizSession)

    @Update
    suspend fun updateSession(session: QuizSession)
}

