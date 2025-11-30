package com.excell44.educam.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.excell44.educam.core.network.NetworkObserver
import com.excell44.educam.data.dao.UserDao
import com.excell44.educam.util.Logger
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

/**
 * Background worker to sync PENDING_CREATE users to Firebase.
 * Runs when network is available to convert PASSIVE -> ACTIVE accounts.
 */
@HiltWorker
class UserSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val networkObserver: NetworkObserver
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            Logger.i("UserSyncWorker", "Starting user sync check...")
            
            // Check if online
            if (!networkObserver.isOnline()) {
                Logger.w("UserSyncWorker", "Device offline, skipping sync")
                return Result.retry()
            }
            
            // Get all pending users (expire time check included)
            val twentyFourHoursAgo = System.currentTimeMillis() - (24L * 60 * 60 * 1000)
            val pendingUsers = userDao.getExpiredUnsyncedUsers(0L) // All unsynced users
            
            if (pendingUsers.isEmpty()) {
                Logger.d("UserSyncWorker", "No pending users to sync")
                return Result.success()
            }
            
            Logger.i("UserSyncWorker", "Found ${pendingUsers.size} pending user(s) to sync")
            
            var syncedCount = 0
            var failedCount = 0
            
            for (user in pendingUsers) {
                try {
                    // Upload user metadata to Firestore (no private data)
                    val userMetadata = hashMapOf(
                        "id" to user.id,
                        "email" to user.email,
                        "name" to user.name,
                        "gradeLevel" to user.gradeLevel,
                        "createdAt" to user.createdAt,
                        "role" to "ACTIVE", // Promote to ACTIVE after sync
                        "syncedAt" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("users")
                        .document(user.id)
                        .set(userMetadata)
                        .await()
                    
                    // Update local user to ACTIVE and SYNCED
                    val updatedUser = user.copy(
                        role = "ACTIVE",
                        syncStatus = "SYNCED",
                        lastSyncTimestamp = System.currentTimeMillis(),
                        trialExpiresAt = null // Remove trial expiry
                    )
                    userDao.insertUser(updatedUser)
                    
                    Logger.i("UserSyncWorker", "Synced user: ${user.email} (${user.id})")
                    syncedCount++
                    
                } catch (e: Exception) {
                    Logger.e("UserSyncWorker", "Failed to sync user ${user.email}", e)
                    failedCount++
                }
            }
            
            Logger.i("UserSyncWorker", "Sync complete: $syncedCount synced, $failedCount failed")
            
            return if (failedCount == 0) {
                Result.success()
            } else {
                Result.retry() // Retry later if some failed
            }
            
        } catch (e: Exception) {
            Logger.e("UserSyncWorker", "Sync worker error", e)
            return Result.retry()
        }
    }
}
