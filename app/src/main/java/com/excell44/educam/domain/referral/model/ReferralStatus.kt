package com.excell44.educam.domain.referral.model

/**
 * Modèle de domaine pour le statut de parrainage Beta-User.
 * Représente l'état actuel du système de récompenses pour un utilisateur.
 */
data class ReferralStatus(
    val userId: String,
    val currentCount: Int = 0,
    val quota: Int = 5,
    val level: Int = 1,
    val whatsappRequestSent: Boolean = false,
    val isActive: Boolean = true
) {
    /**
     * Progrès actuel sous forme de float (0.0 à 1.0).
     */
    val progress: Float = if (quota > 0) currentCount.toFloat() / quota else 0f

    /**
     * Texte d'affichage du compteur (ex: "3/5").
     */
    val displayText: String = "$currentCount/$quota"

    /**
     * Indique si le bouton cadeau doit être visible.
     */
    val isGiftButtonVisible: Boolean = currentCount >= quota && !whatsappRequestSent

    /**
     * Indique si le bouton cadeau doit être activé.
     */
    val isGiftButtonEnabled: Boolean = isGiftButtonVisible && isActive

    /**
     * Message d'état pour l'utilisateur.
     */
    val statusMessage: String = when {
        !isActive -> "En attente d'activation admin"
        whatsappRequestSent -> "Demande de paiement envoyée"
        currentCount >= quota -> "Prêt à réclamer votre récompense !"
        else -> "Invitez ${quota - currentCount} ami(s) de plus"
    }
}
