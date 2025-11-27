package com.excell44.educam.di

import android.content.Context
import androidx.room.Room
import com.excell44.educam.data.local.AppDatabase
import com.excell44.educam.data.repository.QuizRepository
import com.excell44.educam.data.repository.QuizRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * Test module pour remplacer DatabaseModule en tests.
 * Utilise une base de données en mémoire pour isolation.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class, RepositoryModule::class]
)
object TestAppModule {
    
    @Provides
    @Singleton
    fun provideTestDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
        .allowMainThreadQueries() // Pour tests uniquement
        .build()
    }
    
    @Provides
    @Singleton
    fun provideQuizDao(db: AppDatabase) = db.quizDao()
    
    @Provides
    @Singleton
    fun provideQuestionDao(db: AppDatabase) = db.questionDao()
    
    @Provides
    @Singleton
    fun provideAnswerDao(db: AppDatabase) = db.answerDao()
    
    @Provides
    @Singleton
    fun provideResultDao(db: AppDatabase) = db.resultDao()
    
    @Provides
    @Singleton
    fun provideSubjectDao(db: AppDatabase) = db.subjectDao()
    
    @Provides
    @Singleton
    fun provideQuizQuestionDao(db: AppDatabase) = db.quizQuestionDao()
    
    @Provides
    @Singleton
    fun provideQuizSessionDao(db: AppDatabase) = db.quizSessionDao()
    
    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase) = db.userDao()
    
    @Provides
    @Singleton
    fun provideProblemSolutionDao(db: AppDatabase) = db.problemSolutionDao()
    
    @Provides
    @Singleton
    fun provideTestQuizRepository(
        db: AppDatabase,
        @ApplicationContext context: Context
    ): QuizRepository {
        // Utilise le repository réel mais avec DB en mémoire
        return QuizRepositoryImpl(
            database = db,
            context = context
        )
    }
}
