package com.excell44.educam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey
    val id: String,
    val title: String,
    val subject: String, // Math, Physics, Chemistry
    val topic: String,
    val content: String, // Contenu du sujet
    val solution: String, // Solution détaillée
    val gradeLevel: String,
    val createdAt: Long = System.currentTimeMillis()
)

