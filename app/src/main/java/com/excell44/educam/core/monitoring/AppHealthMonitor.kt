package com.excell44.educam.core.monitoring

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Moniteur de santé de l'application en temps réel.
 * Collecte et expose des métriques critiques.
 */
class AppHealthMonitor private constructor(
    private val context: Context
) {
    
    private val _healthMetrics = MutableStateFlow(HealthMetrics())
    val healthMetrics: StateFlow<HealthMetrics> = _healthMetrics.asStateFlow()
    
    private val crashHistory = mutableListOf<CrashEvent>()
    private val performanceHistory = ConcurrentHashMap<String, List<PerformanceMetric>>()
    
    /**
     * Enregistre un crash.
     */
    fun recordCrash(throwable: Throwable, screenName: String? = null) {
        val crash = CrashEvent(
            timestamp = System.currentTimeMillis(),
            exceptionType = throwable.javaClass.simpleName,
            message = throwable.message ?: "Unknown error",
            stackTrace = throwable.stackTraceToString(),
            screenName = screenName
        )
        
        crashHistory.add(crash)
        
        // Garder seulement les 50 derniers crashs
        if (crashHistory.size > 50) {
            crashHistory.removeAt(0)
        }
        
        updateMetrics()
    }
    
    /**
     * Enregistre une métrique de performance.
     */
    fun recordPerformance(
        operation: String,
        durationMs: Long,
        success: Boolean = true
    ) {
        val metric = PerformanceMetric(
            timestamp = System.currentTimeMillis(),
            operation = operation,
            durationMs = durationMs,
            success = success
        )
        
        val history = performanceHistory.getOrPut(operation) { emptyList() }
        performanceHistory[operation] = (history + metric).takeLast(100)
        
        updateMetrics()
    }
    
    /**
     * Enregistre une interaction utilisateur.
     */
    fun recordUserInteraction(action: String, screenName: String) {
        val currentMetrics = _healthMetrics.value
        _healthMetrics.value = currentMetrics.copy(
            totalUserInteractions = currentMetrics.totalUserInteractions + 1,
            lastInteractionTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Met à jour le temps de session.
     */
    fun updateSessionDuration(durationMs: Long) {
        val currentMetrics = _healthMetrics.value
        _healthMetrics.value = currentMetrics.copy(
            sessionDuration = durationMs
        )
    }
    
    /**
     * Enregistre un changement d'écran.
     */
    fun recordScreenView(screenName: String) {
        val currentMetrics = _healthMetrics.value
        _healthMetrics.value = currentMetrics.copy(
            currentScreen = screenName,
            totalScreenViews = currentMetrics.totalScreenViews + 1
        )
    }
    
    /**
     * Obtient les crashs récents.
     */
    fun getRecentCrashes(limit: Int = 10): List<CrashEvent> {
        return crashHistory.takeLast(limit).reversed()
    }
    
    /**
     * Obtient les métriques de performance pour une opération.
     */
    fun getPerformanceMetrics(operation: String): PerformanceStats? {
        val metrics = performanceHistory[operation] ?: return null
        if (metrics.isEmpty()) return null
        
        val durations = metrics.map { it.durationMs }
        val successCount = metrics.count { it.success }
        
        return PerformanceStats(
            operation = operation,
            averageDurationMs = durations.average().toLong(),
            minDurationMs = durations.minOrNull() ?: 0L,
            maxDurationMs = durations.maxOrNull() ?: 0L,
            successRate = (successCount.toFloat() / metrics.size) * 100,
            totalCalls = metrics.size
        )
    }
    
    /**
     * Obtient toutes les statistiques de performance.
     */
    fun getAllPerformanceStats(): List<PerformanceStats> {
        return performanceHistory.keys.mapNotNull { getPerformanceMetrics(it) }
    }
    
    /**
     * Obtient le taux de crash (crashs par heure de session).
     */
    fun getCrashRate(): Float {
        val sessionHours = (_healthMetrics.value.sessionDuration / (1000f * 60f * 60f))
        if (sessionHours == 0f) return 0f
        return crashHistory.size / sessionHours
    }
    
    /**
     * Vérifie si l'app est en bonne santé.
     */
    fun isHealthy(): Boolean {
        val crashRate = getCrashRate()
        val recentCrashes = getRecentCrashes(5).size
        
        return crashRate < 2f && recentCrashes < 3
    }
    
    /**
     * Reset des métriques (pour debug ou après factory reset).
     */
    fun resetMetrics() {
        crashHistory.clear()
        performanceHistory.clear()
        _healthMetrics.value = HealthMetrics()
    }
    
    private fun updateMetrics() {
        val currentMetrics = _healthMetrics.value
        _healthMetrics.value = currentMetrics.copy(
            totalCrashes = crashHistory.size,
            crashRate = getCrashRate(),
            isHealthy = isHealthy()
        )
    }
    
    companion object {
        @Volatile
        private var instance: AppHealthMonitor? = null
        
        fun getInstance(context: Context): AppHealthMonitor {
            return instance ?: synchronized(this) {
                instance ?: AppHealthMonitor(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Métriques de santé de l'application.
 */
data class HealthMetrics(
    val totalCrashes: Int = 0,
    val crashRate: Float = 0f,
    val totalUserInteractions: Int = 0,
    val totalScreenViews: Int = 0,
    val sessionDuration: Long = 0L,
    val currentScreen: String = "",
    val lastInteractionTime: Long = 0L,
    val isHealthy: Boolean = true
)

/**
 * Événement de crash.
 */
data class CrashEvent(
    val timestamp: Long,
    val exceptionType: String,
    val message: String,
    val stackTrace: String,
    val screenName: String? = null
)

/**
 * Métrique de performance.
 */
data class PerformanceMetric(
    val timestamp: Long,
    val operation: String,
    val durationMs: Long,
    val success: Boolean
)

/**
 * Statistiques de performance compilées.
 */
data class PerformanceStats(
    val operation: String,
    val averageDurationMs: Long,
    val minDurationMs: Long,
    val maxDurationMs: Long,
    val successRate: Float,
    val totalCalls: Int
)

/**
 * Extension pour mesurer facilement une opération.
 */
suspend fun <T> AppHealthMonitor.measureOperation(
    operationName: String,
    block: suspend () -> T
): T {
    val startTime = System.currentTimeMillis()
    return try {
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        recordPerformance(operationName, duration, success = true)
        result
    } catch (e: Exception) {
        val duration = System.currentTimeMillis() - startTime
        recordPerformance(operationName, duration, success = false)
        throw e
    }
}
