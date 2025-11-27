package com.excell44.educam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: Long,
    val score: Int,
    val maxScore: Int,
    val completionTime: Long,
    val userAnswers: String,       // JSON sérialisé
    val aiFeedback: String?,       // Feedback personnalisé par IA
    val completedAt: Long = System.currentTimeMillis()
)
