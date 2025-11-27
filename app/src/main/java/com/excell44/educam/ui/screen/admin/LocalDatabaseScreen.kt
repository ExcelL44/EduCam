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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDatabaseScreen(
    onNavigateBack: () -> Unit,
    viewModel: LocalDatabaseViewModel = hiltViewModel()
) {
    val dbStats by viewModel.databaseStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Base de Données Locale") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "Gestion de la Base de Données",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Statistiques de la BD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📊 Statistiques",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Divider()
                    StatRow("Utilisateurs", dbStats.userCount.toString())
                    StatRow("Questions de Quiz", dbStats.quizQuestionCount.toString())
                    StatRow("Sujets", dbStats.subjectCount.toString())
                    StatRow("Solutions sauvegardées", dbStats.solutionCount.toString())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Actions disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Actions de gestion
            DatabaseActionCard(
                title = "Exporter les données",
                description = "Créer une sauvegarde de la base de données",
                icon = Icons.Default.CloudDownload,
                onClick = { viewModel.exportDatabase() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            DatabaseActionCard(
                title = "Importer des données",
                description = "Restaurer depuis une sauvegarde",
                icon = Icons.Default.CloudUpload,
                onClick = { viewModel.importDatabase() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            DatabaseActionCard(
                title = "Nettoyer le cache",
                description = "Supprimer les données temporaires",
                icon = Icons.Default.CleaningServices,
                onClick = { viewModel.clearCache() },
                isDestructive = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            DatabaseActionCard(
                title = "Réinitialiser la BD",
                description = "⚠️ Supprimer toutes les données (irreversible)",
                icon = Icons.Default.DeleteForever,
                onClick = { viewModel.resetDatabase() },
                isDestructive = true
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DatabaseActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = if (isDestructive)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ViewModel pour gérer les stats et actions
data class DatabaseStats(
    val userCount: Int = 0,
    val quizQuestionCount: Int = 0,
    val subjectCount: Int = 0,
    val solutionCount: Int = 0
)
