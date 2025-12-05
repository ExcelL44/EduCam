package com.excell44.educam.di

import android.content.Context
import androidx.room.Room
import com.excell44.educam.data.local.AppDatabase
import com.excell44.educam.data.local.dao.AnswerDao
import com.excell44.educam.data.local.dao.BetaReferralDao
import com.excell44.educam.data.local.dao.ChatMessageDao
import com.excell44.educam.data.local.dao.LearningPatternDao
import com.excell44.educam.data.local.dao.QuestionDao
import com.excell44.educam.data.local.dao.QuizDao
import com.excell44.educam.data.local.dao.QuizResultDao
import com.excell44.educam.data.dao.SubjectDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "quiz_database"
        )
        .addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6
        )
        .fallbackToDestructiveMigration()
        .addCallback(com.excell44.educam.di.DatabaseCallback(context))
        .build()
    
    @Provides
    fun provideQuizDao(db: AppDatabase): QuizDao = db.quizDao()
    
    @Provides
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()

    @Provides
    fun provideAnswerDao(db: AppDatabase): AnswerDao = db.answerDao()

    @Provides
    fun provideResultDao(db: AppDatabase): QuizResultDao = db.resultDao()

    @Provides
    fun provideSubjectDao(db: AppDatabase): SubjectDao = db.subjectDao()

    @Provides
    fun provideQuizQuestionDao(db: AppDatabase): com.excell44.educam.data.dao.QuizQuestionDao = db.quizQuestionDao()

    @Provides
    fun provideQuizSessionDao(db: AppDatabase): com.excell44.educam.data.dao.QuizSessionDao = db.quizSessionDao()

    @Provides
    fun provideBetaReferralDao(db: AppDatabase): BetaReferralDao = db.betaReferralDao()

    @Provides
    fun provideChatMessageDao(db: AppDatabase): ChatMessageDao = db.chatMessageDao()

    @Provides
    fun provideLearningPatternDao(db: AppDatabase): LearningPatternDao = db.learningPatternDao()

    @Provides
    fun provideUserDao(db: AppDatabase): com.excell44.educam.data.dao.UserDao = db.userDao()

    @Provides
    fun provideProblemSolutionDao(db: AppDatabase): com.excell44.educam.data.dao.ProblemSolutionDao = db.problemSolutionDao()
}
