package com.excell44.educam.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Un bouton qui affiche un indicateur de chargement et se désactive pendant le traitement.
 * Utilise DebouncedButton en interne.
 */
@Composable
fun LoadingButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String? = null,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    DebouncedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            if (text != null || content != null) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        if (content != null) {
            content()
        } else if (text != null) {
            Text(text = text)
        }
    }
}
