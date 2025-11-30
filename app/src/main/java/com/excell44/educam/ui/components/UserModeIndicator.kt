package com.excell44.educam.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.excell44.educam.data.model.UserMode

@Composable
fun UserModeIndicator(
    userMode: UserMode,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Icône selon le mode
    val icon = when (userMode) {
        UserMode.ADMIN -> Icons.Filled.Star
        UserMode.BETA_T -> Icons.Filled.Verified
        UserMode.ACTIVE -> Icons.Filled.Verified
        UserMode.TRIAL -> Icons.Filled.Star
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Glow effect background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .blur(glowIntensity.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            userMode.glowColor.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        // Main card with enhanced effects
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp,
                pressedElevation = 8.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                userMode.glowColor.copy(alpha = 0.1f),
                                userMode.color.copy(alpha = 0.05f),
                                userMode.glowColor.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                userMode.color.copy(alpha = alpha),
                                userMode.glowColor.copy(alpha = alpha * 0.7f),
                                userMode.color.copy(alpha = alpha)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Animated icon
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = userMode.color.copy(alpha = alpha),
                        modifier = Modifier
                            .size(28.dp)
                            .alpha(alpha)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Animated text
                    Text(
                        text = userMode.label.uppercase(),
                        color = userMode.color,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(alpha)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Second animated icon for emphasis
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = userMode.color.copy(alpha = alpha * 0.8f),
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(alpha * 0.8f)
                    )
                }
            }
        }

        // Sparkle overlay effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.Center)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = alpha * 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )
    }
}
