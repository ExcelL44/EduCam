package com.excell44.educam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room pour les messages du chat Smarty IA.
 * Optimisée pour faible consommation mémoire.
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val message: String,
    val isFromUser: Boolean, // true = utilisateur, false = IA
    val timestamp: Long,
    val messageType: MessageType = MessageType.TEXT,
    val confidence: Float = 1.0f, // Confiance de la réponse IA (0.0-1.0)
    val contextTags: String? = null, // Tags JSON pour contexte (matière, difficulté, etc.)
    val isLearned: Boolean = false // Si ce message vient de l'apprentissage
)

/**
 * Types de messages supportés.
 */
enum class MessageType {
    TEXT,
    MATH_EXPRESSION, // Pour formules mathématiques
    QUIZ_QUESTION,   // Question de quiz
    EXPLANATION      // Explication détaillée
}
