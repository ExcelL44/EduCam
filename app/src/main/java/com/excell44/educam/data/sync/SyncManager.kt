package com.excell44.educam.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Gestionnaire de synchronisation utilisant WorkManager.
 * Simplifié pour éviter les dépendances complexes.
 */
class SyncManager(private val context: Context) {

    /**
     * Lance une synchronisation unique pour un utilisateur.
     * 
     * @param userId L'identifiant de l'utilisateur à synchroniser
     * @param forceSync Force la sync même si une est déjà en cours
     */
    fun scheduleSyncForUser(userId: String, forceSync: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Nécessite une connexion
            .setRequiresBatteryNotLow(true) // Ne pas vider la batterie
            .build()

        val inputData = workDataOf(SyncWorker.KEY_USER_ID to userId)

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            // Retry policy: backoff exponentiel
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, // 30 secondes initial
                TimeUnit.SECONDS
            )
            .build()

        val existingWorkPolicy = if (forceSync) {
            ExistingWorkPolicy.REPLACE
        } else {
            ExistingWorkPolicy.KEEP // Garde le travail existant si déjà en cours
        }

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SyncWorker.WORK_NAME,
                existingWorkPolicy,
                syncRequest
            )
    }

    /**
     * Annule toutes les synchronisations en attente.
     */
    fun cancelAllSync() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(SyncWorker.WORK_NAME)
    }

    /**
     * Récupère le statut de la synchronisation en cours.
     */
    fun getSyncStatus() = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)

    companion object {
        @Volatile
        private var instance: SyncManager? = null

        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
