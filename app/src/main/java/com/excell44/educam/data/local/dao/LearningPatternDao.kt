package com.excell44.educam.data.local.dao

import androidx.room.*
import com.excell44.educam.data.local.entity.LearningPatternEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les patterns d'apprentissage de l'IA.
 * Optimisé pour apprentissage incrémental et faible mémoire.
 */
@Dao
interface LearningPatternDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: LearningPatternEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<LearningPatternEntity>)

    @Update
    suspend fun updatePattern(pattern: LearningPatternEntity)

    @Query("SELECT * FROM learning_patterns WHERE userId = :userId ORDER BY usageCount DESC, successRate DESC LIMIT :limit")
    suspend fun getBestPatterns(userId: String, limit: Int = 20): List<LearningPatternEntity>

    @Query("SELECT * FROM learning_patterns WHERE userId = :userId AND inputPattern LIKE '%' || :keyword || '%' ORDER BY usageCount DESC")
    suspend fun searchPatterns(userId: String, keyword: String): List<LearningPatternEntity>

    @Query("SELECT * FROM learning_patterns WHERE userId = :userId AND subject = :subject ORDER BY successRate DESC")
    suspend fun getPatternsBySubject(userId: String, subject: String): List<LearningPatternEntity>

    @Query("SELECT * FROM learning_patterns WHERE userId = :userId AND isSynced = 0 ORDER BY syncPriority DESC, usageCount DESC")
    suspend fun getUnsyncedPatterns(userId: String): List<LearningPatternEntity>

    @Query("UPDATE learning_patterns SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("UPDATE learning_patterns SET usageCount = usageCount + 1, lastUsed = :timestamp WHERE id = :id")
    suspend fun incrementUsage(id: Long, timestamp: Long)

    @Query("UPDATE learning_patterns SET successRate = :newRate WHERE id = :id")
    suspend fun updateSuccessRate(id: Long, newRate: Float)

    // Nettoyage pour limiter la mémoire
    @Query("DELETE FROM learning_patterns WHERE userId = :userId AND usageCount < 2 AND firstLearned < :before")
    suspend fun deleteOldUnusedPatterns(userId: String, before: Long): Int

    // Statistiques d'apprentissage
    @Query("SELECT COUNT(*) FROM learning_patterns WHERE userId = :userId")
    suspend fun getPatternCount(userId: String): Int

    @Query("SELECT AVG(successRate) FROM learning_patterns WHERE userId = :userId AND usageCount > 1")
    suspend fun getAverageSuccessRate(userId: String): Float?

    @Query("SELECT subject, COUNT(*) as count FROM learning_patterns WHERE userId = :userId GROUP BY subject ORDER BY count DESC")
    suspend fun getSubjectDistribution(userId: String): List<SubjectCount>

    // Optimisation mémoire : limite à 500 patterns par utilisateur
    @Query("""
        DELETE FROM learning_patterns
        WHERE userId = :userId AND id NOT IN (
            SELECT id FROM learning_patterns
            WHERE userId = :userId
            ORDER BY usageCount DESC, successRate DESC, lastUsed DESC
            LIMIT 500
        )
    """)
    suspend fun cleanupExcessPatterns(userId: String): Int
}

/**
 * Résultat pour la distribution des sujets.
 */
data class SubjectCount(
    val subject: String?,
    val count: Int
)
