package com.excell44.educam.data.model

import androidx.compose.ui.graphics.Color

enum class UserMode(
    val label: String,
    val color: Color,
    val glowColor: Color,
    val description: String
) {
    TRIAL(
        label = "Mode Passif",
        color = Color(0xFFFFD700), // Gold vibrant (plus jaune/or)
        glowColor = Color(0xFFFFF8DC), // Light gold glow
        description = "Période d'essai (7 jours)"
    ),
    ACTIVE(
        label = "Mode Actif",
        color = Color(0xFF9C27B0), // Purple vibrant (plus violet)
        glowColor = Color(0xFFE1BEE7), // Light purple glow
        description = "Accès complet"
    ),
    BETA_T(
        label = "Mode Beta Testeur",
        color = Color(0xFF2196F3), // Blue vibrant (plus bleu)
        glowColor = Color(0xFFBBDEFB), // Light blue glow
        description = "Beta Testeur"
    ),
    ADMIN(
        label = "Mode Admin",
        color = Color(0xFFF44336), // Red vibrant (plus rouge)
        glowColor = Color(0xFFFFCDD2), // Light red glow
        description = "Super User"
    )
}
