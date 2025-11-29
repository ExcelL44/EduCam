package com.excell44.educam.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrations pour la base de données AppDatabase
 * 
 * Pour ajouter une nouvelle migration :
 * 1. Incrémenter la version dans AppDatabase
 * 2. Créer une nouvelle migration ici
 * 3. L'ajouter dans DatabaseModule avec .addMigrations()
 * 
 * Exemple :
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         database.execSQL("ALTER TABLE users ADD COLUMN new_column TEXT")
 *     }
 * }
 */

// Exemple de migration (actuellement non utilisée car version = 1)
// val MIGRATION_1_2 = object : Migration(1, 2) {
//     override fun migrate(database: SupportSQLiteDatabase) {
//         // Ajouter une nouvelle colonne
//         database.execSQL("ALTER TABLE users ADD COLUMN new_field TEXT DEFAULT ''")
//     }
// }

// Exemple de migration pour version 2 à 3
// val MIGRATION_2_3 = object : Migration(2, 3) {
//     override fun migrate(database: SupportSQLiteDatabase) {
//         // Créer une nouvelle table
//         database.execSQL("""
//             CREATE TABLE IF NOT EXISTS new_table (
//                 id TEXT PRIMARY KEY NOT NULL,
//                 name TEXT NOT NULL
//             )
//         """.trimIndent())
//     }
// }

