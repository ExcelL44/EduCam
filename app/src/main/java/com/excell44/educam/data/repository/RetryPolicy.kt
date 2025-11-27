package com.excell44.educam.data.repository

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

/**
 * Classe utilitaire pour gérer la stratégie de retry avec backoff exponentiel.
 */
class RetryPolicy(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 10000L,
    private val factor: Double = 2.0
) {
    /**
     * Exécute une opération avec retry automatique et backoff exponentiel.
     * 
     * @param operation L'opération à exécuter
     * @return Le résultat de l'opération
     * @throws Exception La dernière exception si toutes les tentatives échouent
     */
    suspend fun <T> execute(operation: suspend () -> T): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                
                // Ne pas retry sur la dernière tentative
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    // Calcul du délai pour la prochaine tentative (backoff exponentiel)
                    currentDelay = min(
                        (currentDelay * factor.pow(attempt + 1)).toLong(),
                        maxDelayMs
                    )
                }
            }
        }
        
        // Si on arrive ici, toutes les tentatives ont échoué
        throw lastException ?: Exception("Operation failed after $maxRetries retries")
    }
}
