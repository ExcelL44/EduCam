package com.excell44.educam.data.sync

import android.content.Context
import androidx.work.*
import com.excell44.educam.core.performance.PerformanceManager
import java.util.concurrent.TimeUnit

/**
 * Gestionnaire de synchronisation adaptative qui prend en compte
 * l'état de la batterie et la performance.
 */
class AdaptiveSyncManager(private val context: Context) {
    
    private val performanceManager = PerformanceManager.getInstance(context)
    
    /**
     * Configure les contraintes de synchronisation selon le mode de performance.
     */
    private fun getAdaptiveConstraints(): Constraints {
        val mode = performanceManager.getRecommendedPerformanceMode()
        
        return when (mode) {
            PerformanceManager.PerformanceMode.HIGH -> {
                // Mode haute performance : sync aggressive
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            }
            
            PerformanceManager.PerformanceMode.BALANCED -> {
                // Mode équilibré : sync uniquement sur WiFi
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only
                    .setRequiresBatteryNotLow(true)
                    .build()
            }
            
            PerformanceManager.PerformanceMode.LOW_POWER -> {
                // Mode économie : sync seulement en charge
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresCharging(true) // Only when charging
                    .build()
            }
        }
    }
    
    /**
     * Obtient le délai de backoff selon le mode de performance.
     */
    private fun getAdaptiveBackoffDelay(): Long {
        return when (performanceManager.getRecommendedPerformanceMode()) {
            PerformanceManager.PerformanceMode.HIGH -> 30L // 30 seconds
            PerformanceManager.PerformanceMode.BALANCED -> 60L // 1 minute
            PerformanceManager.PerformanceMode.LOW_POWER -> 300L // 5 minutes
        }
    }
    
    /**
     * Planifie une synchronisation avec contraintes adaptatives.
     */
    fun scheduleAdaptiveSync(
        userId: String,
        immediate: Boolean = false
    ) {
        val constraints = getAdaptiveConstraints()
        val backoffDelay = getAdaptiveBackoffDelay()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(SyncWorker.KEY_USER_ID to userId))
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                backoffDelay,
                TimeUnit.SECONDS
            )
            .apply {
                if (!immediate) {
                    // Délai initial selon le mode
                    val initialDelay = when (performanceManager.getRecommendedPerformanceMode()) {
                        PerformanceManager.PerformanceMode.HIGH -> 0L
                        PerformanceManager.PerformanceMode.BALANCED -> 60L
                        PerformanceManager.PerformanceMode.LOW_POWER -> 300L
                    }
                    setInitialDelay(initialDelay, TimeUnit.SECONDS)
                }
            }
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "adaptive_sync_$userId",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
    }
    
    /**
     * Planifie une synchronisation périodique adaptative.
     */
    fun schedulePeriodicSync(userId: String) {
        // La fréquence dépend du mode de performance
        val interval = when (performanceManager.getRecommendedPerformanceMode()) {
            PerformanceManager.PerformanceMode.HIGH -> 30L // Every 30 minutes
            PerformanceManager.PerformanceMode.BALANCED -> 60L // Every hour
            PerformanceManager.PerformanceMode.LOW_POWER -> 180L // Every 3 hours
        }
        
        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            interval, TimeUnit.MINUTES
        )
            .setConstraints(getAdaptiveConstraints())
            .setInputData(workDataOf(SyncWorker.KEY_USER_ID to userId))
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "periodic_sync_$userId",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
    }
    
    companion object {
        @Volatile
        private var instance: AdaptiveSyncManager? = null
        
        fun getInstance(context: Context): AdaptiveSyncManager {
            return instance ?: synchronized(this) {
                instance ?: AdaptiveSyncManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
