package com.excell44.educam.data.quiz

import androidx.room.Embedded
import androidx.room.Relation

data class QuestionWithOptions(
    @Embedded val question: Question,
    @Relation(parentColumn = "id", entityColumn = "questionId")
    val options: List<OptionEntity>
)
