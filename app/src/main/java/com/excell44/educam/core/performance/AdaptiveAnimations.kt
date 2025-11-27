package com.excell44.educam.core.performance

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Utilitaires pour ajuster les animations selon le mode de performance.
 */

/**
 * Obtient la durée d'animation ajustée selon le mode de performance.
 * 
 * @param baseDuration Durée de base en millisecondes
 * @return Durée ajustée selon le contexte
 */
@Composable
fun rememberAdaptiveDuration(baseDuration: Int): Int {
    val context = LocalContext.current
    val performanceManager = remember { PerformanceManager.getInstance(context) }
    val multiplier = remember { performanceManager.getPerformanceMultiplier() }
    
    return (baseDuration * multiplier).toInt()
}

/**
 * Crée un AnimationSpec adapté au mode de performance.
 */
@Composable
fun <T> rememberAdaptiveAnimation(
    baseDuration: Int = 300,
    delayMillis: Int = 0
): AnimationSpec<T> {
    val duration = rememberAdaptiveDuration(baseDuration)
    return remember(duration) {
        tween(durationMillis = duration, delayMillis = delayMillis)
    }
}

/**
 * State qui observe le mode de performance et se met à jour automatiquement.
 */
@Composable
fun rememberPerformanceMode(): State<PerformanceManager.PerformanceMode> {
    val context = LocalContext.current
    val performanceManager = remember { PerformanceManager.getInstance(context) }
    
    // Observe les changements toutes les 10 secondes
    return produceState(
        initialValue = performanceManager.getRecommendedPerformanceMode(),
        producer = {
            while (true) {
                kotlinx.coroutines.delay(10_000L) // Check every 10 seconds
                value = performanceManager.getRecommendedPerformanceMode()
            }
        }
    )
}

/**
 * Décide si une fonctionnalité lourde devrait être activée.
 */
@Composable
fun shouldEnableHeavyFeature(): Boolean {
    val mode = rememberPerformanceMode().value
    return mode != PerformanceManager.PerformanceMode.LOW_POWER
}
