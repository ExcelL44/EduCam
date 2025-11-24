package com.excell44.educam.data.quiz

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val subject: String,
    val level: String,
    val hint: String?,
    val tags: String? = null
)
