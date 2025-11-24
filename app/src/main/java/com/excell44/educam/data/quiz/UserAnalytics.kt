package com.excell44.educam.data.quiz

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_analytics")
data class UserAnalytics(
    @PrimaryKey val userId: String,
    val totalQuizzes: Int = 0,
    val weaknessesJson: String? = null,
    val strengthsJson: String? = null,
    val lastUpdated: Long = 0L
)
