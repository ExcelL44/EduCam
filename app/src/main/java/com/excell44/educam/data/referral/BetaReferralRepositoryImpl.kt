package com.excell44.educam.data.referral

import android.content.Context
import android.util.Log
import com.excell44.educam.core.network.NetworkObserver
import com.excell44.educam.data.local.dao.BetaReferralDao
import com.excell44.educam.data.local.entity.BetaReferralEntity
import com.excell44.educam.domain.referral.BetaReferralRepository
import com.excell44.educam.domain.referral.model.ReferralStatus
import com.excell44.educam.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation du repository BetaReferral.
 * Gère la logique de données pour le système de parrainage.
 */
@Singleton
class BetaReferralRepositoryImpl @Inject constructor(
    private val dao: BetaReferralDao,
    private val networkObserver: NetworkObserver,
    @ApplicationContext private val context: Context
) : BetaReferralRepository {

    companion object {
        private const val TAG = "BetaReferralRepository"
    }

    override fun getReferralStatus(userId: String): Flow<ReferralStatus> = flow {
        try {
            // Priorité au local pour UI rapide
            dao.getReferralData(userId).collect { entity ->
                if (entity != null) {
                    emit(entity.toDomainModel())
                    Log.d(TAG, "Emitted local status for user $userId: ${entity.currentCount}/${entity.quota}")
                } else {
                    // Si pas de données locales, émettre un statut par défaut
                    val defaultStatus = ReferralStatus(
                        userId = userId,
                        currentCount = 0,
                        quota = 5,
                        level = 1,
                        whatsappRequestSent = false,
                        isActive = false // Pas encore activé
                    )
                    emit(defaultStatus)
                    Log.d(TAG, "Emitted default status for user $userId (not activated)")
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting referral status for $userId", e)
            // En cas d'erreur, émettre un statut d'erreur
            emit(ReferralStatus(
                userId = userId,
                isActive = false
            ))
        }
    }

    override suspend fun requestPayment(userId: String): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                Log.w(TAG, "Cannot request payment offline for user $userId")
                return Result.failure(NetworkException("Connexion internet requise"))
            }

            // Vérifier que l'utilisateur peut faire une demande
            val currentData = dao.getReferralDataSync(userId)
            if (currentData == null) {
                Log.w(TAG, "User $userId not found in referral system")
                return Result.failure(IllegalStateException("Système de parrainage non activé"))
            }

            if (currentData.currentCount < currentData.quota) {
                Log.w(TAG, "User $userId hasn't reached quota: ${currentData.currentCount}/${currentData.quota}")
                return Result.failure(IllegalStateException("Quota non atteint"))
            }

            if (currentData.whatsappRequestSent) {
                Log.w(TAG, "User $userId already sent WhatsApp request")
                return Result.failure(IllegalStateException("Demande déjà envoyée"))
            }

            // Ouvrir WhatsApp avec le message prédéfini
            openWhatsAppForPayment(userId, currentData.level)

            // Marquer la demande comme envoyée
            dao.updateRequestSent(userId, true)
            Log.i(TAG, "Payment request sent for user $userId (level ${currentData.level})")

            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Error requesting payment for $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun incrementReferral(token: String): Result<Unit> {
        return try {
            val affectedRows = dao.incrementCount(token)
            if (affectedRows > 0) {
                Log.i(TAG, "Incremented referral count for token: $token")
                // TODO: Sync avec remote si implémenté
                Result.success(Unit)
            } else {
                Log.w(TAG, "Invalid or quota reached for token: $token")
                Result.failure(InvalidTokenException("Token invalide ou quota atteint"))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error incrementing referral for token $token", e)
            Result.failure(e)
        }
    }

    override suspend fun generateReferralToken(userId: String): String {
        val token = generateUniqueToken()
        Log.d(TAG, "Generated referral token for user $userId: $token")
        return token
    }

    override suspend fun activateBetaUser(userId: String): Result<String> {
        return try {
            // Vérifier si déjà activé
            val existing = dao.getReferralDataSync(userId)
            if (existing != null) {
                Log.w(TAG, "User $userId already activated")
                return Result.failure(IllegalStateException("Utilisateur déjà activé"))
            }

            // Générer token et créer l'entrée
            val token = generateReferralToken(userId)
            val entity = BetaReferralEntity(
                userId = userId,
                referralToken = token,
                currentCount = 0,
                quota = 5,
                level = 1,
                whatsappRequestSent = false,
                isActive = true
            )

            dao.insert(entity)
            Log.i(TAG, "Activated beta user $userId with token $token")
            Result.success(token)
        } catch (e: Exception) {
            Logger.e(TAG, "Error activating beta user $userId", e)
            Result.failure(e)
        }
    }

    // --- Helper Methods ---

    private fun BetaReferralEntity.toDomainModel(): ReferralStatus {
        return ReferralStatus(
            userId = userId,
            currentCount = currentCount,
            quota = quota,
            level = level,
            whatsappRequestSent = whatsappRequestSent,
            isActive = isActive
        )
    }

    private fun isNetworkAvailable(): Boolean {
        return networkObserver.isOnline()
    }

    private fun generateUniqueToken(): String {
        return UUID.randomUUID().toString().substring(0, 8).uppercase()
    }

    private fun openWhatsAppForPayment(userId: String, level: Int) {
        try {
            val phoneNumber = "+22912345678" // TODO: Configurable
            val message = """
                *DEMANDE DE PAIEMENT BETA-USER*
                User ID: $userId
                Niveau atteint: $level
                Palier: ${level * 5} filleuls
                Date: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date())}
            """.trimIndent()

            val uri = android.net.Uri.parse("https://wa.me/${phoneNumber.removePrefix("+")}?text=${android.net.Uri.encode(message)}")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            Log.d(TAG, "Opened WhatsApp for payment request: $userId")
        } catch (e: Exception) {
            Logger.e(TAG, "Error opening WhatsApp for user $userId", e)
            throw e
        }
    }
}

// --- Custom Exceptions ---

class NetworkException(message: String) : Exception(message)
class InvalidTokenException(message: String) : Exception(message)
