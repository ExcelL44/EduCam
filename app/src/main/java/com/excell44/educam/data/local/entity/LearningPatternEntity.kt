package com.excell44.educam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité pour l'apprentissage de l'IA Smarty.
 * Stocke les patterns d'interaction utilisateur-IA.
 * Optimisé pour faible mémoire et apprentissage incrémental.
 */
@Entity(tableName = "learning_patterns")
data class LearningPatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,

    // Pattern d'entrée (question utilisateur)
    val inputPattern: String, // Question ou phrase utilisateur

    // Pattern de sortie (réponse IA)
    val outputPattern: String, // Réponse associée

    // Métadonnées d'apprentissage
    val subject: String? = null, // Math, Physics, etc.
    val difficulty: String? = null, // Facile, Moyen, Difficile
    val successRate: Float = 0.5f, // Taux de succès (0.0-1.0)

    // Statistiques d'usage
    val usageCount: Int = 1, // Nombre d'utilisations
    val lastUsed: Long = System.currentTimeMillis(),
    val firstLearned: Long = System.currentTimeMillis(),

    // Flags de synchronisation
    val isSynced: Boolean = false, // Synchronisé avec serveur
    val syncPriority: Int = 1, // Priorité de sync (1-5, 5 = haute)

    // Contexte additionnel (JSON compact)
    val contextData: String? = null // Données JSON légères (matière, niveau, etc.)
)
