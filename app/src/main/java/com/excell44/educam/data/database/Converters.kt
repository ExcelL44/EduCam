package com.excell44.educam.data.database

import androidx.room.TypeConverter
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuestionType
import com.excell44.educam.data.model.QuizMode

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun fromQuestionType(value: String?): QuestionType {
        if (value.isNullOrBlank()) return QuestionType.MULTIPLE_CHOICE
        return try {
            QuestionType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            QuestionType.MULTIPLE_CHOICE // Valeur par défaut en cas d'erreur
        }
    }

    @TypeConverter
    fun toQuestionType(value: QuestionType): String {
        return value.name
    }

    @TypeConverter
    fun fromDifficulty(value: String?): Difficulty {
        if (value.isNullOrBlank()) return Difficulty.MEDIUM
        return try {
            Difficulty.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Difficulty.MEDIUM // Valeur par défaut en cas d'erreur
        }
    }

    @TypeConverter
    fun toDifficulty(value: Difficulty): String {
        return value.name
    }

    @TypeConverter
    fun fromQuizMode(value: String?): QuizMode {
        if (value.isNullOrBlank()) return QuizMode.FAST
        return try {
            QuizMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            QuizMode.FAST // Valeur par défaut en cas d'erreur
        }
    }

    @TypeConverter
    fun toQuizMode(value: QuizMode): String {
        return value.name
    }
}

