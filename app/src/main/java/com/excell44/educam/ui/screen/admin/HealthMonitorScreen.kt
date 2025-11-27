package com.excell44.educam.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.excell44.educam.core.monitoring.AppHealthMonitor
import com.excell44.educam.ui.util.screenPadding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran de monitoring de la santé de l'application.
 * Affiche les métriques en temps réel, crashs, et performances.
 */
@Composable
fun HealthMonitorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val healthMonitor = remember { AppHealthMonitor.getInstance(context) }
    val healthMetrics by healthMonitor.healthMetrics.collectAsState()
    
    val crashes = remember { healthMonitor.getRecentCrashes(10) }
    val perfStats = remember { healthMonitor.getAllPerformanceStats() }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .screenPadding()
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Health Monitor") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Status Card
            item {
                HealthStatusCard(healthMetrics)
            }
            
            // Session Info Card
            item {
                SessionInfoCard(healthMetrics)
            }
            
            // Performance Stats
            item {
                Text(
                    "Performance",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(perfStats) { stat ->
                PerformanceStatCard(stat)
            }
            
            // Recent Crashes
            item {
                Text(
                    "Recent Crashes (${crashes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (crashes.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("No crashes recorded! 🎉")
                        }
                    }
                }
            } else {
                items(crashes) { crash ->
                    CrashEventCard(crash)
                }
            }
        }
    }
}

@Composable
private fun HealthStatusCard(metrics: com.excell44.educam.core.monitoring.HealthMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (metrics.isHealthy) 
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else 
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "App Health",
                    style = MaterialTheme.typography.titleLarge
                )
                Icon(
                    if (metrics.isHealthy) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (metrics.isHealthy) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MetricRow("Status", if (metrics.isHealthy) "Healthy ✅" else "Unhealthy ⚠️")
            MetricRow("Total Crashes", metrics.totalCrashes.toString())
            MetricRow("Crash Rate", String.format("%.2f per hour", metrics.crashRate))
        }
    }
}

@Composable
private fun SessionInfoCard(metrics: com.excell44.educam.core.monitoring.HealthMetrics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Session Info",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val hours = (metrics.sessionDuration / (1000 * 60 * 60)) % 24
            val minutes = (metrics.sessionDuration / (1000 * 60)) % 60
            val seconds = (metrics.sessionDuration / 1000) % 60
            
            MetricRow("Session Duration", String.format("%02d:%02d:%02d", hours, minutes, seconds))
            MetricRow("User Interactions", metrics.totalUserInteractions.toString())
            MetricRow("Screen Views", metrics.totalScreenViews.toString())
            MetricRow("Current Screen", metrics.currentScreen.ifEmpty { "N/A" })
        }
    }
}

@Composable
private fun PerformanceStatCard(stat: com.excell44.educam.core.monitoring.PerformanceStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                stat.operation,
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MetricRow("Average", "${stat.averageDurationMs}ms")
            MetricRow("Min/Max", "${stat.minDurationMs}ms / ${stat.maxDurationMs}ms")
            MetricRow("Success Rate", String.format("%.1f%%", stat.successRate))
            MetricRow("Total Calls", stat.totalCalls.toString())
        }
    }
}

@Composable
private fun CrashEventCard(crash: com.excell44.educam.core.monitoring.CrashEvent) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    crash.exceptionType,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    dateFormat.format(Date(crash.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                crash.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            crash.screenName?.let { screen ->
                Text(
                    "Screen: $screen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
