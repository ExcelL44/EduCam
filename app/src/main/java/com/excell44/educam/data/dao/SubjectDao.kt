package com.excell44.educam.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.excell44.educam.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE subject = :subject AND gradeLevel = :gradeLevel ORDER BY createdAt DESC")
    fun getSubjectsBySubjectAndGrade(subject: String, gradeLevel: String): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: String): Subject?

    @Query("SELECT DISTINCT subject FROM subjects")
    fun getAllSubjects(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)
}

