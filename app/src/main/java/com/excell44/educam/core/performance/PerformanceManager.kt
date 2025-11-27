package com.excell44.educam.core.performance

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi

/**
 * Gestionnaire de performance adaptatif basé sur l'état de la batterie et la température.
 * Permet d'adapter automatiquement les performances de l'app selon le contexte.
 */
class PerformanceManager(private val context: Context) {
    
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    
    /**
     * Mode de performance actuel recommandé.
     */
    enum class PerformanceMode {
        HIGH,      // Batterie > 50%, température OK
        BALANCED,  // Batterie 20-50% ou température moyenne
        LOW_POWER  // Batterie < 20%, mode économie, ou surchauffe
    }
    
    /**
     * Obtient le niveau de batterie actuel (0-100).
     */
    fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    /**
     * Vérifie si le mode économie d'énergie est activé.
     */
    fun isPowerSaveMode(): Boolean {
        return powerManager.isPowerSaveMode
    }
    
    /**
     * Vérifie si l'appareil est en charge.
     */
    fun isCharging(): Boolean {
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
               status == BatteryManager.BATTERY_STATUS_FULL
    }
    
    /**
     * Obtient la température de la batterie en Celsius.
     */
    fun getBatteryTemperature(): Float {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temperature / 10f  // Temperature is in tenths of degree
    }
    
    /**
     * Obtient le thermal headroom (marge thermique disponible).
     * Valeur entre 0.0 (surchauffe) et 1.0 (température idéale).
     * Retourne null si l'API n'est pas disponible.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun getThermalHeadroom(forecastSeconds: Int = 30): Float? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                powerManager.getThermalHeadroom(forecastSeconds)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Recommande un mode de performance basé sur tous les facteurs.
     */
    fun getRecommendedPerformanceMode(): PerformanceMode {
        val batteryLevel = getBatteryLevel()
        val temperature = getBatteryTemperature()
        val isPowerSave = isPowerSaveMode()
        val isCharging = isCharging()
        
        // Mode économie forcé
        if (isPowerSave) {
            return PerformanceMode.LOW_POWER
        }
        
        // Surchauffe (> 40°C)
        if (temperature > 40f) {
            return PerformanceMode.LOW_POWER
        }
        
        // Batterie critique (< 15%)
        if (batteryLevel < 15) {
            return PerformanceMode.LOW_POWER
        }
        
        // Batterie faible (< 30%) et pas en charge
        if (batteryLevel < 30 && !isCharging) {
            return PerformanceMode.BALANCED
        }
        
        // Température élevée (> 35°C)
        if (temperature > 35f) {
            return PerformanceMode.BALANCED
        }
        
        // Sinon, mode haute performance
        return PerformanceMode.HIGH
    }
    
    /**
     * Obtient un multiplicateur de performance (0.0 à 1.0).
     * Utilisé pour ajuster les animations, les fréquences de mise à jour, etc.
     */
    fun getPerformanceMultiplier(): Float {
        return when (getRecommendedPerformanceMode()) {
            PerformanceMode.HIGH -> 1.0f
            PerformanceMode.BALANCED -> 0.7f
            PerformanceMode.LOW_POWER -> 0.5f
        }
    }
    
    companion object {
        @Volatile
        private var instance: PerformanceManager? = null
        
        fun getInstance(context: Context): PerformanceManager {
            return instance ?: synchronized(this) {
                instance ?: PerformanceManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Classe de configuration pour adapter les fonctionnalités selon le mode de performance.
 */
data class PerformanceConfig(
    val mode: PerformanceManager.PerformanceMode,
    val animationDurationMultiplier: Float = when(mode) {
        PerformanceManager.PerformanceMode.HIGH -> 1.0f
        PerformanceManager.PerformanceMode.BALANCED -> 0.8f
        PerformanceManager.PerformanceMode.LOW_POWER -> 0.6f
    },
    val backgroundSyncEnabled: Boolean = mode != PerformanceManager.PerformanceMode.LOW_POWER,
    val cacheWarmingEnabled: Boolean = mode == PerformanceManager.PerformanceMode.HIGH,
    val maxConcurrentOperations: Int = when(mode) {
        PerformanceManager.PerformanceMode.HIGH -> 4
        PerformanceManager.PerformanceMode.BALANCED -> 2
        PerformanceManager.PerformanceMode.LOW_POWER -> 1
    }
)
