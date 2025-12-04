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
    private val scienceBrain = ScienceBrain() // Le cerveau scientifique

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
            // 1. Nettoyer et analyser le message utilisateur
            val cleanMessage = preprocessMessage(userMessage)
            // Utiliser le sujet détecté, ou celui du contexte précédent
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

            // 3.5. Tenter une résolution scientifique (Maths/Physique/Chimie)
            // Smarty analyse si c'est un problème soluble
            val scientificResult = scienceBrain.trySolve(cleanMessage, detectedSubject)
            if (scientificResult != null) {
                Logger.d(TAG, "Solved scientific problem")
                return@withContext AIResponse(
                    message = scientificResult,
                    confidence = 1.0f,
                    subject = detectedSubject,
                    isLearned = false,
                    messageType = MessageType.EXPLANATION
                )
            }

            // 4. Générer une réponse par défaut variée
            val defaultResponse = generateDefaultResponse(cleanMessage, detectedSubject)

            // 5. Apprentissage automatique minimal : Apprendre des réponses de haute qualité
            // Si la confiance est élevée (>0.7), on apprend automatiquement
            // Cela améliore progressivement Smarty sans feedback utilisateur
            if (defaultResponse.isNotEmpty()) {
                // Apprendre automatiquement les réponses par défaut pertinentes
                aiScope.launch {
                    learnFromInteraction(
                        userId = userId,
                        userMessage = cleanMessage,
                        aiResponse = defaultResponse,
                        subject = detectedSubject,
                        userFeedback = 0.6f // Feedback moyen pour les réponses par défaut
                    )
                }
            }

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
        
        // Extension locale pour choisir aléatoirement
        fun <T> List<T>.randomChoice(): T = this[kotlin.random.Random.nextInt(size)]

        return when {
            // Salutations
            lowerMessage.contains("bonjour") || lowerMessage.contains("salut") || lowerMessage.contains("hello") || 
            lowerMessage.contains("hey") || lowerMessage.contains("coucou") ->
                listOf(
                    "Bonjour ! Je suis Smarty, ton assistant scientifique. Sur quel sujet travailles-tu aujourd'hui ?",
                    "Salut ! Prêt à résoudre des problèmes ? Je t'écoute.",
                    "Hello ! Je suis là pour t'aider en Math, Physique ou Chimie. Une question en tête ?",
                    "Coucou ! On révise quoi aujourd'hui ? N'hésite pas, je suis là pour ça !",
                    "Hé ! Smarty est prêt. Balance ton problème, on va le résoudre ensemble."
                ).randomChoice()

            lowerMessage.contains("merci") || lowerMessage.contains("thanks") ->
                listOf(
                    "Avec plaisir ! N'hésite pas si tu as d'autres questions.",
                    "De rien ! C'est mon travail d'aider.",
                    "Je t'en prie. On continue ?",
                    "Ravi d'avoir pu t'aider ! 😊",
                    "Toujours là pour t'accompagner !"
                ).randomChoice()

            // Demandes d'aide générale
            lowerMessage.contains("aide") || lowerMessage.contains("help") || lowerMessage.contains("besoin") ->
                listOf(
                    "Je peux t'aider en maths, physique, chimie... Pose-moi simplement ta question avec les données du problème.",
                    "Coincé sur un exercice ? Dis-moi tout, je vais essayer de t'expliquer étape par étape.",
                    "Je suis là pour ça. Tu peux me demander des définitions, des formules ou de résoudre un problème.",
                    "Pour t'aider au mieux, donne-moi les données du problème. Par exemple : 'm=10kg, v=5m/s'."
                ).randomChoice()

            // Questions sur les formules
            lowerMessage.contains("formule") || lowerMessage.contains("formula") ->
                when (subject) {
                    "Math" -> "Les formules clés en maths : aire du cercle (πr²), théorème de Pythagore (a²+b²=c²), dérivée (x^n → nx^(n-1)). Laquelle t'intéresse ?"
                    "Physics" -> "Formules essentielles en physique : F=ma (Newton), Ec=½mv² (énergie cinétique), U=RI (Ohm), P=UI (puissance). Tu cherches laquelle ?"
                    "Chemistry" -> "Formules chimie : C=n/V (concentration), n=m/M (quantité de matière), pH=-log[H+]. De quoi as-tu besoin ?"
                    else -> "Je connais plein de formules ! Précise la matière (maths, physique, chimie) et je t'aide."
                }

            // Questions méthodologiques
            lowerMessage.contains("comment") && (lowerMessage.contains("résoudre") || lowerMessage.contains("faire")) ->
                listOf(
                    "Pour résoudre un problème : 1) Identifie les données, 2) Trouve la formule adaptée, 3) Remplace et calcule. Montre-moi ton exercice !",
                    "La méthode : Lis bien l'énoncé, note ce que tu cherches, applique la formule. Besoin d'un exemple ?",
                    "Commence par lister ce que tu as (données) et ce que tu cherches (inconnue). Ensuite, on trouve la formule ensemble."
                ).randomChoice()

            // Encouragements
            lowerMessage.contains("difficile") || lowerMessage.contains("compliqué") || lowerMessage.contains("comprends pas") ->
                listOf(
                    "C'est normal de trouver ça difficile au début. On va y aller pas à pas. Montre-moi où tu bloques.",
                    "Ne t'inquiète pas, on va décomposer ensemble. Quel est le premier point qui te pose problème ?",
                    "Les concepts complexes deviennent simples quand on les décompose. Explique-moi ce que tu ne comprends pas.",
                    "Tout le monde galère parfois. L'important c'est de persévérer. Reformule ta question, je vais t'aider différemment."
                ).randomChoice()

            // Questions spécifiques par matière
            subject == "Math" -> generateMathResponse(lowerMessage)
            subject == "Physics" -> generatePhysicsResponse(lowerMessage)
            subject == "Chemistry" -> generateChemistryResponse(lowerMessage)

            // Questions sur l'identité
            lowerMessage.contains("qui es-tu") || lowerMessage.contains("t'es qui") || lowerMessage.contains("c'est quoi smarty") ->
                "Je suis Smarty, ton assistant personnel d'apprentissage scientifique. Je peux résoudre des problèmes, expliquer des concepts et je m'améliore à chaque échange !"

            lowerMessage.contains("quoi") || lowerMessage.contains("what") || lowerMessage.contains("que peux-tu") ->
                "Je suis une IA conçue pour tes études scientifiques. Je peux :\n• Résoudre des équations\n• Calculer en physique (forces, énergies, électricité)\n• Aider en chimie (concentrations, pH, masses molaires)\n• Expliquer des concepts\n\nPose-moi une question !"

            lowerMessage.contains("pourquoi") ->
                "Bonne question ! Le 'pourquoi' est la base de la science. Précise de quel concept tu parles et je t'expliquerai."

            // Expressions de frustration
            lowerMessage.contains("bug") || lowerMessage.contains("marche pas") || lowerMessage.contains("erreur") ->
                "Je fais de mon mieux ! Si je ne comprends pas, essaie de reformuler avec les données chiffrées. Exemple : 'Résoudre 2x+5=15' ou 'F si m=10kg et a=2m/s²'"

            // Réponse par défaut contextuelle (Fallback)
            else -> {
                val contextPhrase = when (subject) {
                    "Math" -> " en mathématiques"
                    "Physics" -> " en physique"
                    "Chemistry" -> " en chimie"
                    else -> ""
                }
                listOf(
                    "Intéressant$contextPhrase. Peux-tu préciser ta question avec les données du problème ?",
                    "Je ne suis pas sûr de comprendre. Reformule avec les valeurs numériques si c'est un calcul.",
                    "C'est un point intéressant. Donne-moi plus de détails ou un exemple concret.",
                    "Peux-tu me donner les données du problème ? Par exemple : 'm=5, v=10' ou 'x^2-3x+2=0'",
                    "Je vois que tu parles$contextPhrase. Quelle formule ou quelle notion t'intéresse précisément ?"
                ).randomChoice()
            }
        }
    }

    private fun generateMathResponse(message: String): String {
        fun <T> List<T>.randomChoice(): T = this[kotlin.random.Random.nextInt(size)]
        
        return when {
            message.contains("équation") -> listOf(
                "Pour les équations, identifie d'abord l'inconnue. C'est du premier ou second degré ?",
                "Une équation, c'est comme une balance. Ce que tu fais à gauche, fais-le à droite.",
                "Cherche à isoler x. Tu as besoin d'aide pour une étape précise ?"
            ).randomChoice()
            
            message.contains("dérivée") -> listOf(
                "La dérivée, c'est la pente de la tangente. Rappelle-toi : (x²)' = 2x.",
                "C'est le taux de variation instantané. Tu veux la formule pour une fonction précise ?",
                "Dériver, c'est réduire le degré d'un polynôme. (x^n)' = n*x^(n-1)."
            ).randomChoice()
            
            else -> listOf(
                "En maths, la rigueur est clé. Qu'est-ce qui te bloque exactement ?",
                "On parle de quel chapitre ? Algèbre, Analyse, Géométrie ?",
                "Peux-tu me donner l'énoncé ou la formule qui te pose problème ?"
            ).randomChoice()
        }
    }

    private fun generatePhysicsResponse(message: String): String {
        fun <T> List<T>.randomChoice(): T = this[kotlin.random.Random.nextInt(size)]
        
        return when {
            message.contains("force") -> listOf(
                "Newton a dit : F = m * a. La force cause l'accélération.",
                "N'oublie pas les vecteurs ! Une force a une direction et un sens.",
                "Bilan des forces : Poids, Réaction, Frottements... Lesquels s'appliquent ici ?"
            ).randomChoice()
            
            message.contains("énergie") -> listOf(
                "Rien ne se perd, rien ne se crée, tout se transforme. L'énergie totale est conservée.",
                "Cinétique (vitesse) ou Potentielle (hauteur) ? Souvent, l'une devient l'autre.",
                "L'unité est le Joule (J). Tu cherches à calculer un travail ?"
            ).randomChoice()
            
            else -> listOf(
                "La physique décrit le monde. Fais un schéma, ça aide toujours !",
                "Quelles sont les données du problème ? Masse, Vitesse, Temps ?",
                "Tu es sur la mécanique, l'électricité ou la thermodynamique ?"
            ).randomChoice()
        }
    }

    private fun generateChemistryResponse(message: String): String {
        fun <T> List<T>.randomChoice(): T = this[kotlin.random.Random.nextInt(size)]
        
        return when {
            message.contains("réaction") -> listOf(
                "Lavoisier avait raison : la masse se conserve. Tes équations sont équilibrées ?",
                "Réactifs -> Produits. Vérifie que tu as le même nombre d'atomes de chaque côté.",
                "C'est une réaction acide-base ou d'oxydoréduction ?"
            ).randomChoice()
            
            message.contains("atome") || message.contains("molécule") -> listOf(
                "Le tableau périodique est ton ami. Regarde le nombre de protons.",
                "Les atomes cherchent la stabilité (règle de l'octet).",
                "Covalente ou ionique ? Le type de liaison change tout."
            ).randomChoice()
            
            else -> listOf(
                "La chimie, c'est de la cuisine précise. On parle de moles ou de concentration ?",
                "C = n/V. N'oublie jamais les unités !",
                "Tu bloques sur la structure ou sur les calculs ?"
            ).randomChoice()
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
        
        // Mots-clés enrichis pour les mathématiques
        val mathKeywords = listOf(
            "équation", "calcul", "math", "algèbre", "géométrie", "nombre", "chiffre",
            "addition", "soustraction", "multiplication", "division", "fraction",
            "dérivée", "intégrale", "fonction", "courbe", "graphe", "racine",
            "polynôme", "factoriser", "développer", "résoudre", "solution",
            "théorème", "pythagore", "thalès", "triangle", "cercle", "angle",
            "pourcentage", "%", "probabilité", "statistique", "moyenne"
        )
        
        // Mots-clés enrichis pour la physique
        val physicsKeywords = listOf(
            "force", "énergie", "physique", "mouvement", "vitesse", "accélération",
            "newton", "joule", "watt", "poids", "masse", "gravité", "g",
            "cinétique", "potentielle", "mécanique", "dynamique", "statique",
            "électricité", "tension", "courant", "résistance", "ohm", "volt", "ampère",
            "magnétisme", "champ", "onde", "lumière", "son", "fréquence",
            "thermodynamique", "chaleur", "température", "pression", "volume"
        )
        
        // Mots-clés enrichis pour la chimie
        val chemistryKeywords = listOf(
            "réaction", "acide", "chimie", "molécule", "atome", "ion",
            "concentration", "mole", "mol", "molaire", "masse molaire",
            "tableau périodique", "élément", "liaison", "covalent", "ionique",
            "oxydation", "réduction", "ph", "base", "sel", "équilibre",
            "catalyseur", "réactif", "produit", "stoechiométrie", "avogadro"
        )
        
        // Compter les correspondances pour chaque sujet
        val mathScore = mathKeywords.count { lowerMessage.contains(it) }
        val physicsScore = physicsKeywords.count { lowerMessage.contains(it) }
        val chemistryScore = chemistryKeywords.count { lowerMessage.contains(it) }
        
        // Retourner le sujet avec le score le plus élevé (minimum 1 correspondance)
        return when {
            mathScore > 0 && mathScore >= physicsScore && mathScore >= chemistryScore -> "Math"
            physicsScore > 0 && physicsScore >= mathScore && physicsScore >= chemistryScore -> "Physics"
            chemistryScore > 0 && chemistryScore >= mathScore && chemistryScore >= physicsScore -> "Chemistry"
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

    /**
     * Le Cerveau Scientifique de Smarty.
     * Une implémentation pure Kotlin légère mais robuste pour le niveau Lycée (Bac).
     * Pas de librairies lourdes, juste de la logique pure.
     */
    private class ScienceBrain {

        fun trySolve(message: String, subject: String?): String? {
            return try {
                solveArithmetic(message)
                    ?: solveEquation(message)
                    ?: solvePhysics(message)
                    ?: solveChemistry(message)
            } catch (e: Exception) {
                null // En cas d'échec, on laisse Smarty répondre normalement
            }
        }

        // --- 1. Moteur Arithmétique (Calculatrice) ---
        private fun solveArithmetic(message: String): String? {
            // Détecte si c'est une expression mathématique pure (ex: "12 * (3 + 4)")
            val cleanExpr = message.replace(" ", "").replace(",", ".")
            if (!cleanExpr.matches(Regex("^[0-9+\\-*/().^]+$"))) return null
            
            // Ignore si c'est juste un nombre ou une date
            if (cleanExpr.all { it.isDigit() || it == '.' } || cleanExpr.length < 3) return null

            return try {
                val result = eval(cleanExpr)
                val formatted = if (result % 1.0 == 0.0) result.toLong().toString() else String.format("%.2f", result)
                "J'ai calculé ça pour toi : $formatted"
            } catch (e: Exception) { null }
        }

        // Parser récursif simple (Shunting-yard simplifié)
        private fun eval(str: String): Double {
            return object : Any() {
                var pos = -1
                var ch = 0
                fun nextChar() { ch = if (++pos < str.length) str[pos].code else -1 }
                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == charToEat) { nextChar(); return true }
                    return false
                }
                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < str.length) throw RuntimeException("Caractère inattendu: " + ch.toChar())
                    return x
                }
                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        if      (eat('+'.code)) x += parseTerm() // addition
                        else if (eat('-'.code)) x -= parseTerm() // soustraction
                        else return x
                    }
                }
                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        if      (eat('*'.code)) x *= parseFactor() // multiplication
                        else if (eat('/'.code)) x /= parseFactor() // division
                        else return x
                    }
                }
                fun parseFactor(): Double {
                    if (eat('+'.code)) return parseFactor()
                    if (eat('-'.code)) return -parseFactor()
                    var x: Double
                    val startPos = pos
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                        while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                        x = str.substring(startPos, pos).toDouble()
                    } else {
                        throw RuntimeException("Inattendu: " + ch.toChar())
                    }
                    if (eat('^'.code)) x = Math.pow(x, parseFactor())
                    return x
                }
            }.parse()
        }

        // --- 2. Solveur d'Équations (Algèbre) ---
        private fun solveEquation(message: String): String? {
            val clean = message.replace(" ", "").replace(",", ".").lowercase()
            
            // Extraction intelligente : chercher une équation dans le texte
            // Patterns possibles : "2x+5=15", "résoudre x^2-5x+6=0", "x^2+3x=10", etc.
            val equationPattern = Regex("""([+-]?\d*\.?\d*x\^?2?[+-]?\d*\.?\d*x?[+-]?\d*\.?\d*)\s*=\s*([+-]?\d+\.?\d*)""")
            val match = equationPattern.find(clean) ?: return null
            
            val leftSide = match.groupValues[1]
            val rightSide = match.groupValues[2].toDoubleOrNull() ?: 0.0
            
            // Normaliser : tout ramener à "... = 0"
            val normalized = if (rightSide != 0.0) {
                leftSide.replace("=", "") + "${if (rightSide > 0) "-" else "+"}${kotlin.math.abs(rightSide)}"
            } else {
                leftSide
            }
            
            // Équation Quadratique : ax^2 + bx + c = 0
            if (normalized.contains("x^2") || normalized.contains("x2")) {
                try {
                    var a = 0.0
                    var b = 0.0
                    var c = 0.0
                    
                    // Parser amélioré : extraction des coefficients
                    val terms = normalized.replace("-", "+-").split("+").filter { it.isNotEmpty() }
                    for (term in terms) {
                        val trimmed = term.trim()
                        when {
                            trimmed.contains("x^2") || trimmed.contains("x2") -> {
                                val coef = trimmed.replace("x^2", "").replace("x2", "")
                                a = when {
                                    coef.isEmpty() || coef == "+" -> 1.0
                                    coef == "-" -> -1.0
                                    else -> coef.toDoubleOrNull() ?: 1.0
                                }
                            }
                            trimmed.contains("x") -> {
                                val coef = trimmed.replace("x", "")
                                b = when {
                                    coef.isEmpty() || coef == "+" -> 1.0
                                    coef == "-" -> -1.0
                                    else -> coef.toDoubleOrNull() ?: 1.0
                                }
                            }
                            else -> {
                                c = trimmed.toDoubleOrNull() ?: 0.0
                            }
                        }
                    }

                    val delta = b * b - 4 * a * c
                    val steps = StringBuilder("**Équation du second degré** :\n")
                    steps.append("$$ ${formatCoef(a)}x^2 ${formatTerm(b, "x")} ${formatTerm(c, "")} = 0 $$\n\n")
                    steps.append("**Étape 1 :** Calcul du discriminant\n")
                    steps.append("$$ \\Delta = b^2 - 4ac = (${formatNum(b)})^2 - 4(${formatNum(a)})(${formatNum(c)}) = ${formatNum(delta)} $$\n\n")

                    return if (delta > 0) {
                        val x1 = (-b - Math.sqrt(delta)) / (2 * a)
                        val x2 = (-b + Math.sqrt(delta)) / (2 * a)
                        steps.append("**Étape 2 :** $\\Delta > 0$ → Deux solutions réelles\n")
                        steps.append("$$ x_1 = \\frac{-b - \\sqrt{\\Delta}}{2a} = ${formatNum(x1)} $$\n")
                        steps.append("$$ x_2 = \\frac{-b + \\sqrt{\\Delta}}{2a} = ${formatNum(x2)} $$")
                        steps.toString()
                    } else if (delta == 0.0) {
                        val x0 = -b / (2 * a)
                        steps.append("**Étape 2 :** $\\Delta = 0$ → Une solution double\n")
                        steps.append("$$ x_0 = \\frac{-b}{2a} = ${formatNum(x0)} $$")
                        steps.toString()
                    } else {
                        steps.append("**Étape 2 :** $\\Delta < 0$ → Pas de solution réelle\n")
                        steps.append("(Solutions complexes uniquement)")
                        steps.toString()
                    }
                } catch (e: Exception) { 
                    Logger.w("ScienceBrain", "Failed to solve quadratic equation: ${e.message}")
                    return null 
                }
            }
            
            // Équation Linéaire : ax + b = c (formats plus souples)
            // Accepte : "2x+5=15", "3x=12", "-x+7=3", etc.
            val linearPattern = Regex("""([+-]?\d*\.?\d*)x([+-]?\d*\.?\d*)=([+-]?\d+\.?\d*)""")
            val linearMatch = linearPattern.find(clean)
            if (linearMatch != null) {
                try {
                    val aStr = linearMatch.groupValues[1]
                    val bStr = linearMatch.groupValues[2]
                    val cStr = linearMatch.groupValues[3]
                    
                    val aVal = when {
                        aStr.isEmpty() || aStr == "+" -> 1.0
                        aStr == "-" -> -1.0
                        else -> aStr.toDoubleOrNull() ?: 1.0
                    }
                    val bVal = if (bStr.isEmpty()) 0.0 else bStr.toDoubleOrNull() ?: 0.0
                    val cVal = cStr.toDoubleOrNull() ?: 0.0
                    
                    val steps = StringBuilder("**Équation linéaire** :\n")
                    steps.append("$$ ${formatCoef(aVal)}x ${formatTerm(bVal, "")} = ${formatNum(cVal)} $$\n\n")
                    steps.append("**Étape 1 :** Isoler le terme en x\n")
                    steps.append("$$ ${formatCoef(aVal)}x = ${formatNum(cVal)} ${formatTerm(-bVal, "")} $$\n\n")
                    steps.append("**Étape 2 :** Calculer\n")
                    steps.append("$$ ${formatCoef(aVal)}x = ${formatNum(cVal - bVal)} $$\n\n")
                    steps.append("**Étape 3 :** Diviser par le coefficient de x\n")
                    steps.append("$$ x = \\frac{${formatNum(cVal - bVal)}}{${formatNum(aVal)}} = ${formatNum((cVal - bVal) / aVal)} $$")
                    return steps.toString()
                } catch (e: Exception) {
                    Logger.w("ScienceBrain", "Failed to solve linear equation: ${e.message}")
                    return null
                }
            }

            return null
        }
        
        // Helpers pour formater les nombres et termes
        private fun formatNum(n: Double): String = if (n % 1.0 == 0.0) n.toLong().toString() else String.format("%.2f", n)
        private fun formatCoef(c: Double): String = when {
            c == 1.0 -> ""
            c == -1.0 -> "-"
            c % 1.0 == 0.0 -> c.toLong().toString()
            else -> String.format("%.2f", c)
        }
        private fun formatTerm(coef: Double, variable: String): String = when {
            coef == 0.0 -> ""
            coef > 0 -> "+ ${formatCoef(coef)}$variable"
            else -> "- ${formatCoef(-coef)}$variable"
        }

        // --- 3. Moteur Physique (Lycée) ---
        private fun solvePhysics(message: String): String? {
            val lower = message.lowercase()
            
            // Extraction de variables améliorée (ex: "m=10kg", "masse = 10", "la vitesse est de 5")
            fun getVar(vararg names: String): Double? {
                for (name in names) {
                    // Pattern 1: "m=10" ou "m = 10"
                    val regex1 = Regex("""$name\s*[=:]\s*([0-9.]+)""")
                    regex1.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
                    
                    // Pattern 2: "masse de 10" ou "vitesse est 5"
                    val regex2 = Regex("""$name\s+(?:de|est|:)\s*([0-9.]+)""")
                    regex2.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
                    
                    // Pattern 3: "10 kg" (pour masse), "5 m/s" (pour vitesse)
                    if (name == "m" || name == "masse") {
                        val regex3 = Regex("""([0-9.]+)\s*kg""")
                        regex3.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
                    }
                    if (name == "v" || name == "vitesse") {
                        val regex4 = Regex("""([0-9.]+)\s*m/s""")
                        regex4.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
                    }
                }
                return null
            }

            // Loi de Newton : F = m * a
            if (lower.contains("force") || lower.contains("newton") || lower.contains("accélération")) {
                val m = getVar("m", "masse")
                val a = getVar("a", "accélération", "acceleration")
                val f = getVar("f", "force")
                
                if (m != null && a != null && f == null) {
                    val result = m * a
                    return "**Loi de Newton** (2ème loi) :\n" +
                           "$$ F = m \\cdot a $$\n\n" +
                           "**Données :**\n" +
                           "- Masse : $m kg\n" +
                           "- Accélération : $a m/s²\n\n" +
                           "**Calcul :**\n" +
                           "$$ F = ${formatNum(m)} \\times ${formatNum(a)} = ${formatNum(result)} \\text{ N} $$"
                }
                if (f != null && m != null && a == null) {
                    val result = f / m
                    return "**Loi de Newton** ($$ a = \\frac{F}{m} $$) :\n\n" +
                           "**Données :**\n" +
                           "- Force : $f N\n" +
                           "- Masse : $m kg\n\n" +
                           "**Calcul :**\n" +
                           "$$ a = \\frac{${formatNum(f)}}{${formatNum(m)}} = ${formatNum(result)} \\text{ m/s}^2 $$"
                }
                if (f != null && a != null && m == null) {
                    val result = f / a
                    return "**Loi de Newton** ($$ m = \\frac{F}{a} $$) :\n\n" +
                           "**Calcul :**\n" +
                           "$$ m = \\frac{${formatNum(f)}}{${formatNum(a)}} = ${formatNum(result)} \\text{ kg} $$"
                }
            }

            // Énergie Cinétique : Ec = 0.5 * m * v^2
            if (lower.contains("cinétique") || lower.contains("ec") || (lower.contains("énergie") && lower.contains("vitesse"))) {
                val m = getVar("m", "masse")
                val v = getVar("v", "vitesse")
                if (m != null && v != null) {
                    val result = 0.5 * m * v * v
                    return "**Énergie Cinétique** :\n" +
                           "$$ E_c = \\frac{1}{2} m v^2 $$\n\n" +
                           "**Données :**\n" +
                           "- Masse : $m kg\n" +
                           "- Vitesse : $v m/s\n\n" +
                           "**Calcul :**\n" +
                           "$$ E_c = 0.5 \\times ${formatNum(m)} \\times ${formatNum(v)}^2 = ${formatNum(result)} \\text{ J} $$"
                }
            }
            
            // Énergie Potentielle : Ep = m * g * h
            if (lower.contains("potentielle") || lower.contains("ep") || (lower.contains("énergie") && lower.contains("hauteur"))) {
                val m = getVar("m", "masse")
                val h = getVar("h", "hauteur", "height")
                val g = getVar("g") ?: 9.8 // Par défaut sur Terre
                if (m != null && h != null) {
                    val result = m * g * h
                    return "**Énergie Potentielle de pesanteur** :\n" +
                           "$$ E_p = m \\cdot g \\cdot h $$\n\n" +
                           "**Données :**\n" +
                           "- Masse : $m kg\n" +
                           "- Hauteur : $h m\n" +
                           "- g : ${formatNum(g)} m/s² (gravité terrestre)\n\n" +
                           "**Calcul :**\n" +
                           "$$ E_p = ${formatNum(m)} \\times ${formatNum(g)} \\times ${formatNum(h)} = ${formatNum(result)} \\text{ J} $$"
                }
            }
            
            // Loi d'Ohm : U = R * I
            if (lower.contains("ohm") || lower.contains("tension") || lower.contains("résistance") || lower.contains("courant")) {
                val u = getVar("u", "tension", "voltage")
                val r = getVar("r", "résistance", "resistance")
                val i = getVar("i", "courant", "intensité", "intensite")
                
                if (r != null && i != null && u == null) {
                    val result = r * i
                    return "**Loi d'Ohm** ($$ U = R \\cdot I $$) :\n\n" +
                           "**Données :**\n" +
                           "- Résistance : $r Ω\n" +
                           "- Intensité : $i A\n\n" +
                           "**Calcul :**\n" +
                           "$$ U = ${formatNum(r)} \\times ${formatNum(i)} = ${formatNum(result)} \\text{ V} $$"
                }
                if (u != null && r != null && i == null) {
                    val result = u / r
                    return "**Loi d'Ohm** ($$ I = \\frac{U}{R} $$) :\n\n" +
                           "**Calcul :**\n" +
                           "$$ I = \\frac{${formatNum(u)}}{${formatNum(r)}} = ${formatNum(result)} \\text{ A} $$"
                }
                if (u != null && i != null && r == null) {
                    val result = u / i
                    return "**Loi d'Ohm** ($$ R = \\frac{U}{I} $$) :\n\n" +
                           "**Calcul :**\n" +
                           "$$ R = \\frac{${formatNum(u)}}{${formatNum(i)}} = ${formatNum(result)} \\Omega $$"
                }
            }

            return null
        }

        // --- 4. Moteur Chimie (Lycée) ---
        private fun solveChemistry(message: String): String? {
            val lower = message.lowercase()
            
            // Extraction de variables améliorée
            fun getVar(vararg names: String): Double? {
                for (name in names) {
                    val regex1 = Regex("""$name\s*[=:]\s*([0-9.]+)""")
                    regex1.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
                    
                    val regex2 = Regex("""$name\s+(?:de|est|:)\s*([0-9.]+)""")
                    regex2.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
                }
                return null
            }

            // Concentration : C = n / V
            if (lower.contains("concentration") || lower.contains("molaire") || lower.contains("c=n/v")) {
                val n = getVar("n", "mole", "moles", "quantité")
                val v = getVar("v", "volume")
                val c = getVar("c", "concentration")
                
                if (n != null && v != null && c == null) {
                    val result = n / v
                    return "**Concentration molaire** :\n" +
                           "$$ C = \\frac{n}{V} $$\n\n" +
                           "**Données :**\n" +
                           "- Quantité de matière : $n mol\n" +
                           "- Volume : $v L\n\n" +
                           "**Calcul :**\n" +
                           "$$ C = \\frac{${formatNum(n)}}{${formatNum(v)}} = ${formatNum(result)} \\text{ mol/L} $$"
                }
                if (c != null && v != null && n == null) {
                    val result = c * v
                    return "**Quantité de matière** ($$ n = C \\cdot V $$) :\n\n" +
                           "**Données :**\n" +
                           "- Concentration : $c mol/L\n" +
                           "- Volume : $v L\n\n" +
                           "**Calcul :**\n" +
                           "$$ n = ${formatNum(c)} \\times ${formatNum(v)} = ${formatNum(result)} \\text{ mol} $$"
                }
                if (c != null && n != null && v == null) {
                    val result = n / c
                    return "**Volume** ($$ V = \\frac{n}{C} $$) :\n\n" +
                           "**Calcul :**\n" +
                           "$$ V = \\frac{${formatNum(n)}}{${formatNum(c)}} = ${formatNum(result)} \\text{ L} $$"
                }
            }
            
            // Dilution : C1*V1 = C2*V2
            if (lower.contains("dilution") || lower.contains("diluer")) {
                val c1 = getVar("c1", "concentration1", "ci")
                val v1 = getVar("v1", "volume1", "vi")
                val c2 = getVar("c2", "concentration2", "cf")
                val v2 = getVar("v2", "volume2", "vf")
                
                if (c1 != null && v1 != null && c2 != null && v2 == null) {
                    val result = (c1 * v1) / c2
                    return "**Dilution** ($$ C_1 V_1 = C_2 V_2 $$) :\n\n" +
                           "**Données :**\n" +
                           "- C₁ = ${formatNum(c1)} mol/L\n" +
                           "- V₁ = ${formatNum(v1)} L\n" +
                           "- C₂ = ${formatNum(c2)} mol/L\n\n" +
                           "**Calcul de V₂ :**\n" +
                           "$$ V_2 = \\frac{C_1 V_1}{C_2} = \\frac{${formatNum(c1)} \\times ${formatNum(v1)}}{${formatNum(c2)}} = ${formatNum(result)} \\text{ L} $$"
                }
            }
            
            // pH (simplifié : pH = -log[H+])
            if (lower.contains("ph") && (lower.contains("concentration") || lower.contains("h+"))) {
                val h = getVar("h+", "h", "[h+]")
                if (h != null) {
                    val pH = -Math.log10(h)
                    return "**Calcul du pH** :\n" +
                           "$$ pH = -\\log[H^+] $$\n\n" +
                           "**Données :**\n" +
                           "- [H⁺] = $h mol/L\n\n" +
                           "**Calcul :**\n" +
                           "$$ pH = -\\log(${formatNum(h)}) = ${formatNum(pH)} $$"
                }
            }

            // Masse molaire simple (molécules courantes)
            if (lower.contains("masse molaire") || lower.contains("m=")) {
                val molecules = mapOf(
                    "h2o" to Pair("H₂O (Eau)", 18.0),
                    "eau" to Pair("H₂O (Eau)", 18.0),
                    "co2" to Pair("CO₂ (Dioxyde de carbone)", 44.0),
                    "dioxyde" to Pair("CO₂ (Dioxyde de carbone)", 44.0),
                    "o2" to Pair("O₂ (Dioxygène)", 32.0),
                    "n2" to Pair("N₂ (Diazote)", 28.0),
                    "nacl" to Pair("NaCl (Chlorure de sodium)", 58.5),
                    "sel" to Pair("NaCl (Chlorure de sodium)", 58.5),
                    "ch4" to Pair("CH₄ (Méthane)", 16.0),
                    "nh3" to Pair("NH₃ (Ammoniac)", 17.0),
                    "h2so4" to Pair("H₂SO₄ (Acide sulfurique)", 98.0)
                )
                
                for ((key, value) in molecules) {
                    if (lower.contains(key)) {
                        return "**Masse molaire de ${value.first}** :\n" +
                               "$$ M = ${formatNum(value.second)} \\text{ g/mol} $$"
                    }
                }
            }

            return null
        }
    }
}
