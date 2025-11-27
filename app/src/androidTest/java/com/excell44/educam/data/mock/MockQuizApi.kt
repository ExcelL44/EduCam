package com.excell44.educam.data.mock

import com.excell44.educam.data.model.QuizQuestion
import java.io.IOException

/**
 * Mock API pour tester le comportement offline-first.
 * Simule l'échec réseau pour forcer l'utilisation du cache local.
 */
class MockQuizApi {
    
    /**
     * Simule echec de synchronisation (offline)
     */
    suspend fun syncQuestions(): List<QuizQuestion> {
        throw IOException("Pas de réseau (simulation offline pour test)")
    }
    
    /**
     * Simule échec d'envoi des résultats
     */
    suspend fun submitResults(sessionId: String): Boolean {
        // Force la persistance locale uniquement
        return false
    }
    
    /**
     * Simule délai réseau lent (2G)
     */
    suspend fun fetchQuestionsWithDelay(delayMs: Long = 5000): List<QuizQuestion> {
        kotlinx.coroutines.delay(delayMs)
        throw IOException("Timeout après ${delayMs}ms")
    }
}
