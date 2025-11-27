package com.excell44.educam.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.excell44.educam.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker simple pour synchroniser les données locales avec le serveur.
 * Implémentation minimale sans dépendances Hilt pour éviter la complexité.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Récupérer les paramètres
            val userId = inputData.getString(KEY_USER_ID) ?: return@withContext Result.failure()
            
            // TODO: Implémenter la logique de sync réelle quand le backend sera prêt
            // 1. Récupérer les données locales modifiées (syncStatus = PENDING_*)
            // 2. Envoyer au serveur
            // 3. Mettre à jour le statut local
            // 4. Récupérer les nouvelles données du serveur
            
            // Pour l'instant, on retourne succès
            Result.success(
                workDataOf(
                    KEY_SYNC_STATUS to "SUCCESS",
                    KEY_SYNCED_AT to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // En cas d'erreur, WorkManager retentera selon la retry policy
            Result.retry()
        }
    }

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_SYNC_STATUS = "sync_status"
        const val KEY_SYNCED_AT = "synced_at"
        const val WORK_NAME = "sync_work"
    }
}
