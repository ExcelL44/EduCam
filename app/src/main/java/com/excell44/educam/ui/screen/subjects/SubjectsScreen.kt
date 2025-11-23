@file:OptIn(ExperimentalMaterial3Api::class)

package com.excell44.educam.ui.screen.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SubjectsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSubjects()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Banque de Sujets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtres
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedSubject == null,
                    onClick = { viewModel.filterBySubject(null) },
                    label = { Text("Tous") }
                )
                FilterChip(
                    selected = uiState.selectedSubject == "Math",
                    onClick = { viewModel.filterBySubject("Math") },
                    label = { Text("Math") }
                )
                FilterChip(
                    selected = uiState.selectedSubject == "Physics",
                    onClick = { viewModel.filterBySubject("Physics") },
                    label = { Text("Physique") }
                )
                FilterChip(
                    selected = uiState.selectedSubject == "Chemistry",
                    onClick = { viewModel.filterBySubject("Chemistry") },
                    label = { Text("Chimie") }
                )
            }

            // Liste des sujets
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.subjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "Aucun sujet disponible",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.subjects) { subject ->
                        SubjectCard(
                            subject = subject,
                            onClick = { viewModel.selectSubject(subject.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialog pour afficher le sujet sélectionné
    uiState.selectedSubjectContent?.let { subject ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSelectedSubject() },
            title = { Text(subject.title) },
            text = {
                Column {
                    Text(
                        text = "Sujet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = subject.content)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Solution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = subject.solution)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSelectedSubject() }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: com.excell44.educam.data.model.Subject,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = subject.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = subject.subject,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = subject.topic,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

