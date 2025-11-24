package com.excell44.educam.data.quiz

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_sessions")
data class QuizSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String?,
    val mode: String,
    val subject: String?,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Double,
    val startedAt: Long,
    val endedAt: Long,
    val detailsJson: String? = null
)
