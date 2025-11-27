package com.excell44.educam.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Moniteur de l'impact batterie de l'application.
 * Mesure la consommation et propose des optimisations.
 */
class BatteryImpactMonitor(private val context: Context) {
    
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    
    private val _batteryImpact = MutableStateFlow(BatteryImpact())
    val batteryImpact: StateFlow<BatteryImpact> = _batteryImpact.asStateFlow()
    
    private var sessionStartBatteryLevel = 0
    private var sessionStartTime = 0L
    
    /**
     * Démarre le monitoring de l'impact batterie.
     */
    fun startMonitoring() {
        sessionStartBatteryLevel = getCurrentBatteryLevel()
        sessionStartTime = System.currentTimeMillis()
        updateImpact()
    }
    
    /**
     * Met à jour les métriques d'impact.
     */
    fun updateImpact() {
        val currentLevel = getCurrentBatteryLevel()
        val sessionDurationHours = (System.currentTimeMillis() - sessionStartTime) / (1000f * 60f * 60f)
        
        val batteryDrain = maxOf(0, sessionStartBatteryLevel - currentLevel)
        val drainPerHour = if (sessionDurationHours > 0) batteryDrain / sessionDurationHours else 0f
        
        _batteryImpact.value = BatteryImpact(
            sessionBatteryDrain = batteryDrain,
            drainPerHour = drainPerHour,
            sessionDurationMinutes = ((System.currentTimeMillis() - sessionStartTime) / (1000 * 60)).toInt(),
            currentBatteryLevel = currentLevel,
            isCharging = isCharging(),
            batteryHealth = getBatteryHealth(),
            impactLevel = calculateImpactLevel(drainPerHour)
        )
    }
    
    /**
     * Obtient le niveau de batterie actuel.
     */
    private fun getCurrentBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    /**
     * Vérifie si l'appareil est en charge.
     */
    private fun isCharging(): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
               status == BatteryManager.BATTERY_STATUS_FULL
    }
    
    /**
     * Obtient la santé de la batterie.
     */
    private fun getBatteryHealth(): BatteryHealth {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        
        return when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }
    }
    
    /**
     * Calcule le niveau d'impact basé sur le drain par heure.
     */
    private fun calculateImpactLevel(drainPerHour: Float): ImpactLevel {
        return when {
            drainPerHour < 5f -> ImpactLevel.LOW      // < 5% par heure = excellent
            drainPerHour < 10f -> ImpactLevel.MEDIUM  // 5-10% par heure = normal
            drainPerHour < 20f -> ImpactLevel.HIGH    // 10-20% par heure = élevé
            else -> ImpactLevel.CRITICAL               // > 20% par heure = critique
        }
    }
    
    /**
     * Obtient des recommandations d'optimisation.
     */
    fun getOptimizationRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val impact = _batteryImpact.value
        
        when (impact.impactLevel) {
            ImpactLevel.CRITICAL -> {
                recommendations.add("⚠️ Consommation critique ! Activer le mode économie d'énergie")
                recommendations.add("Désactiver les animations")
                recommendations.add("Réduire la synchronisation en arrière-plan")
                recommendations.add("Fermer les fonctionnalités non essentielles")
            }
            ImpactLevel.HIGH -> {
                recommendations.add("⚠️ Consommation élevée")
                recommendations.add("Réduire la fréquence de synchronisation")
                recommendations.add("Limiter les animations")
            }
            ImpactLevel.MEDIUM -> {
                recommendations.add("✅ Consommation normale")
                recommendations.add("Envisager le mode économie si batterie < 30%")
            }
            ImpactLevel.LOW -> {
                recommendations.add("✅ Consommation optimale")
            }
        }
        
        if (impact.batteryHealth != BatteryHealth.GOOD) {
            recommendations.add("⚠️ Santé batterie: ${impact.batteryHealth}")
        }
        
        return recommendations
    }
    
    /**
     * Reset le monitoring.
     */
    fun reset() {
        sessionStartBatteryLevel = getCurrentBatteryLevel()
        sessionStartTime = System.currentTimeMillis()
        _batteryImpact.value = BatteryImpact()
    }
    
    companion object {
        @Volatile
        private var instance: BatteryImpactMonitor? = null
        
        fun getInstance(context: Context): BatteryImpactMonitor {
            return instance ?: synchronized(this) {
                instance ?: BatteryImpactMonitor(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Données d'impact batterie.
 */
data class BatteryImpact(
    val sessionBatteryDrain: Int = 0,           // Drain total de la session (%)
    val drainPerHour: Float = 0f,               // Drain par heure (%)
    val sessionDurationMinutes: Int = 0,        // Durée session (min)
    val currentBatteryLevel: Int = 100,         // Niveau actuel (%)
    val isCharging: Boolean = false,            // En charge
    val batteryHealth: BatteryHealth = BatteryHealth.GOOD,
    val impactLevel: ImpactLevel = ImpactLevel.LOW
)

enum class BatteryHealth {
    GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, COLD, UNKNOWN
}

enum class ImpactLevel {
    LOW,      // < 5% par heure
    MEDIUM,   // 5-10% par heure
    HIGH,     // 10-20% par heure
    CRITICAL  // > 20% par heure
}

/**
 * Extension Composable pour monitorer l'impact batterie.
 */
@androidx.compose.runtime.Composable
fun rememberBatteryImpact(context: android.content.Context): androidx.compose.runtime.State<BatteryImpact> {
    val monitor = androidx.compose.runtime.remember { BatteryImpactMonitor.getInstance(context) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        monitor.startMonitoring()
    }
    
    return monitor.batteryImpact.collectAsState()
}
