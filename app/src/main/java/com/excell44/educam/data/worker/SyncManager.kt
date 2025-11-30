package com.excell44.educam.data.worker

import android.content.Context
import androidx.work.*
import com.excell44.educam.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages sync operations for the app.
 * Schedules periodic sync of PENDING_CREATE users to Firebase.
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val SYNC_WORK_NAME = "user_sync_work"
        private const val SYNC_INTERVAL_HOURS = 6L // Sync every 6 hours
    }
    
    /**
     * Schedule periodic sync worker (runs when network available).
     */
    fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<UserSyncWorker>(
            SYNC_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        
        Logger.i("SyncManager", "Periodic sync worker scheduled (every ${SYNC_INTERVAL_HOURS}h)")
    }
    
    /**
     * Trigger immediate one-time sync (e.g., after registration).
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<UserSyncWorker>()
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueue(syncRequest)
        Logger.d("SyncManager", "Immediate sync triggered")
    }
    
    /**
     * Cancel all sync work (for testing or debugging).
     */
    fun cancelSync() {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Logger.w("SyncManager", "Sync worker cancelled")
    }
}
