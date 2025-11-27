package com.excell44.educam.core.device

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Gestionnaire de capacités de l'appareil.
 * Détecte les caractéristiques matérielles et adapte l'UI en conséquence.
 */
class DeviceCapabilitiesManager(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    
    /**
     * Catégorie de l'appareil selon ses capacités.
     */
    enum class DeviceTier {
        LOW_END,    // < 2GB RAM, processeur faible
        MID_RANGE,  // 2-4GB RAM, processeur moyen
        HIGH_END    // > 4GB RAM, processeur puissant
    }
    
    /**
     * Obtient la catégorie de l'appareil.
     */
    fun getDeviceTier(): DeviceTier {
        val ramMB = getTotalRAM() / (1024 * 1024)
        
        return when {
            ramMB < 2048 -> DeviceTier.LOW_END
            ramMB < 4096 -> DeviceTier.MID_RANGE
            else -> DeviceTier.HIGH_END
        }
    }
    
    /**
     * Obtient la RAM totale en bytes.
     */
    fun getTotalRAM(): Long {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.totalMem
    }
    
    /**
     * Obtient la RAM disponible en bytes.
     */
    fun getAvailableRAM(): Long {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.availMem
    }
    
    /**
     * Vérifie si l'appareil a peu de mémoire.
     */
    fun isLowMemory(): Boolean {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.lowMemory
    }
    
    /**
     * Obtient la taille de l'écran.
     */
    fun getScreenSize(): ScreenSize {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        
        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        val smallestWidth = minOf(widthDp, heightDp)
        
        return when {
            smallestWidth >= 720 -> ScreenSize.XLARGE // Tablet
            smallestWidth >= 600 -> ScreenSize.LARGE  // 7" tablet
            smallestWidth >= 480 -> ScreenSize.NORMAL // Most phones
            else -> ScreenSize.SMALL                   // Small phones
        }
    }
    
    enum class ScreenSize {
        SMALL, NORMAL, LARGE, XLARGE
    }
    
    /**
     * Vérifie si l'appareil est une tablette.
     */
    fun isTablet(): Boolean {
        val screenSize = getScreenSize()
        return screenSize == ScreenSize.LARGE || screenSize == ScreenSize.XLARGE
    }
    
    /**
     * Vérifie si l'appareil est en mode multi-fenêtre.
     */
    fun isInMultiWindowMode(activity: android.app.Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.isInMultiWindowMode
        } else {
            false
        }
    }
    
    /**
     * Obtient l'orientation actuelle.
     */
    fun getOrientation(): Int {
        return context.resources.configuration.orientation
    }
    
    /**
     * Vérifie si l'orientation est portrait.
     */
    fun isPortrait(): Boolean {
        return getOrientation() == Configuration.ORIENTATION_PORTRAIT
    }
    
    /**
     * Recommandations UI selon les capacités de l'appareil.
     */
    fun getUIRecommendations(): UIRecommendations {
        val tier = getDeviceTier()
        val isTablet = isTablet()
        val isLowMem = isLowMemory()
        
        return UIRecommendations(
            enableRichAnimations = tier == DeviceTier.HIGH_END && !isLowMem,
            enableParticleEffects = tier == DeviceTier.HIGH_END,
            maxConcurrentAnimations = when (tier) {
                DeviceTier.HIGH_END -> 6
                DeviceTier.MID_RANGE -> 3
                DeviceTier.LOW_END -> 1
            },
            enableBlur = tier != DeviceTier.LOW_END,
            enableShadows = !isLowMem,
            useCompactLayout = !isTablet && tier == DeviceTier.LOW_END,
            imageCacheSize = when (tier) {
                DeviceTier.HIGH_END -> 100 // MB
                DeviceTier.MID_RANGE -> 50
                DeviceTier.LOW_END -> 20
            }
        )
    }
    
    data class UIRecommendations(
        val enableRichAnimations: Boolean,
        val enableParticleEffects: Boolean,
        val maxConcurrentAnimations: Int,
        val enableBlur: Boolean,
        val enableShadows: Boolean,
        val useCompactLayout: Boolean,
        val imageCacheSize: Int // in MB
    )
    
    companion object {
        @Volatile
        private var instance: DeviceCapabilitiesManager? = null
        
        fun getInstance(context: Context): DeviceCapabilitiesManager {
            return instance ?: synchronized(this) {
                instance ?: DeviceCapabilitiesManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Extension Composable pour accéder aux capacités de l'appareil.
 */
@androidx.compose.runtime.Composable
fun rememberDeviceCapabilities(): DeviceCapabilitiesManager {
    val context = androidx.compose.ui.platform.LocalContext.current
    return androidx.compose.runtime.remember { DeviceCapabilitiesManager.getInstance(context) }
}

/**
 * Extension Composable pour obtenir les recommandations UI.
 */
@androidx.compose.runtime.Composable
fun rememberUIRecommendations(): DeviceCapabilitiesManager.UIRecommendations {
    val capabilities = rememberDeviceCapabilities()
    return androidx.compose.runtime.remember { capabilities.getUIRecommendations() }
}
