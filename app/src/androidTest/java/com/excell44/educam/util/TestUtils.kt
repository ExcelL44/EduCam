package com.excell44.educam.util

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Utilities pour simplifier l'écriture des tests d'intégration.
 */
object TestUtils {
    
    /**
     * Attend qu'un noeud avec le texte donné apparaisse.
     * Timeout configurable.
     */
    fun ComposeTestRule.waitForText(
        text: String,
        substring: Boolean = false,
        timeoutMs: Long = 5000
    ) {
        waitUntil(timeoutMillis = timeoutMs) {
            onAllNodesWithText(text, substring = substring)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
    
    /**
     * Attend qu'un noeud avec le tag donné apparaisse.
     */
    fun ComposeTestRule.waitForTag(
        testTag: String,
        timeoutMs: Long = 5000
    ) {
        waitUntil(timeoutMillis = timeoutMs) {
            onAllNodesWithTag(testTag)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
    
    /**
     * Simule un délai humain (évite détection anti-triche).
     */
    suspend fun humanDelay(minMs: Long = 1500, maxMs: Long = 3000) {
        val delay = (minMs..maxMs).random()
        delay(delay)
    }
    
    /**
     * Récupère l'utilisation mémoire actuelle en MB.
     */
    fun getMemoryUsageMB(): Long {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        return usedMemory / 1024 / 1024
    }
    
    /**
     * Force un garbage collection et attend qu'il se termine.
     */
    fun forceGC() {
        runBlocking {
            System.gc()
            delay(500) // Laisse temps au GC
            System.runFinalization()
            delay(500)
        }
    }
    
    /**
     * Vérifie si l'activité est en cours (pas crashed).
     */
    fun ComposeTestRule.assertActivityAlive() {
        assert(!activity.isFinishing) {
            "Activity has finished (possible crash)"
        }
        assert(!activity.isDestroyed) {
            "Activity has been destroyed"
        }
    }
    
    /**
     * Log formaté pour les tests.
     */
    fun testLog(tag: String, message: String) {
        println("🧪 [$tag] $message")
    }
}

/**
 * Extensions pour SemanticsNodeInteraction.
 */
fun SemanticsNodeInteractionCollection.onFirst(): SemanticsNodeInteraction {
    return get(0)
}

fun SemanticsNodeInteractionCollection.onLast(): SemanticsNodeInteraction {
    val nodes = fetchSemanticsNodes()
    return get(nodes.size - 1)
}
