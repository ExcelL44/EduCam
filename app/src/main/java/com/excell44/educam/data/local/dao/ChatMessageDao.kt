package com.excell44.educam.data.local.dao

import androidx.room.*
import com.excell44.educam.data.local.entity.ChatMessageEntity
import com.excell44.educam.data.local.entity.MessageType
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les messages de chat.
 * Optimisé pour performance offline et faible mémoire.
 */
@Dao
interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(userId: String, limit: Int = 50): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND timestamp > :since ORDER BY timestamp ASC")
    fun getMessagesSince(userId: String, since: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE userId = :userId")
    suspend fun getMessageCount(userId: String): Int

    @Query("DELETE FROM chat_messages WHERE userId = :userId AND timestamp < :before")
    suspend fun deleteOldMessages(userId: String, before: Long): Int

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND isFromUser = 0 AND confidence > 0.7 ORDER BY confidence DESC LIMIT 20")
    suspend fun getBestLearnedResponses(userId: String): List<ChatMessageEntity>

    // Nettoyage périodique (garde seulement 100 derniers messages par utilisateur)
    @Query("""
        DELETE FROM chat_messages
        WHERE userId = :userId AND id NOT IN (
            SELECT id FROM chat_messages
            WHERE userId = :userId
            ORDER BY timestamp DESC
            LIMIT 100
        )
    """)
    suspend fun cleanupOldMessages(userId: String): Int

    // Statistiques pour analytics
    @Query("SELECT COUNT(*) FROM chat_messages WHERE userId = :userId AND isFromUser = 1")
    suspend fun getUserMessageCount(userId: String): Int

    @Query("SELECT AVG(confidence) FROM chat_messages WHERE userId = :userId AND isFromUser = 0")
    suspend fun getAverageConfidence(userId: String): Float?
}
