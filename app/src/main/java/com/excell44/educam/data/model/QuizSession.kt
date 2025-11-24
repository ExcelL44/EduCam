package com.excell44.educam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_sessions")
data class QuizSession(
    @PrimaryKey
    val id: String,
    val userId: String,
    val mode: QuizMode,
    val subject: String? = null,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isCompleted: Boolean = false,
    // JSON details per question or paused progress (answers/indices)
    val detailsJson: String? = null
)

enum class QuizMode {
    FAST, // Mode rapide
    SLOW  // Mode lent
}

