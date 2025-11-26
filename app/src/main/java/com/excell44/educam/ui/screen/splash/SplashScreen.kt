package com.excell44.educam.ui.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

@Composable
fun SplashScreen(
    postSplashDestination: String,
    onNavigate: (String) -> Unit,
    totalDurationMs: Int = 4500
) {
    // Brilliance-inspired particle system with mathematical elegance
    val particles = remember { List(24) { Particle.random() } }
    val particleAnimations = remember { particles.map { Animatable(0f) } }

    // Video game-like character progression
    var currentXp by remember { mutableStateOf(0f) }
    val maxXp = 100f
    val xpAnim = remember { Animatable(0f) }

    // Geometric transformations
    val logoScale = remember { Animatable(0.1f) }
    val logoRotate = remember { Animatable(-180f) }
    val glowAlpha = remember { Animatable(0f) }

    // Wave animation for premium feel
    val waveAnim = remember { Animatable(0f) }

    // Nexus-style portal effect
    val portalAlpha = remember { Animatable(0f) }
    val portalScale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        // Stage 1: Portal emergence (Khan Academy brilliant-style)
        launch {
            delay(200)
            portalAlpha.animateTo(0.3f, animationSpec = tween(800, easing = FastOutSlowInEasing))
            portalScale.animateTo(1.2f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        }

        // Stage 2: XP loading simulation
        launch {
            for (i in 0..100 step 2) {
                currentXp = i.toFloat()
                xpAnim.animateTo(i.toFloat() / maxXp, animationSpec = tween(50))
                delay(30)
            }
        }

        // Stage 3: Particle constellation formation
        launch {
            delay(500)
            for (i in particleAnimations.indices) {
                launch {
                    delay((i * 50).toLong())
                    particleAnimations[i].animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
                }
            }
        }

        // Stage 4: Logo revelation with dramatic flair
        launch {
            delay(800)
            logoScale.animateTo(1.5f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
            logoRotate.animateTo(0f, animationSpec = tween(800, easing = FastOutSlowInEasing))
            glowAlpha.animateTo(0.8f, animationSpec = tween(400))

            logoScale.animateTo(1.0f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ))
        }

        // Stage 5: Brilliant wave completion
        launch {
            delay(1800)
            waveAnim.animateTo(360f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
        }

        delay(totalDurationMs.toLong())
        onNavigate(postSplashDestination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f),
                        Color.Black
                    ),
                    center = androidx.compose.ui.geometry.Offset(0f, 0f),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Brilliant particle constellation (like educational portals)
        particles.forEachIndexed { index, particle ->
            val progress = particleAnimations[index].value
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .offset(
                            DpOffset(
                                x = (particle.startX * (1 - progress) + particle.targetX * progress).dp,
                                y = (particle.startY * (1 - progress) + particle.targetY * progress).dp
                            )
