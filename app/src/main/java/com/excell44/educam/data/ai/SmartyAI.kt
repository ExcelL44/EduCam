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

            // 5. Apprentissage différé : On n'apprend PLUS automatiquement ici.
            // L'apprentissage est déclenché uniquement par le feedback positif de l'utilisateur (ChatViewModel).

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
            lowerMessage.contains("bonjour") || lowerMessage.contains("salut") || lowerMessage.contains("hello") ->
                listOf(
                    "Bonjour ! Je suis Smarty. Sur quel sujet travailles-tu aujourd'hui ?",
                    "Salut ! Prêt à apprendre ? Je t'écoute.",
                    "Hello ! Je suis là pour t'aider. Une question en tête ?",
                    "Coucou ! On révise quoi aujourd'hui ?"
                ).randomChoice()

            lowerMessage.contains("merci") ->
                listOf(
                    "Avec plaisir ! N'hésite pas si tu as d'autres questions.",
                    "De rien ! C'est mon travail.",
                    "Je t'en prie. On continue ?",
                    "Ravi d'avoir pu t'aider !"
                ).randomChoice()

            // Aide générale
            lowerMessage.contains("aide") || lowerMessage.contains("help") ->
                listOf(
                    "Je peux t'aider en maths, physique, chimie... Pose-moi simplement ta question.",
                    "Coincé sur un exercice ? Dis-moi tout, je vais essayer de t'expliquer.",
                    "Je suis là pour ça. Tu peux me demander des définitions, des formules ou des méthodes."
                ).randomChoice()

            // Questions sur les matières (Contextuel)
            subject == "Math" -> generateMathResponse(lowerMessage)
            subject == "Physics" -> generatePhysicsResponse(lowerMessage)
            subject == "Chemistry" -> generateChemistryResponse(lowerMessage)

            // Questions générales sur l'identité
            lowerMessage.contains("qui es-tu") || lowerMessage.contains("t'es qui") ->
                "Je suis Smarty, ton assistant personnel d'apprentissage. Je m'améliore à chaque fois qu'on discute !"

            lowerMessage.contains("quoi") || lowerMessage.contains("what") ->
                "Je suis une IA conçue pour tes études. Je peux expliquer des concepts et apprendre de nos échanges."

            lowerMessage.contains("comment") || lowerMessage.contains("how") ->
                "Pose-moi une question sur tes cours ! Si ma réponse te plaît, mets un pouce bleu, ça m'aide à apprendre."

            // Réponse par défaut (Fallback)
            else -> {
                val contextPhrase = if (subject != null) " concernant $subject" else ""
                listOf(
                    "Intéressant. Peux-tu préciser ta pensée$contextPhrase ?",
                    "Je ne suis pas sûr de comprendre. Tu peux reformuler ?",
                    "C'est un point intéressant. Dis-m'en plus.",
                    "Peux-tu me donner un exemple de ce que tu cherches ?"
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
            val clean = message.replace(" ", "").replace(",", ".")
            
            // Équation Quadratique : ax^2 + bx + c = 0
            // Regex robuste pour capturer les coefficients (supporte -x^2, +x, etc.)
            // Note: C'est une simplification pour l'exemple, couvre les cas standards "ax^2+bx+c=0"
            if (clean.contains("x^2") && clean.contains("=0")) {
                try {
                    // Extraction très basique des coefficients (à améliorer pour prod)
                    // On suppose format standard pour la démo : ax^2+bx+c=0
                    // Ceci est une version simplifiée "Smart Code"
                    var a = 1.0; var b = 0.0; var c = 0.0
                    
                    // Logique d'extraction simplifiée pour l'exemple
                    // Dans un vrai parser, on itérerait sur les termes
                    val parts = clean.split("=0")[0].replace("-", "+-").split("+").filter { it.isNotEmpty() }
                    for (part in parts) {
                        if (part.contains("x^2")) {
                            val coef = part.replace("x^2", "")
                            a = if (coef.isEmpty() || coef == "+") 1.0 else if (coef == "-") -1.0 else coef.toDouble()
                        } else if (part.contains("x")) {
                            val coef = part.replace("x", "")
                            b = if (coef.isEmpty() || coef == "+") 1.0 else if (coef == "-") -1.0 else coef.toDouble()
                        } else {
                            c = part.toDouble()
                        }
                    }

                    val delta = b * b - 4 * a * c
                    val steps = StringBuilder("C'est une équation du second degré ($a x² + $b x + $c = 0).\n")
                    steps.append("1. Calcul du discriminant Δ = b² - 4ac\n")
                    steps.append("   Δ = $b² - 4*$a*$c = $delta\n")

                    return if (delta > 0) {
                        val x1 = (-b - Math.sqrt(delta)) / (2 * a)
                        val x2 = (-b + Math.sqrt(delta)) / (2 * a)
                        steps.append("2. Δ > 0, deux solutions réelles :\n")
                        steps.append("   x1 = (-b - √Δ) / 2a = ${String.format("%.2f", x1)}\n")
                        steps.append("   x2 = (-b + √Δ) / 2a = ${String.format("%.2f", x2)}")
                        steps.toString()
                    } else if (delta == 0.0) {
                        val x0 = -b / (2 * a)
                        steps.append("2. Δ = 0, une solution double :\n")
                        steps.append("   x0 = -b / 2a = ${String.format("%.2f", x0)}")
                        steps.toString()
                    } else {
                        steps.append("2. Δ < 0, pas de solution réelle (solutions complexes uniquement).")
                        steps.toString()
                    }
                } catch (e: Exception) { return null }
            }
            
            // Équation Linéaire : ax + b = c
            // Pattern simple : 2x + 5 = 15
            val linearMatch = Regex("([\\d.-]+)x\\+([\\d.-]+)=([\\d.-]+)").find(clean)
            if (linearMatch != null) {
                val (a, b, res) = linearMatch.destructured
                val aVal = a.toDouble()
                val bVal = b.toDouble()
                val resVal = res.toDouble()
                
                val steps = StringBuilder("Résolution de l'équation linéaire ${a}x + $b = $res :\n")
                steps.append("1. Isoler x : ${a}x = $res - $b\n")
                steps.append("2. ${a}x = ${resVal - bVal}\n")
                steps.append("3. x = ${(resVal - bVal)} / $aVal\n")
                steps.append("4. x = ${String.format("%.2f", (resVal - bVal) / aVal)}")
                return steps.toString()
            }

            return null
        }

        // --- 3. Moteur Physique (Lycée) ---
        private fun solvePhysics(message: String): String? {
            val lower = message.lowercase()
            
            // Extraction de variables (ex: "m=10kg", "v=5")
            fun getVar(name: String): Double? {
                val regex = Regex("$name\\s*=\\s*([0-9.]+)")
                return regex.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()
            }

            // Loi de Newton : F = m * a
            if (lower.contains("force") || lower.contains("newton")) {
                val m = getVar("m")
                val a = getVar("a")
                val f = getVar("f")
                
                if (m != null && a != null && f == null) {
                    return "D'après la 2ème loi de Newton (F = m*a) :\nF = $m kg * $a m/s² = ${m * a} N"
                }
                if (f != null && m != null && a == null) {
                    return "D'après F = m*a, on cherche l'accélération a = F/m :\na = $f N / $m kg = ${f / m} m/s²"
                }
            }

            // Énergie Cinétique : Ec = 0.5 * m * v^2
            if (lower.contains("cinétique") || lower.contains("ec")) {
                val m = getVar("m")
                val v = getVar("v")
                if (m != null && v != null) {
                    return "L'énergie cinétique se calcule par Ec = 1/2 * m * v² :\nEc = 0.5 * $m * $v² = ${0.5 * m * v * v} Joules"
                }
            }
            
            // Loi d'Ohm : U = R * I
            if (lower.contains("ohm") || lower.contains("tension") || lower.contains("résistance")) {
                val u = getVar("u")
                val r = getVar("r")
                val i = getVar("i")
                
                if (r != null && i != null) return "Loi d'Ohm (U = R*I) :\nU = $r Ω * $i A = ${r * i} V"
                if (u != null && r != null) return "Loi d'Ohm (I = U/R) :\nI = $u V / $r Ω = ${u / r} A"
                if (u != null && i != null) return "Loi d'Ohm (R = U/I) :\nR = $u V / $i A = ${u / i} Ω"
            }

            return null
        }

        // --- 4. Moteur Chimie (Lycée) ---
        private fun solveChemistry(message: String): String? {
            val lower = message.lowercase()
            
            fun getVar(name: String): Double? {
                val regex = Regex("$name\\s*=\\s*([0-9.]+)")
                return regex.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()
            }

            // Concentration : C = n / V
            if (lower.contains("concentration") || lower.contains("molaire")) {
                val n = getVar("n")
                val v = getVar("v")
                val c = getVar("c")
                
                if (n != null && v != null) return "Concentration molaire (C = n/V) :\nC = $n mol / $v L = ${n / v} mol/L"
                if (c != null && v != null) return "Quantité de matière (n = C*V) :\nn = $c mol/L * $v L = ${c * v} mol"
            }

            // Masse molaire simple (ex: H2O)
            // C'est basique, on supporte quelques atomes courants
            if (lower.contains("masse molaire")) {
                val weights = mapOf("h" to 1.0, "c" to 12.0, "o" to 16.0, "n" to 14.0, "na" to 23.0, "cl" to 35.5)
                // Regex pour trouver une formule simple (ex: H2O, CO2)
                // Pour l'instant, on répond juste avec les données atomiques si on détecte des atomes
                if (lower.contains("h") && lower.contains("o")) {
                    return "Pour H2O (Eau) : 2*H(1.0) + 1*O(16.0) = 18.0 g/mol"
                }
                if (lower.contains("c") && lower.contains("o")) {
                    return "Pour CO2 (Dioxyde de carbone) : 1*C(12.0) + 2*O(16.0) = 44.0 g/mol"
                }
            }

            return null
        }
    }
}
