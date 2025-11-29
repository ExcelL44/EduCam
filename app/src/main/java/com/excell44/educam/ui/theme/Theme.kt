package com.excell44.educam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Data class pour définir un thème complet
data class ThemeData(
    val name: String,
    val colors: ColorScheme,
    val darkColors: ColorScheme,
    val description: String
)

// Collection des 7 thèmes pédagogiques optimisés
val BacXThemes = listOf(
    // Theme 0: Focus Clair (THÈME DÉFAUT - Blanc épuré)
    ThemeData(
        name = "Focus Clair",
        colors = lightColorScheme(
            primary = Color(0xFF4A86E8),      // Bleu logiciel éducatif
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF00A896),    // Vert réussite
            tertiary = Color(0xFFFFB940),     // Orange attention doux
            background = Color(0xFFFDFDFD),   // Blanc cassé (pas #FFFFFF pur)
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFE53E3E),
            onBackground = Color(0xFF1A1A1A),
            onSurface = Color(0xFF2D3748)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFF6BA3FF),
            secondary = Color(0xFF4FD1B0),
            tertiary = Color(0xFFF6AD55),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        ),
        description = "Blanc cassé doux pour concentration maximale, couleurs pures pour feedback pédagogique."
    ),

    // Theme 1: Calme Math (Bleu-vert concentration)
    ThemeData(
        name = "Calme Math",
        colors = lightColorScheme(
            primary = Color(0xFF2E7D9B),      // Bleu-vert low saturation
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF5E9FA0),    // Complémentaire bleu-vert
            tertiary = Color(0xFFB8C5D1),     // Gris-bleu pour détails
            background = Color(0xFFF8FBFC),   // Bleu très pâle
            surface = Color(0xFFFCFDFE),
            error = Color(0xFFD67B7B)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFF7BB3C7),
            secondary = Color(0xFF8BBDBE),
            tertiary = Color(0xFFC8D4E0),
            background = Color(0xFF0F1419),
            surface = Color(0xFF1A1F24)
        ),
        description = "Bleu-vert apaisant pour logique et calculs, réduit la fatigue visuelle."
    ),

    // Theme 2: Créatif Français (Violet doux inspiration)
    ThemeData(
        name = "Créatif Français",
        colors = lightColorScheme(
            primary = Color(0xFF6B5B95),      // Violet low saturation (32%)
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF9A86C7),    // Mauve clair
            tertiary = Color(0xFFD4C5E8),     // Lavande très pâle
            background = Color(0xFFFEFBFF),   // Violet-tinged white
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFCC7A7A)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFB0A1D6),
            secondary = Color(0xFFC5B3E0),
            tertiary = Color(0xFFE0D4F0),
            background = Color(0xFF1A141F),
            surface = Color(0xFF251F2A)
        ),
        description = "Violet doux pour stimulation créative sans surcharge émotionnelle."
    ),

    // Theme 3: Énergie Physique (Orange terreux contrôlé)
    ThemeData(
        name = "Énergie Physique",
        colors = lightColorScheme(
            primary = Color(0xFFD2691E),      // Orange terre (34% saturation)
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFFCD853F),    // Tan chaud
            tertiary = Color(0xFFF4E4C1),     // Beige crémeux
            background = Color(0xFFFCF9F5),   // Orange très pâle
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFCC6666)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFE5A86A),
            secondary = Color(0xFFE6C28E),
            tertiary = Color(0xFFF5ECD0),
            background = Color(0xFF1C160F),
            surface = Color(0xFF27211A)
        ),
        description = "Orange terreux pour dynamisme scientifique contrôlé, évite la surcharge."
    ),

    // Theme 4: Nature Bio (Vert sauge sciences)
    ThemeData(
        name = "Nature Bio",
        colors = lightColorScheme(
            primary = Color(0xFF5A936B),      // Vert sauge (35% saturation)
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF7FB069),    // Vert printemps
            tertiary = Color(0xFFC8D5B9),     // Vert très pâle
            background = Color(0xFFF8FBF8),   // Vert-tinged white
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFD67B7B)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFF8BB89C),
            secondary = Color(0xFFA3C68A),
            tertiary = Color(0xFFD8E5C9),
            background = Color(0xFF0F1A12),
            surface = Color(0xFF1A251D)
        ),
        description = "Vert sauge apaisant pour biologie et observation, naturel et reposant."
    ),

    // Theme 5: Focus Soir (Bleu nuit mode sombre soft)
    ThemeData(
        name = "Focus Soir",
        colors = darkColorScheme(               // UNIQUEMENT DARK
            primary = Color(0xFF4A86E8),      // Bleu logiciel (même que défaut)
            onPrimary = Color(0xFFE0E0E0),
            secondary = Color(0xFF5A9B9B),    // Cyan soft
            tertiary = Color(0xFF8B7B8B),     // Gris violet
            background = Color(0xFF121212),   // Noir pur <20% lumière
            surface = Color(0xFF1E1E1E),      // Surface légèrement plus claire
            error = Color(0xFFCF6679),
            onBackground = Color(0xFFE0E0E0),
            onSurface = Color(0xFFB0B0B0)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFF4A86E8),
            secondary = Color(0xFF5A9B9B),
            tertiary = Color(0xFF8B7B8B),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        ),
        description = "Mode sombre bleu nuit, luminosité <40% pour études nocturnes sans fatigue."
    ),

    // Theme 6: Zen Break (Rose pêche détente)
    ThemeData(
        name = "Zen Break",
        colors = lightColorScheme(
            primary = Color(0xFFE6A8A2),      // Rose pêche (38% saturation)
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFFF5C2C1),    // Rose bonbon très pâle
            tertiary = Color(0xFFFCE4EC),     // Rose crémeux
            background = Color(0xFFFEF9F9),   // Rose-tinged white
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFCC7A7A)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFF5B0AA),
            secondary = Color(0xFFF6C5C3),
            tertiary = Color(0xFFFCE4EC),
            background = Color(0xFF1A0F0F),
            surface = Color(0xFF2A1F1F)
        ),
        description = "Rose pêche doux pour moments de pause, détente et réflexion."
    )
)

// Fonction principale du thème BacX
@Composable
fun BacXTheme(
    themeIndex: Int = 0,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val theme = BacXThemes.getOrElse(themeIndex) { BacXThemes[0] }
    val colorScheme = if (darkTheme) theme.darkColors else theme.colors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BacXTypography,
        content = content
    )
}
