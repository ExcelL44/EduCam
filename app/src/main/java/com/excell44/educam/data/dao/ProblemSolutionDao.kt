package com.excell44.educam.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.excell44.educam.data.model.ProblemSolution
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemSolutionDao {
    @Query("SELECT * FROM problem_solutions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSolutionsByUser(userId: String): Flow<List<ProblemSolution>>

    @Query("SELECT * FROM problem_solutions WHERE id = :id")
    suspend fun getSolutionById(id: String): ProblemSolution?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolution(solution: ProblemSolution)
}

