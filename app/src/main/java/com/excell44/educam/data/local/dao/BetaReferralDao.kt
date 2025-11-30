package com.excell44.educam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.excell44.educam.data.local.entity.BetaReferralEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les opérations de parrainage Beta-User.
 * Gère les compteurs, quotas et tokens de parrainage.
 */
@Dao
interface BetaReferralDao {

    /**
     * Récupère les données de parrainage d'un utilisateur.
     */
    @Query("SELECT * FROM beta_referral WHERE userId = :userId LIMIT 1")
    fun getReferralData(userId: String): Flow<BetaReferralEntity?>

    /**
     * Récupère les données par token (pour les invitations).
     */
    @Query("SELECT * FROM beta_referral WHERE referralToken = :token LIMIT 1")
    suspend fun getByReferralToken(token: String): BetaReferralEntity?

    /**
     * Incrémente le compteur d'invitations pour un token.
     * @return nombre de lignes affectées (0 si token invalide).
     */
    @Query("UPDATE beta_referral SET currentCount = currentCount + 1, lastUpdated = :timestamp WHERE referralToken = :token AND currentCount < quota")
    suspend fun incrementCount(token: String, timestamp: Long = System.currentTimeMillis()): Int

    /**
     * Réinitialise après paiement réussi (niveau suivant).
     */
    @Transaction
    suspend fun resetAfterPayment(userId: String) {
        // Marquer la demande WhatsApp comme envoyée
        updateRequestSent(userId, true)
        // Réinitialiser le compteur et augmenter le quota
        val currentEntity = getReferralDataSync(userId)
        if (currentEntity != null) {
            val newQuota = currentEntity.quota + 5
            val newLevel = currentEntity.level + 1
            updateQuotaAndLevel(userId, newQuota, newLevel, 0)
        }
    }

    /**
     * Méthode synchrone pour getReferralData (pour les transactions).
     */
    @Query("SELECT * FROM beta_referral WHERE userId = :userId LIMIT 1")
    suspend fun getReferralDataSync(userId: String): BetaReferralEntity?

    /**
     * Met à jour le statut de demande WhatsApp.
     */
    @Query("UPDATE beta_referral SET whatsappRequestSent = :sent, lastUpdated = :timestamp WHERE userId = :userId")
    suspend fun updateRequestSent(userId: String, sent: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Met à jour quota, niveau et compteur.
     */
    @Query("UPDATE beta_referral SET quota = :quota, level = :level, currentCount = :count, lastUpdated = :timestamp WHERE userId = :userId")
    suspend fun updateQuotaAndLevel(userId: String, quota: Int, level: Int, count: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Active/désactive un utilisateur beta.
     */
    @Query("UPDATE beta_referral SET isActive = :active, lastUpdated = :timestamp WHERE userId = :userId")
    suspend fun updateActiveStatus(userId: String, active: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Insère ou met à jour les données de parrainage.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BetaReferralEntity)

    /**
     * Supprime les données de parrainage (pour nettoyage).
     */
    @Query("DELETE FROM beta_referral WHERE userId = :userId")
    suspend fun delete(userId: String)
}
