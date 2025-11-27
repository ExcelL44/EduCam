package com.excell44.educam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Banner affiché en haut de l'écran quand le Safe Mode est actif.
 */
@Composable
fun SafeModeBanner(
    isVisible: Boolean,
    onDeactivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFF9800) // Orange warning
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Safe Mode",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "Mode Sans Échec Activé",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Fonctionnalités réduites pour stabilité",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                TextButton(
                    onClick = onDeactivate,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Désactiver")
                }
            }
        }
    }
}

/**
 * Dialog complet pour mode sans échec avec explications.
 */
@Composable
fun SafeModeDialog(
    isVisible: Boolean,
    crashCount: Int,
    onContinueSafeMode: () -> Unit,
    onTryNormalMode: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Mode Sans Échec Activé")
            },
            text = {
                Column {
                    Text(
                        "L'application a crashé $crashCount fois récemment. " +
                        "Pour votre sécurité, le mode sans échec a été activé.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "En mode sans échec :",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val features = listOf(
                        "• Animations désactivées",
                        "• Synchronisation désactivée",
                        "• Cache désactivé",
                        "• Fonctionnalités avancées désactivées",
                        "• Mode hors ligne forcé"
                    )
                    
                    features.forEach { feature ->
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Le mode sans échec se désactivera automatiquement " +
                        "après 30 minutes sans crash.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = onContinueSafeMode) {
                    Text("Continuer en mode sans échec")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onTryNormalMode) {
                    Text("Essayer le mode normal")
                }
            }
        )
    }
}
