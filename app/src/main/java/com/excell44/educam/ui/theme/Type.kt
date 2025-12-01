package com.excell44.educam.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.excell44.educam.R

// 🎨 POLICE DE BRANDING - Exo 2 (futuriste, moderne, professionnel)
// ⚠️ UTILISÉE UNIQUEMENT pour le label "Bac-X_237" dans les écrans
// Cette police sert EXCLUSIVEMENT pour l'identité visuelle de la marque
val BacXBrandFont = FontFamily(
    Font(
        resId = R.font.exo2_variablefont_wght,
        weight = FontWeight.SemiBold,
        style = FontStyle.Normal
    )
)

// 📖 POLICE PRINCIPALE - Inter (optimisée pour lecture intensive sur écran)
// Utilisée pour TOUT le contenu de l'application (textes, boutons, titres)
// 
// ✅ AVANTAGES de Inter pour la lecture longue durée :
// - Conçue spécifiquement pour les interfaces numériques
// - Grande hauteur d'x (meilleure lisibilité à petite taille)
// - Espacements optimaux pour réduire la fatigue oculaire
// - Distingue clairement les caractères similaires (I, l, 1, O, 0)
// - Meilleure performance à 14-16sp (taille de lecture standard)
// - Rendu exceptionnel sur écrans LCD/OLED
val BacXFontFamily = FontFamily(
    // Police variable Inter - Normal
    Font(
        resId = R.font.inter_variablefont_opsz_wght,
        weight = FontWeight.Light,
        style = FontStyle.Normal
    ),
    Font(
        resId = R.font.inter_variablefont_opsz_wght,
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    ),
    Font(
        resId = R.font.inter_variablefont_opsz_wght,
        weight = FontWeight.Medium,
        style = FontStyle.Normal
    ),
    Font(
        resId = R.font.inter_variablefont_opsz_wght,
        weight = FontWeight.SemiBold,
        style = FontStyle.Normal
    ),
    Font(
        resId = R.font.inter_variablefont_opsz_wght,
        weight = FontWeight.Bold,
        style = FontStyle.Normal
    ),
    // Police variable Inter - Italic
    Font(
        resId = R.font.inter_italic_variablefont_opsz_wght,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.inter_italic_variablefont_opsz_wght,
        weight = FontWeight.Medium,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.inter_italic_variablefont_opsz_wght,
        weight = FontWeight.SemiBold,
        style = FontStyle.Italic
    )
)

// 📐 Configuration typographique optimisée pour l'éducation
// Basée sur les recommandations Material Design 3 + ajustements pour lecture intensive
val BacXTypography = Typography(

    // Titres principaux - Lisibilité maximale
    displayLarge = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp // Inter bénéficie d'un léger tracking négatif aux grandes tailles
    ),
    displayMedium = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Titres de section
    headlineLarge = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Titres dans les composants
    titleLarge = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // 📖 Corps de texte - OPTIMISÉ POUR LECTURE INTENSIVE
    // Ces styles sont les plus utilisés et méritent le plus d'attention
    bodyLarge = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // Taille optimale pour lecture confortable
        lineHeight = 26.sp, // 1.625 ratio (idéal pour lecture longue)
        letterSpacing = 0.5.sp // Inter nécessite un peu plus d'espacement
    ),
    bodyMedium = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp, // 1.57 ratio (bon compromis)
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp, // 1.5 ratio minimum pour lisibilité
        letterSpacing = 0.4.sp
    ),

    // Étiquettes et boutons
    labelLarge = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BacXFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
