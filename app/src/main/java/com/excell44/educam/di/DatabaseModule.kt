package com.excell44.educam.di

import android.content.Context
import androidx.room.Room
import com.excell44.educam.data.dao.ProblemSolutionDao
import com.excell44.educam.data.dao.QuizQuestionDao
import com.excell44.educam.data.dao.QuizSessionDao
import com.excell44.educam.data.dao.SubjectDao
import com.excell44.educam.data.dao.UserDao
import com.excell44.educam.data.database.EduCamDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EduCamDatabase {
        return Room.databaseBuilder(
            context,
            EduCamDatabase::class.java,
            EduCamDatabase.DATABASE_NAME
        )
            // Pour le développement, on utilise fallbackToDestructiveMigration
            // En production, ajouter des migrations spécifiques :
            // .addMigrations(MIGRATION_1_2, MIGRATION_2_3, ...)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: EduCamDatabase): UserDao = database.userDao()

    @Provides
    fun provideQuizQuestionDao(database: EduCamDatabase): QuizQuestionDao = database.quizQuestionDao()

    @Provides
    fun provideQuizSessionDao(database: EduCamDatabase): QuizSessionDao = database.quizSessionDao()

    @Provides
    fun provideSubjectDao(database: EduCamDatabase): SubjectDao = database.subjectDao()

    @Provides
    fun provideProblemSolutionDao(database: EduCamDatabase): ProblemSolutionDao = database.problemSolutionDao()
}

