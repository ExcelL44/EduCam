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
    val createdAt: Long = System.currentTimeMillis()
)

