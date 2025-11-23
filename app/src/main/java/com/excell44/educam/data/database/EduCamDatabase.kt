package com.excell44.educam.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.excell44.educam.data.dao.QuizQuestionDao
import com.excell44.educam.data.dao.QuizSessionDao
import com.excell44.educam.data.dao.SubjectDao
import com.excell44.educam.data.dao.UserDao
import com.excell44.educam.data.dao.ProblemSolutionDao
import com.excell44.educam.data.model.ProblemSolution
import com.excell44.educam.data.model.QuizQuestion
import com.excell44.educam.data.model.QuizSession
import com.excell44.educam.data.model.Subject
import com.excell44.educam.data.model.User

@Database(
    entities = [
        User::class,
        QuizQuestion::class,
        QuizSession::class,
        Subject::class,
        ProblemSolution::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EduCamDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun quizQuestionDao(): QuizQuestionDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun subjectDao(): SubjectDao
    abstract fun problemSolutionDao(): ProblemSolutionDao
}

