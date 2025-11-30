package com.excell44.educam.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.excell44.educam.data.local.dao.AnswerDao
import com.excell44.educam.data.local.dao.BetaReferralDao
import com.excell44.educam.data.local.dao.ChatMessageDao
import com.excell44.educam.data.local.dao.LearningPatternDao
import com.excell44.educam.data.local.dao.QuestionDao
import com.excell44.educam.data.local.dao.QuizDao
import com.excell44.educam.data.local.dao.QuizResultDao
import com.excell44.educam.data.local.entity.AnswerEntity
import com.excell44.educam.data.local.entity.BetaReferralEntity
import com.excell44.educam.data.local.entity.ChatMessageEntity
import com.excell44.educam.data.local.entity.LearningPatternEntity
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
        BetaReferralEntity::class,
        ChatMessageEntity::class,
        LearningPatternEntity::class,
        Subject::class,
        QuizQuestion::class,
        QuizSession::class,
        User::class,
        ProblemSolution::class
    ],
    version = 6, // Incremented for Smarty AI chat system
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao
    abstract fun resultDao(): QuizResultDao
    abstract fun betaReferralDao(): BetaReferralDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun learningPatternDao(): LearningPatternDao
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

        /**
         * Migration from version 2 to 3: Adds localId field for multi-device conflict resolution.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add localId column with unique UUID for each existing user
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN localId TEXT NOT NULL DEFAULT ''"
                )
                // Update existing users with unique localIds
                database.execSQL(
                    """
                    UPDATE users SET localId = (
                        SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' ||
                               hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6)))
                    )
                    """
                )
            }
        }

        /**
         * Migration from version 3 to 4: Rename email to pseudo and add salt field for security.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Rename email column to pseudo
                database.execSQL("ALTER TABLE users RENAME COLUMN email TO pseudo")
                // Add salt column for password hashing
                database.execSQL("ALTER TABLE users ADD COLUMN salt TEXT NOT NULL DEFAULT ''")
            }
        }

        /**
         * Migration from version 4 to 5: Add beta_referral table for referral system.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create beta_referral table
                database.execSQL("""
                    CREATE TABLE beta_referral (
                        userId TEXT PRIMARY KEY NOT NULL,
                        referralToken TEXT NOT NULL,
                        currentCount INTEGER NOT NULL DEFAULT 0,
                        quota INTEGER NOT NULL DEFAULT 5,
                        level INTEGER NOT NULL DEFAULT 1,
                        whatsappRequestSent INTEGER NOT NULL DEFAULT 0,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        lastUpdated INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        /**
         * Migration from version 5 to 6: Add Smarty AI chat system tables.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create chat_messages table for conversation history
                database.execSQL("""
                    CREATE TABLE chat_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        message TEXT NOT NULL,
                        isFromUser INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        messageType TEXT NOT NULL DEFAULT 'TEXT',
                        confidence REAL NOT NULL DEFAULT 1.0,
                        contextTags TEXT,
                        isLearned INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // Create learning_patterns table for AI learning
                database.execSQL("""
                    CREATE TABLE learning_patterns (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        inputPattern TEXT NOT NULL,
                        outputPattern TEXT NOT NULL,
                        subject TEXT,
                        difficulty TEXT,
                        successRate REAL NOT NULL DEFAULT 0.5,
                        usageCount INTEGER NOT NULL DEFAULT 1,
                        lastUsed INTEGER NOT NULL,
                        firstLearned INTEGER NOT NULL,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        syncPriority INTEGER NOT NULL DEFAULT 1,
                        contextData TEXT
                    )
                """)

                // Create indexes for performance
                database.execSQL("CREATE INDEX index_chat_messages_userId_timestamp ON chat_messages(userId, timestamp)")
                database.execSQL("CREATE INDEX index_learning_patterns_userId ON learning_patterns(userId)")
                database.execSQL("CREATE INDEX index_learning_patterns_usage ON learning_patterns(usageCount DESC, successRate DESC)")
            }
        }
    }
}
