@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.excell44.educam.ui.screen.problemsolver

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun ProblemSolverScreen(
    onNavigateBack: () -> Unit,
    navigationViewModel: com.excell44.educam.ui.navigation.NavigationViewModel,
    viewModel: ProblemSolverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smarty IA") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Prenez une photo ou sélectionnez un PDF",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Smarty IA résout vos exercices : prenez une photo, et obtenez la solution en un clin d'œil",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton pour prendre une photo
            com.excell44.educam.ui.components.PrimaryButton(
                onClick = {
                    if (permissionsState.allPermissionsGranted) {
                        viewModel.captureImage()
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                text = "Prendre une photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )

            // Bouton pour sélectionner un PDF
            com.excell44.educam.ui.components.PrimaryButton(
                onClick = { viewModel.selectPdf() },
                text = "Sélectionner un PDF",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )

            // Nouveau bouton pour discuter avec Smarty
            com.excell44.educam.ui.components.SecondaryButton(
                onClick = {
                    navigationViewModel.navigate(com.excell44.educam.ui.navigation.NavCommand.NavigateTo(com.excell44.educam.ui.navigation.Screen.Chat.route))
                },
                text = "💬 Discuter avec Smarty",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )

            // Affichage de l'image sélectionnée
            uiState.imageUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    // Ici on pourrait utiliser Coil pour afficher l'image
                    Text(
                        text = "Image sélectionnée: ${uri.toString()}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Solution
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator()
                Text("Analyse en cours...")
            }

            uiState.solution?.let { solution ->
                Spacer(modifier = Modifier.height(32.dp))
                SolutionCard(solution = solution)
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SolutionCard(solution: com.excell44.educam.data.model.ProblemSolution) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Solution",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = solution.solution,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Étapes de résolution:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            solution.steps.forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
