package com.excell44.educam.data.quiz

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "options")
data class OptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val index: Int,
    val text: String,
    val isCorrect: Boolean = false
)
