package com.excell44.educam.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class QuizWithQuestions(
    @Embedded val quiz: QuizEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "quizId",
        entity = QuestionEntity::class
    )
    val questions: List<QuestionWithAnswers>
)

data class QuestionWithAnswers(
    @Embedded val question: QuestionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId",
        entity = AnswerEntity::class
    )
    val answers: List<AnswerEntity>
)
