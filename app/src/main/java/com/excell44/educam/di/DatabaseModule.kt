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
import com.excell44.educam.data.local.dao.SubjectDao
import com.excell44.educam.data.model.QuizQuestion
import com.excell44.educam.data.model.Difficulty
import com.excell44.educam.data.model.QuestionType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            AppDatabase.MIGRATION_4_5
        )
        .addCallback(DatabaseCallback(context))
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

    /**
     * Callback pour initialiser la base de données avec des questions d'exemple
     */
    private class DatabaseCallback(private val context: Context) : androidx.room.RoomDatabase.Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateSampleQuestions()
            }
        }

        private suspend fun populateSampleQuestions() {
            try {
                val database = provideDatabase(context)
                val quizQuestionDao = database.quizQuestionDao()

                // Vérifier si des questions existent déjà
                val existingQuestions = quizQuestionDao.getRandomQuestions("Mathématiques", "Terminale", 1)
                if (existingQuestions.isNotEmpty()) {
                    return // Les questions sont déjà insérées
                }

                // Questions de Mathématiques
                val mathQuestions = listOf(
                    QuizQuestion(
                        id = "math_001",
                        subject = "Mathématiques",
                        topic = "Équations du premier degré",
                        question = "Résoudre l'équation: 2x + 5 = 17",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("x = 16", "x = 6", "x = 11", "x = 2"),
                        correctAnswer = "x = 6",
                        explanation = "Soustraire 5 des deux membres: 2x = 12, puis diviser par 2: x = 6",
                        difficulty = Difficulty.EASY,
                        gradeLevel = "Terminale"
                    ),
                    QuizQuestion(
                        id = "math_002",
                        subject = "Mathématiques",
                        topic = "Théorème de Pythagore",
                        question = "Dans un triangle rectangle, si les cathètes mesurent 3 cm et 4 cm, l'hypoténuse mesure:",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("5 cm", "7 cm", "1 cm", "12 cm"),
                        correctAnswer = "5 cm",
                        explanation = "a² + b² = c² donc 3² + 4² = c² => 9 + 16 = c² => c² = 25 => c = 5",
                        difficulty = Difficulty.EASY,
                        gradeLevel = "Terminale"
                    ),
                    QuizQuestion(
                        id = "math_003",
                        subject = "Mathématiques",
                        topic = "Dérivation",
                        question = "La dérivée de f(x) = x² + 3x + 2 est:",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("f'(x) = 2x + 3", "f'(x) = x² + 3", "f'(x) = 2x + 2", "f'(x) = x² + 3x"),
                        correctAnswer = "f'(x) = 2x + 3",
                        explanation = "Dérivée de x² = 2x, dérivée de 3x = 3, dérivée de 2 = 0",
                        difficulty = Difficulty.MEDIUM,
                        gradeLevel = "Terminale"
                    )
                )

                // Questions de Physique
                val physicsQuestions = listOf(
                    QuizQuestion(
                        id = "phys_001",
                        subject = "Physique",
                        topic = "Électricité",
                        question = "Quelle est l'unité d'intensité électrique?",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("Volt", "Ampère", "Watt", "Ohm"),
                        correctAnswer = "Ampère",
                        explanation = "L'unité d'intensité électrique est l'Ampère (A)",
                        difficulty = Difficulty.EASY,
                        gradeLevel = "Terminale"
                    ),
                    QuizQuestion(
                        id = "phys_002",
                        subject = "Physique",
                        topic = "Électromagnétisme",
                        question = "Selon la loi de Coulomb, la force électrique entre deux charges est proportionnelle à:",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("Le produit des charges divisé par le carré de la distance", "La somme des charges divisée par la distance", "Le carré des charges multiplié par la distance", "La différence des charges multipliée par la distance"),
                        correctAnswer = "Le produit des charges divisé par le carré de la distance",
                        explanation = "F = k × q₁ × q₂ / r²",
                        difficulty = Difficulty.MEDIUM,
                        gradeLevel = "Terminale"
                    )
                )

                // Questions de Chimie
                val chemistryQuestions = listOf(
                    QuizQuestion(
                        id = "chim_001",
                        subject = "Chimie",
                        topic = "Structure atomique",
                        question = "Combien y a-t-il d'électrons dans un atome de carbone neutre?",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("4", "6", "8", "12"),
                        correctAnswer = "6",
                        explanation = "Le numéro atomique du carbone est 6, donc il a 6 électrons",
                        difficulty = Difficulty.EASY,
                        gradeLevel = "Terminale"
                    ),
                    QuizQuestion(
                        id = "chim_002",
                        subject = "Chimie",
                        topic = "Configuration électronique",
                        question = "Quelle est la configuration électronique de l'oxygène?",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("1s² 2s² 2p⁴", "1s² 2s² 2p⁶", "1s² 2s² 2p²", "1s² 2s⁴ 2p²"),
                        correctAnswer = "1s² 2s² 2p⁴",
                        explanation = "Numéro atomique 8: 1s² 2s² 2p⁴",
                        difficulty = Difficulty.MEDIUM,
                        gradeLevel = "Terminale"
                    )
                )

                // Insérer toutes les questions
                val allQuestions = mathQuestions + physicsQuestions + chemistryQuestions
                quizQuestionDao.insertQuestions(allQuestions)

                android.util.Log.i("DatabaseCallback", "Inserted ${allQuestions.size} sample questions")
            } catch (e: Exception) {
                android.util.Log.e("DatabaseCallback", "Error populating sample questions", e)
            }
        }
    }
}
