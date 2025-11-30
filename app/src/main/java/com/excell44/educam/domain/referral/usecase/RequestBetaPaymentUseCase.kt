package com.excell44.educam.domain.referral.usecase

import com.excell44.educam.domain.referral.BetaReferralRepository
import javax.inject.Inject

/**
 * Use Case pour envoyer une demande de paiement WhatsApp.
 * Gère la logique métier de demande de récompense.
 */
class RequestBetaPaymentUseCase @Inject constructor(
    private val repository: BetaReferralRepository
) {
    /**
     * Envoie une demande de paiement pour un utilisateur.
     * Cette opération peut échouer si l'utilisateur n'a pas atteint le quota,
     * ou en cas de problème réseau/admin.
     *
     * @param userId ID de l'utilisateur
     * @return Result indiquant le succès ou l'échec
     */
    suspend operator fun invoke(userId: String): Result<Unit> {
        return try {
            repository.requestPayment(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
