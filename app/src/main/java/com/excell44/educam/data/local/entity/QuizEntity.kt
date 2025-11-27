package com.excell44.educam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val difficulty: String,        // EASY, MEDIUM, HARD
    val category: String,
    val timeLimitSeconds: Int?,
    val isAIEnhanced: Boolean = false,  // Indique si IA active
    val createdAt: Long = System.currentTimeMillis()
)
