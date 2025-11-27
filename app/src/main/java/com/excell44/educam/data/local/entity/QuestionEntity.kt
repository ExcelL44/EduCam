package com.excell44.educam.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quizId"])]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: Long,              // Clé étrangère
    val questionText: String,
    val questionType: String,      // MULTIPLE_CHOICE, TRUE_FALSE, OPEN
    val points: Int = 1,
    val explanation: String?,
    val aiGeneratedHint: String?   // Hint généré par IA
)
