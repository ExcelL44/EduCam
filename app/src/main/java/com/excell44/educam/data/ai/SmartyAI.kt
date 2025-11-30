package com.excell44.educam.data.ai

import com.excell44.educam.data.local.dao.ChatMessageDao
import com.excell44.educam.data.local.dao.LearningPatternDao
import com.excell44.educam.data.local.entity.ChatMessageEntity
import com.excell44.educam.data.local.entity.LearningPatternEntity
import com.excell44.educam.data.local.entity.MessageType
import com.excell44.educam.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * IA locale Smarty - Apprentissage adaptatif léger.
 * Optimisée pour faible consommation mémoire et performance offline.
 */
@Singleton
class SmartyAI @Inject constructor(
    private val chatDao: ChatMessageDao,
    private val patternDao: LearningPatternDao
) {

    private val aiScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val learningMutex = Mutex()

    // Cache en mémoire pour les patterns fréquents (limité à 100 entrées)
    private val patternCache = LinkedHashMap<String, LearningPatternEntity>(100 + 1, 0.75f, true)
    private val cacheMutex = Mutex()

    companion object {
        private const val TAG = "SmartyAI"
        private const val MAX_CACHE_SIZE = 100
        private const val MIN_CONFIDENCE_THRESHOLD = 0.3f
        private const val LEARNING_DECAY_FACTOR = 0.95f
    }

    init {
        // Nettoyer le cache périodiquement
        aiScope.launch {
            while (true) {
                kotlinx.coroutines.delay(300000) // 5 minutes
                cleanupCache()
            }
        }
    }

    /**
     * Génère une réponse intelligente basée sur l'apprentissage.
     */
    suspend fun generateResponse(
        userId: String,
        userMessage: String,
        subject: String? = null
    ): AIResponse = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Generating response for user $userId: $userMessage")

            // 1. Nettoyer et analyser le message utilisateur
            val cleanMessage = preprocessMessage(userMessage)
            val detectedSubject = detectSubject(cleanMessage) ?: subject

            // 2. Chercher dans les patterns appris
            val learnedResponse = findLearnedResponse(userId, cleanMessage, detectedSubject)

            // 3. Si pattern trouvé avec bonne confiance, l'utiliser
            if (learnedResponse != null && learnedResponse.confidence > MIN_CONFIDENCE_THRESHOLD) {
                Logger.d(TAG, "Using learned response with confidence ${learnedResponse.confidence}")
                updatePatternUsage(learnedResponse.patternId)

                return@withContext AIResponse(
                    message = learnedResponse.response,
                    confidence = learnedResponse.confidence,
                    subject = detectedSubject,
                    isLearned = true,
                    messageType = MessageType.TEXT
                )
            }

            // 4. Générer une réponse par défaut basée sur des règles simples
            val defaultResponse = generateDefaultResponse(cleanMessage, detectedSubject)

            // 5. Apprendre de cette interaction pour plus tard
            learnFromInteraction(userId, cleanMessage, defaultResponse, detectedSubject)

            Logger.d(TAG, "Generated default response")
            AIResponse(
                message = defaultResponse,
                confidence = 0.5f,
                subject = detectedSubject,
                isLearned = false,
                messageType = MessageType.TEXT
            )

        } catch (e: Exception) {
            Logger.e(TAG, "Error generating response", e)
            AIResponse(
                message = "Désolé, je n'ai pas compris. Peux-tu reformuler ta question ?",
                confidence = 0.0f,
                subject = null,
                isLearned = false,
                messageType = MessageType.TEXT
            )
        }
    }

    /**
     * Apprentissage incrémental à partir des interactions.
     */
    suspend fun learnFromInteraction(
        userId: String,
        userMessage: String,
        aiResponse: String,
        subject: String? = null,
        userFeedback: Float = 0.5f // 0.0 = mauvais, 1.0 = excellent
    ) {
        learningMutex.withLock {
            aiScope.launch {
                try {
                    val cleanInput = preprocessMessage(userMessage)

                    // Vérifier si ce pattern existe déjà
                    val existingPattern = patternDao.searchPatterns(userId, cleanInput).firstOrNull()

                    if (existingPattern != null) {
                        // Mettre à jour le pattern existant
                        val newSuccessRate = min(1.0f, max(0.0f,
                            (existingPattern.successRate * existingPattern.usageCount + userFeedback) /
                            (existingPattern.usageCount + 1)
                        ))

                        patternDao.updateSuccessRate(existingPattern.id, newSuccessRate)
                        patternDao.incrementUsage(existingPattern.id, System.currentTimeMillis())

                        Logger.d(TAG, "Updated existing pattern: ${existingPattern.id}")
                    } else {
                        // Créer un nouveau pattern
                        val newPattern = LearningPatternEntity(
                            userId = userId,
                            inputPattern = cleanInput,
                            outputPattern = aiResponse,
                            subject = subject,
                            difficulty = detectDifficulty(cleanInput),
                            successRate = userFeedback,
                            usageCount = 1,
                            lastUsed = System.currentTimeMillis(),
                            firstLearned = System.currentTimeMillis(),
                            isSynced = false,
                            syncPriority = 1
                        )

                        patternDao.insertPattern(newPattern)
                        Logger.d(TAG, "Learned new pattern for user $userId")
                    }

                    // Nettoyer les vieux patterns si nécessaire
                    cleanupOldPatterns(userId)

                } catch (e: Exception) {
                    Logger.e(TAG, "Error learning from interaction", e)
                }
            }
        }
    }

    /**
     * Sauvegarde un message de chat.
     */
    suspend fun saveChatMessage(
        userId: String,
        message: String,
        isFromUser: Boolean,
        confidence: Float = 1.0f,
        messageType: MessageType = MessageType.TEXT,
        contextTags: String? = null,
        isLearned: Boolean = false
    ) {
        try {
            val chatMessage = ChatMessageEntity(
                userId = userId,
                message = message,
                isFromUser = isFromUser,
                timestamp = System.currentTimeMillis(),
                messageType = messageType,
                confidence = confidence,
                contextTags = contextTags,
                isLearned = isLearned
            )

            chatDao.insertMessage(chatMessage)

            // Nettoyer les anciens messages si nécessaire
            val messageCount = chatDao.getMessageCount(userId)
            if (messageCount > 100) {
                chatDao.cleanupOldMessages(userId)
                Logger.d(TAG, "Cleaned up old messages for user $userId")
            }

        } catch (e: Exception) {
            Logger.e(TAG, "Error saving chat message", e)
        }
    }

    // === MÉTHODES PRIVÉES ===

    private suspend fun findLearnedResponse(
        userId: String,
        userMessage: String,
        subject: String?
    ): LearnedResponse? {
        // Chercher d'abord dans le cache
        cacheMutex.withLock {
            patternCache[userMessage]?.let { cached ->
                return LearnedResponse(cached.outputPattern, cached.successRate, cached.id)
            }
        }

        // Chercher dans la base de données
        val patterns = if (subject != null) {
            patternDao.getPatternsBySubject(userId, subject)
        } else {
            patternDao.getBestPatterns(userId, 10)
        }

        // Trouver la meilleure correspondance
        val bestMatch = patterns.maxByOrNull { pattern ->
            calculateSimilarity(userMessage, pattern.inputPattern) * pattern.successRate
        }

        return bestMatch?.let { pattern ->
            val similarity = calculateSimilarity(userMessage, pattern.inputPattern)
            val confidence = similarity * pattern.successRate

            if (confidence > MIN_CONFIDENCE_THRESHOLD) {
                // Ajouter au cache
                cacheMutex.withLock {
                    if (patternCache.size >= MAX_CACHE_SIZE) {
                        patternCache.remove(patternCache.keys.first())
                    }
                    patternCache[userMessage] = pattern
                }

                LearnedResponse(pattern.outputPattern, confidence, pattern.id)
            } else null
        }
    }

    private fun generateDefaultResponse(message: String, subject: String?): String {
        val lowerMessage = message.lowercase()

        return when {
            // Salutations
            lowerMessage.contains("bonjour") || lowerMessage.contains("salut") ->
                "Bonjour ! Je suis Smarty, ton assistant IA pour les études. Comment puis-je t'aider aujourd'hui ?"

            lowerMessage.contains("merci") ->
                "De rien ! N'hésite pas si tu as d'autres questions."

            // Aide générale
            lowerMessage.contains("aide") || lowerMessage.contains("help") ->
                "Je peux t'aider avec des explications en maths, physique, chimie, et bien d'autres matières. Pose-moi une question !"

            // Questions sur les matières
            subject == "Math" -> generateMathResponse(lowerMessage)
            subject == "Physics" -> generatePhysicsResponse(lowerMessage)
            subject == "Chemistry" -> generateChemistryResponse(lowerMessage)

            // Questions générales
            lowerMessage.contains("quoi") || lowerMessage.contains("what") ->
                "Je suis une IA conçue pour t'aider dans tes études. Je peux expliquer des concepts, résoudre des exercices, et apprendre de nos conversations."

            lowerMessage.contains("comment") || lowerMessage.contains("how") ->
                "Pour m'utiliser, pose-moi simplement une question sur tes études ! Plus on discute, plus je m'améliore pour te répondre."

            // Réponse par défaut
            else -> "Intéressant ! Peux-tu me donner plus de détails sur ta question en ${subject ?: "cette matière"} ?"
        }
    }

    private fun generateMathResponse(message: String): String {
        return when {
            message.contains("équation") -> "Pour résoudre une équation, identifie d'abord le type (linéaire, quadratique...) et applique la méthode appropriée."
            message.contains("dérivée") -> "La dérivée mesure le taux de variation d'une fonction. Par exemple, d/dx[x²] = 2x."
            message.contains("intégrale") -> "L'intégrale est l'opération inverse de la dérivée. Elle calcule l'aire sous une courbe."
            else -> "En maths, il est souvent utile de commencer par identifier le type de problème et les données disponibles."
        }
    }

    private fun generatePhysicsResponse(message: String): String {
        return when {
            message.contains("force") -> "La force est un vecteur qui peut modifier le mouvement d'un objet selon F = m×a."
            message.contains("énergie") -> "L'énergie se conserve. Les formes principales sont cinétique (1/2mv²) et potentielle (mgh)."
            message.contains("mouvement") -> "Le mouvement uniforme a vitesse constante, l'accéléré a vitesse variable."
            else -> "En physique, commence toujours par identifier les grandeurs connues et inconnues."
        }
    }

    private fun generateChemistryResponse(message: String): String {
        return when {
            message.contains("réaction") -> "Une réaction chimique transforme les réactifs en produits selon la loi de conservation de la masse."
            message.contains("acide") -> "Les acides libèrent des ions H⁺ en solution aqueuse (pH < 7)."
            message.contains("molécule") -> "Les molécules sont formées d'atomes liés par des liaisons covalentes."
            else -> "En chimie, équilibre-toi toujours sur la stœchiométrie et les principes fondamentaux."
        }
    }

    private fun preprocessMessage(message: String): String {
        return message
            .lowercase()
            .replace(Regex("[^a-zA-Z0-9àâäéèêëïîôöùûüÿç\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun detectSubject(message: String): String? {
        val lowerMessage = message.lowercase()
        return when {
            lowerMessage.contains("équation") || lowerMessage.contains("calcul") ||
            lowerMessage.contains("math") || lowerMessage.contains("algèbre") -> "Math"

            lowerMessage.contains("force") || lowerMessage.contains("énergie") ||
            lowerMessage.contains("physique") || lowerMessage.contains("mouvement") -> "Physics"

            lowerMessage.contains("réaction") || lowerMessage.contains("acide") ||
            lowerMessage.contains("chimie") || lowerMessage.contains("molécule") -> "Chemistry"

            else -> null
        }
    }

    private fun detectDifficulty(message: String): String? {
        val keywords = listOf("difficile", "compliqué", "complexe", "avancé", "expert")
        val easyKeywords = listOf("simple", "basique", "facile", "débutant")

        return when {
            keywords.any { message.contains(it) } -> "Difficile"
            easyKeywords.any { message.contains(it) } -> "Facile"
            else -> "Moyen"
        }
    }

    private fun calculateSimilarity(text1: String, text2: String): Float {
        // Similarité simple basée sur les mots communs
        val words1 = text1.split(" ").toSet()
        val words2 = text2.split(" ").toSet()

        val intersection = words1.intersect(words2).size.toFloat()
        val union = words1.union(words2).size.toFloat()

        return if (union > 0) intersection / union else 0f
    }

    private suspend fun updatePatternUsage(patternId: Long, timestamp: Long = System.currentTimeMillis()) {
        patternDao.incrementUsage(patternId, timestamp)
    }

    private suspend fun cleanupOldPatterns(userId: String) {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        patternDao.deleteOldUnusedPatterns(userId, thirtyDaysAgo)

        val patternCount = patternDao.getPatternCount(userId)
        if (patternCount > 500) {
            patternDao.cleanupExcessPatterns(userId)
        }
    }

    private suspend fun cleanupCache() {
        cacheMutex.withLock {
            // Garder seulement les 50% plus récents
            val entriesToKeep = patternCache.entries.take(MAX_CACHE_SIZE / 2)
            patternCache.clear()
            entriesToKeep.forEach { patternCache[it.key] = it.value }
        }
    }

    // === CLASSES DE DONNÉES ===

    data class AIResponse(
        val message: String,
        val confidence: Float,
        val subject: String?,
        val isLearned: Boolean,
        val messageType: MessageType
    )

    private data class LearnedResponse(
        val response: String,
        val confidence: Float,
        val patternId: Long
    )
}
