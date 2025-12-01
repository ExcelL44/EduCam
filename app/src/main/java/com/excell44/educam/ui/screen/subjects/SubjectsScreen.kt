@file:OptIn(ExperimentalMaterial3Api::class)

package com.excell44.educam.ui.screen.subjects

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.ui.components.PrimaryButton
import com.excell44.educam.ui.util.screenPadding

// --- Data Models ---

enum class SubjectType(val label: String, val icon: ImageVector, val isActive: Boolean = false) {
    MATH("Mathématiques", Icons.Default.Calculate, true),
    PHYSICS("Physique", Icons.Default.Bolt, true),
    CHEMISTRY("Chimie", Icons.Default.Science, true),
    SVT("SVT", Icons.Default.Biotech, false),
    ENGLISH("Anglais", Icons.Default.Language, false),
    FRENCH("Français", Icons.Default.Book, false)
}

enum class CategoryType(val label: String, val icon: ImageVector) {
    EVALUATION("Évaluations", Icons.Default.Assignment),
    EXAM_BLANC("Examens Blancs", Icons.Default.Description),
    EXAM_OFFICIEL("Examens Officiels", Icons.Default.School)
}

data class BankItem(
    val id: String,
    val label: String, // "2023/2024" ou "Évaluation 1"
    val subjectPdfUrl: String = "",
    val correctionPdfUrl: String = ""
)

// --- Mock Data Generator ---

fun getCategories(): List<CategoryType> = CategoryType.values().toList()

fun getItemsForCategory(category: CategoryType): List<BankItem> {
    return when (category) {
        CategoryType.EVALUATION -> (1..5).map {
            BankItem("eval_$it", "Évaluation N°$it")
        }
        CategoryType.EXAM_BLANC -> (2024 downTo 2019).map {
            BankItem("blanc_$it", "Année $it/${it+1}")
        }
        CategoryType.EXAM_OFFICIEL -> (2019..2025).map { year ->
            BankItem("officiel_mai_$year", "Session de Mai $year")
        }
    }
}

// --- Screen State ---

enum class NavigationLevel {
    SUBJECTS, CATEGORIES, ITEMS, DOCUMENTS
}

@Composable
fun SubjectsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubjectsNavigationViewModel = hiltViewModel()
) {
    val navigationState by viewModel.navigationState.collectAsState()

    // Handle system back button
    BackHandler {
        if (!viewModel.navigateBack()) {
            onNavigateBack()
        }
    }

    // Handle back navigation internally first
    val handleBack = {
        if (!viewModel.navigateBack()) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (navigationState.level) {
                            NavigationLevel.SUBJECTS -> "Banque de Sujets"
                            NavigationLevel.CATEGORIES -> navigationState.selectedSubject?.label ?: "Matière"
                            NavigationLevel.ITEMS -> navigationState.selectedCategory?.label ?: "Catégorie"
                            NavigationLevel.DOCUMENTS -> navigationState.selectedItem?.label ?: "Documents"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .screenPadding()  // ✅ Évite les barres système (status + navigation + clavier)
                .padding(16.dp)
        ) {
            val isWideScreen = maxWidth > 600.dp

            when (navigationState.level) {
                NavigationLevel.SUBJECTS -> {
                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(SubjectType.values()) { subject ->
                                BankCard(
                                    title = subject.label,
                                    icon = subject.icon,
                                    isActive = subject.isActive,
                                    onClick = {
                                        viewModel.navigateToCategories(subject)
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(SubjectType.values()) { subject ->
                                BankCard(
                                    title = subject.label,
                                    icon = subject.icon,
                                    isActive = subject.isActive,
                                    onClick = {
                                        viewModel.navigateToCategories(subject)
                                    }
                                )
                            }
                        }
                    }
                }
                
                NavigationLevel.CATEGORIES -> {
                    Column {
                        Text(
                            text = "Choisissez une catégorie pour ${navigationState.selectedSubject?.label}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (isWideScreen) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(getCategories()) { category ->
                                    BankCard(
                                        title = category.label,
                                        icon = category.icon,
                                        onClick = {
                                            viewModel.navigateToItems(category)
                                        }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(getCategories()) { category ->
                                    BankCard(
                                        title = category.label,
                                        icon = category.icon,
                                        onClick = {
                                            viewModel.navigateToItems(category)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                NavigationLevel.ITEMS -> {
                    Column {
                        Text(
                            text = "${navigationState.selectedCategory?.label} - ${navigationState.selectedSubject?.label}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (isWideScreen) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3), // More dense for years/items
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(viewModel.getCurrentItems()) { item ->
                                    ItemCard(
                                        item = item,
                                        onClick = {
                                            viewModel.navigateToDocuments(item)
                                        }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(viewModel.getCurrentItems()) { item ->
                                    ItemCard(
                                        item = item,
                                        onClick = {
                                            viewModel.navigateToDocuments(item)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                NavigationLevel.DOCUMENTS -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = navigationState.selectedItem?.label ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Document Placeholders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Sujet PDF
                            DocumentPlaceholder(
                                title = "📄 Sujet",
                                modifier = Modifier.weight(1f)
                            )

                            // Corrigé PDF
                            DocumentPlaceholder(
                                title = "📝 Corrigé",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Les fichiers seront disponibles au téléchargement prochainement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BankCard(
    title: String,
    icon: ImageVector,
    isActive: Boolean = true,
    onClick: () -> Unit
) {
    val actualOnClick = if (isActive) onClick else { {} }

    // Utilisation du style PrimaryButton pour la cohérence, mais adapté en Card
    Card(
        onClick = actualOnClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 2.dp else 0.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive)
                MaterialTheme.colorScheme.outlineVariant
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Surface(
                color = if (isActive)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (!isActive) {
                    Text(
                        text = "Bientôt disponible",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            if (isActive) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ItemCard(
    item: BankItem,
    onClick: () -> Unit
) {
    // Style plus compact pour les items (années, evals)
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Ouvrir",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DocumentPlaceholder(
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(0.7f), // Aspect ratio A4 environ
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant) // Dashed border effect simulated
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bientôt disponible",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
