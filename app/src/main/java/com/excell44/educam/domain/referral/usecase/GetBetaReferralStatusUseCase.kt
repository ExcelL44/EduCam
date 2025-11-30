package com.excell44.educam.domain.referral.usecase

import com.excell44.educam.domain.referral.BetaReferralRepository
import com.excell44.educam.domain.referral.model.ReferralStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case pour récupérer le statut de parrainage d'un utilisateur.
 * Fournit un accès propre au repository depuis la couche présentation.
 */
class GetBetaReferralStatusUseCase @Inject constructor(
    private val repository: BetaReferralRepository
) {
    /**
     * Récupère le statut de parrainage pour un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @return Flow du statut de parrainage
     */
    operator fun invoke(userId: String): Flow<ReferralStatus> {
        return repository.getReferralStatus(userId)
    }
}
