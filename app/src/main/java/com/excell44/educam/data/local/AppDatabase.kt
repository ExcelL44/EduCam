package com.excell44.educam.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.excell44.educam.data.local.dao.AnswerDao
import com.excell44.educam.data.local.dao.QuestionDao
import com.excell44.educam.data.local.dao.QuizDao
import com.excell44.educam.data.local.dao.QuizResultDao
import com.excell44.educam.data.local.entity.AnswerEntity
import com.excell44.educam.data.local.entity.QuestionEntity
import com.excell44.educam.data.local.entity.QuizEntity
import com.excell44.educam.data.local.entity.QuizResultEntity
import com.excell44.educam.data.dao.SubjectDao
import com.excell44.educam.data.dao.QuizQuestionDao
import com.excell44.educam.data.dao.QuizSessionDao
import com.excell44.educam.data.dao.UserDao
import com.excell44.educam.data.dao.ProblemSolutionDao
import com.excell44.educam.data.model.Subject
import com.excell44.educam.data.model.QuizQuestion
import com.excell44.educam.data.model.QuizSession
import com.excell44.educam.data.model.User
import com.excell44.educam.data.model.ProblemSolution

@Database(
    entities = [
        QuizEntity::class,
        QuestionEntity::class,
        AnswerEntity::class,
        QuizResultEntity::class,
        Subject::class,
        QuizQuestion::class,
        QuizSession::class,
        User::class,
        ProblemSolution::class
    ],
    version = 2, // Incremented for new field
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao
    abstract fun resultDao(): QuizResultDao
    abstract fun subjectDao(): SubjectDao
    abstract fun quizQuestionDao(): QuizQuestionDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun userDao(): UserDao
    abstract fun problemSolutionDao(): ProblemSolutionDao
    
    companion object {
        /**
         * Migration from version 1 to 2: Adds lastSyncTimestamp field to users table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add lastSyncTimestamp column with default value 0
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN lastSyncTimestamp INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
