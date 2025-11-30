package com.excell44.educam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room pour le système de parrainage Beta-User EduCam.
 * Permet de tracker les invitations et récompenses des beta-testeurs.
 */
@Entity(tableName = "beta_referral")
data class BetaReferralEntity(
    @PrimaryKey
    val userId: String,
    val referralToken: String,
    val currentCount: Int = 0,
    val quota: Int = 5,
    val level: Int = 1,
    val whatsappRequestSent: Boolean = false,
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
