package com.excell44.educam.data.database

import androidx.room.TypeConverter
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuestionType
import com.excell44.educam.data.model.QuizMode

class Converters {
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromQuestionType(value: QuestionType): String {
        return value.name
    }

    @TypeConverter
    fun toQuestionType(value: String): QuestionType {
        return QuestionType.valueOf(value)
    }

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String {
        return value.name
    }

    @TypeConverter
    fun toDifficulty(value: String): Difficulty {
        return Difficulty.valueOf(value)
    }

    @TypeConverter
    fun fromQuizMode(value: QuizMode): String {
        return value.name
    }

    @TypeConverter
    fun toQuizMode(value: String): QuizMode {
        return QuizMode.valueOf(value)
    }
}

