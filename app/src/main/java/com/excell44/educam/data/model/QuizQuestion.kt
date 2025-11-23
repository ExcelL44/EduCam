package com.excell44.educam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_questions")
data class QuizQuestion(
    @PrimaryKey
    val id: String,
    val subject: String, // Math, Physics, Chemistry
    val topic: String,
    val question: String,
    val questionType: QuestionType,
    val options: List<String>, // Pour les QCM
    val correctAnswer: String,
    val explanation: String,
    val difficulty: Difficulty,
    val gradeLevel: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    NUMERIC,
    FORMULA
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

