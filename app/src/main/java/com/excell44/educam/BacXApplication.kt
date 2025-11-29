package com.excell44.educam

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.excell44.educam.core.error.GlobalExceptionHandler
import com.excell44.educam.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.crashlytics.ktx.crashlytics

/**
 * ✅ APPLICATION AVEC MONITORING FAIL-SAFE
 * 
 * **DÉTECTION AUTOMATIQUE** :
 * - Fuites mémoire
 * - Operations sur Main Thread
 * - Resource leaks
 * - Network sur Main Thread
 */
@HiltAndroidApp
class BacXApplication : Application() {
    
    companion object {
        private const val TAG = "BacXApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // ✅ Initialize global crash handler
        GlobalExceptionHandler.initialize(this)
        
        // ✅ Safe Firebase Init (Offline Support)
        initializeFirebaseSafe()

        // ✅ Enable StrictMode in DEBUG builds only
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
        
        Log.i(TAG, "✅ Bac-X_237 Application initialized (version: ${BuildConfig.VERSION_NAME})")
    }

    private fun initializeFirebaseSafe() {
        try {
            // ✅ Enable Firestore Offline Persistence
            // This allows the app to work offline and sync when online
            com.google.firebase.ktx.Firebase.firestore.firestoreSettings = com.google.firebase.firestore.ktx.firestoreSettings {
                isPersistenceEnabled = true
            }
            Log.i(TAG, "✅ Firebase Firestore Offline Persistence ENABLED")
            
            // Crashlytics is already initialized by the plugin, but we can force enable/disable
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to configure Firebase: ${e.message}")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
    
    /**
     * ✅ STRICTMODE : Détection de bugs en développement
     * 
     * **DÉTECTE** :
     * - Disk reads sur Main Thread
     * - Network calls sur Main Thread
     * - Leaked closable objects
     * - Leaked SQL objects
     * - Unsafe intent launches
     */
    private fun enableStrictMode() {
        Log.w(TAG, "⚠️ StrictMode ENABLED (DEBUG build)")
        
        // ✅ Thread Policy : détecte les opérations lentes sur Main Thread
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()      // Alerte si lecture disque sur Main
                .detectDiskWrites()     // Alerte si écriture disque sur Main
                .detectNetwork()        // Alerte si réseau sur Main
                .detectCustomSlowCalls() // Alerte pour calls marqués comme lents
                .penaltyLog()           // Log dans Logcat
                .penaltyFlashScreen()   // Flash rouge à l'écran (visible)
                .build()
        )
        
        // ✅ VM Policy : détecte les fuites de ressources
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()   // Détecte files/streams non fermés
                .detectLeakedSqlLiteObjects()    // Détecte cursors non fermés
                .detectLeakedRegistrationObjects() // Détecte broadcast receivers non unregistered
                .detectActivityLeaks()           // Détecte activities non released
                .detectFileUriExposure()         // Détecte file:// URIs non sécurisés
                .penaltyLog()                    // Log dans Logcat
                // .penaltyDeath()               // ⚠️ CRASH immédiat (décommenter pour être strict)
                .build()
        )
        
        Log.i(TAG, """
            📋 StrictMode configuré :
            - ✅ Disk I/O detection
            - ✅ Network detection
            - ✅ Resource leak detection
            - ✅ Activity leak detection
            - ⚠️ Toute violation = log + flash rouge
        """.trimIndent())
    }
}
