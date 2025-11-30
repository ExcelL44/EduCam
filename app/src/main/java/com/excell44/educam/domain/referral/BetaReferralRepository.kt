package com.excell44.educam.domain.referral

import com.excell44.educam.domain.referral.model.ReferralStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interface de domaine pour le système de parrainage Beta-User.
 * Définit les opérations métier pour le système de récompenses.
 */
interface BetaReferralRepository {

    /**
     * Récupère le statut de parrainage d'un utilisateur.
     * Utilise une approche offline-first avec sync en arrière-plan.
     *
     * @param userId ID de l'utilisateur
     * @return Flow du statut de parrainage
     */
    fun getReferralStatus(userId: String): Flow<ReferralStatus>

    /**
     * Envoie une demande de paiement WhatsApp à l'administrateur.
     * Met à jour le statut local et déclenche la sync.
     *
     * @param userId ID de l'utilisateur
     * @return Result indiquant le succès ou l'échec
     */
    suspend fun requestPayment(userId: String): Result<Unit>

    /**
     * Incrémente le compteur de parrainage pour un token.
     * Utilisé lorsque quelqu'un s'inscrit avec un token de parrainage.
     *
     * @param token Token de parrainage
     * @return Result indiquant le succès ou l'échec
     */
    suspend fun incrementReferral(token: String): Result<Unit>

    /**
     * Génère un nouveau token de parrainage pour un utilisateur.
     * Utilisé lors de l'activation initiale du système beta.
     *
     * @param userId ID de l'utilisateur
     * @return Le token généré
     */
    suspend fun generateReferralToken(userId: String): String

    /**
     * Active le système de parrainage pour un utilisateur.
     * Crée l'entrée initiale dans la base de données.
     *
     * @param userId ID de l'utilisateur
     * @return Result indiquant le succès ou l'échec
     */
    suspend fun activateBetaUser(userId: String): Result<String>
}
