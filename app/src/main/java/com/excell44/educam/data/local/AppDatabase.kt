package com.excell44.educam.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.excell44.educam.data.local.dao.AnswerDao
import com.excell44.educam.data.local.dao.QuestionDao
import com.excell44.educam.data.local.dao.QuizDao
import com.excell44.educam.data.dao.SubjectDao
import com.excell44.educam.data.model.Subject

@Database(
    entities = [
        QuizEntity::class,
        QuestionEntity::class,
        AnswerEntity::class,
        QuizResultEntity::class,
        Subject::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao
    abstract fun resultDao(): QuizResultDao
    abstract fun subjectDao(): SubjectDao
}
