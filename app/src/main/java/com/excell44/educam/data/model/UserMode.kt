package com.excell44.educam.data.model

import androidx.compose.ui.graphics.Color

enum class UserMode(
    val label: String,
    val color: Color,
    val description: String
) {
    GUEST(
        label = "Mode Invité",
        color = Color(0xFFFF00FF), // Rose
        description = "Accès limité (3 essais)"
    ),
    PASSIVE(
        label = "Mode Passif",
        color = Color(0xFFFFFF00), // Gris-Jaune (Yellow for visibility)
        description = "Période d'essai (7 jours)"
    ),
    ACTIVE(
        label = "Mode Actif",
        color = Color(0xFF800080), // Violet
        description = "Accès complet"
    ),
    BETA_T(
        label = "Mode Beta Testeur",
        color = Color(0xFF0000FF), // Bleu
        description = "Beta Testeur"
    ),
    ADMIN(
        label = "Mode Admin",
        color = Color(0xFFFF0000), // Rouge
        description = "Super User"
    )
}
