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
    val syncStatus: String = "SYNCED" // SYNCED, PENDING_CREATE, PENDING_UPDATE
)

