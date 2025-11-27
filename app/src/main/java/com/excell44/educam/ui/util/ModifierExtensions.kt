package com.excell44.educam.ui.util

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier

/**
 * Extension pour ajouter tous les paddings nécessaires pour les system bars.
 * Combine systemBarsPadding (status bar + navigation bar) avec imePadding (clavier).
 * 
 * À utiliser sur le Modifier racine de tous les écrans pour éviter que le contenu
 * soit caché par les barres système ou le clavier.
 * 
 * Exemple:
 * ```
 * Column(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .screenPadding()
 *         .padding(16.dp)
 * )
 * ```
 */
fun Modifier.screenPadding(): Modifier {
    return this
        .systemBarsPadding()  // Évite status bar + navigation bar
        .imePadding()         // Évite le clavier
}
