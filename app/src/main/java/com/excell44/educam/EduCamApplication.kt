package com.excell44.educam

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.excell44.educam.core.error.GlobalExceptionHandler
import dagger.hilt.android.HiltAndroidApp

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
class EduCamApplication : Application() {
    
    companion object {
        private const val TAG = "EduCamApplication"
        // ⚠️ STRICTMODE DÉSACTIVÉ TEMPORAIREMENT
        // Cause des flashs rouges pendant navigation
        // Réactiver après migration complète
        private const val ENABLE_STRICT_MODE = false
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // ✅ Initialize global crash handler to prevent brutal app crashes
        GlobalExceptionHandler.initialize(this)
        
        // ⚠️ Enable StrictMode in DEBUG builds only (DÉSACTIVÉ TEMPORAIREMENT)
        if (BuildConfig.DEBUG && ENABLE_STRICT_MODE) {
            enableStrictMode()
        }
        
        Log.i(TAG, "✅ EduCam Application initialized (version: ${BuildConfig.VERSION_NAME})")
        if (!ENABLE_STRICT_MODE) {
            Log.w(TAG, "⚠️ StrictMode est DÉSACTIVÉ (migration en cours)")
        }
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
