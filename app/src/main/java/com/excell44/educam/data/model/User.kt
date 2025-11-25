package com.excell44.educam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val passwordHash: String, // En production, utiliser un hash sécurisé
    val name: String,
    val gradeLevel: String = "", // Niveau d'étude
    val createdAt: Long = System.currentTimeMillis(),
    val isOfflineAccount: Boolean = false,
    val trialExpiresAt: Long? = null, // Null si pas d'essai ou illimité
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_CREATE, PENDING_UPDATE
    val role: String = "USER" // USER, ADMIN, BETA, ACTIVE
) {
    fun getUserMode(): UserMode {
        return when {
            role == "ADMIN" -> UserMode.ADMIN
            role == "BETA" -> UserMode.BETA_T
            role == "ACTIVE" -> UserMode.ACTIVE
            isOfflineAccount -> {
                val now = System.currentTimeMillis()
                if (trialExpiresAt != null && now < trialExpiresAt) {
                    UserMode.PASSIVE
                } else {
                    UserMode.GUEST
                }
            }
            else -> UserMode.ACTIVE // Default for online users
        }
    }
}

