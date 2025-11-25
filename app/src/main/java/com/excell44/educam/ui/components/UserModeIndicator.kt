package com.excell44.educam.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.excell44.educam.data.model.UserMode

@Composable
fun UserModeIndicator(
    userMode: UserMode,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = userMode.color,
                ambientColor = userMode.color
            )
            .border(
                width = 2.dp,
                color = userMode.color.copy(alpha = alpha),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = userMode.label.uppercase(),
            color = userMode.color,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(alpha)
        )
    }
}
