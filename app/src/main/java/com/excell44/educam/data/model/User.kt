package com.excell44.educam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String, // Firebase UID (empty for offline, filled after sync)
    val localId: String = java.util.UUID.randomUUID().toString(), // Local UUID (NEVER conflicts)
    val pseudo: String, // Username/Pseudo (no email required - app for youth)
    val passwordHash: String, // PBKDF2 hash
    val salt: String = "", // Salt for PBKDF2 (empty for Firebase users)
    val name: String,
    val gradeLevel: String = "", // Niveau d'étude
    val createdAt: Long = System.currentTimeMillis(),
    val isOfflineAccount: Boolean = false,
    val trialExpiresAt: Long? = null, // Null si pas d'essai ou illimité
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_CREATE, PENDING_UPDATE
    val role: String = "PASSIVE", // ACTIVE (paid/synced), PASSIVE (trial/unsynced), ADMIN
    val lastSyncTimestamp: Long = 0L // Track when user was last synced to server
) {
    /**
     * Get the current user mode based on role and sync status.
     * - ACTIVE: Fully registered, synced account (online or cached)
     * - PASSIVE: Trial/unsynced account (24h limit)
     * - ADMIN: Admin account with special privileges
     */
    fun getUserMode(): UserMode {
        return when {
            role == "ADMIN" -> UserMode.ADMIN
            role == "ACTIVE" -> UserMode.ACTIVE
            role == "PASSIVE" -> {
                // Check if 24h trial has expired
                val now = System.currentTimeMillis()
                if (trialExpiresAt != null && now < trialExpiresAt) {
                    UserMode.TRIAL
                } else {
                    // Trial expired - need sync or payment
                    UserMode.GUEST
                }
            }
            else -> UserMode.GUEST
        }
    }
    
    /**
     * Check if account needs cleanup (24h expired and not synced).
     * ⚠️ Client-side hygiene only - real enforcement is server-side.
     */
    fun needsCleanup(): Boolean {
        if (role == "ACTIVE" || syncStatus == "SYNCED") return false
        val now = System.currentTimeMillis()
        val age = now - createdAt
        val twentyFourHours = 24L * 60 * 60 * 1000
        return age > twentyFourHours && syncStatus != "SYNCED"
    }
}
