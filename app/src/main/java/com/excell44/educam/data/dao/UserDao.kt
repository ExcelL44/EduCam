package com.excell44.educam.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.excell44.educam.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE pseudo = :pseudo LIMIT 1")
    suspend fun getUserByPseudo(pseudo: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET gradeLevel = :gradeLevel WHERE id = :userId")
    suspend fun updateGradeLevel(userId: String, gradeLevel: String)

    @Query("SELECT COUNT(*) FROM users WHERE isOfflineAccount = 1")
    suspend fun countOfflineUsers(): Int
    
    @Query("DELETE FROM users WHERE syncStatus != 'SYNCED' AND createdAt < :expiryTimestamp")
    suspend fun deleteExpiredUnsyncedUsers(expiryTimestamp: Long): Int
    
    @Query("SELECT * FROM users WHERE syncStatus != 'SYNCED' AND createdAt < :expiryTimestamp")
    suspend fun getExpiredUnsyncedUsers(expiryTimestamp: Long): List<User>
}

