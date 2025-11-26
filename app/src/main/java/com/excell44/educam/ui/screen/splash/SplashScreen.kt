package com.excell44.educam.ui.screen.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    postSplashDestination: String,
    onNavigate: (String) -> Unit,
    totalDurationMs: Int = 4000 // Extended for complex animation
) {
    // Animation states
    val animationProgress = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.2f) }
    val logoRotation = remember { Animatable(0f) }
    val promotionAlpha = remember { Animatable(0f) }
    val particleCount = 150

    // Data class for type safety
    data class Particle(
        val x: Animatable<Float, AnimationVector1D>,
        val y: Animatable<Float, AnimationVector1D>,
        val size: Float,
        val color: Int,
        val phase: Float
    )

    // Particle positions and colors for whirlpool effect
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Animatable(0f),
                y = Animatable(0f),
                size = Random.nextFloat() * 6f + 2f,
                color = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt()),
                phase = Random.nextFloat() * 2 * PI.toFloat()
            )
        }
    }

    // Main animation sequence
    LaunchedEffect(Unit) {
        // Phase 1: Whirlpool formation (0-1500ms)
        animationProgress.animateTo(
            targetValue = 0.4f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )

        // Animate particles in circular motion
        particles.forEachIndexed { index, particle ->
            val angle = (index * 360f / particleCount + particle.phase * 57.3f) % 360f
            val radius = 80f + index * 0.5f

            launch {
                particle.x.animateTo(
                    targetValue = cos(angle * PI.toFloat() / 180f) * radius,
                    animationSpec = tween(1200)
                )
                particle.y.animateTo(
                    targetValue = sin(angle * PI.toFloat() / 180f) * radius,
                    animationSpec = tween(1200)
                )
            }
        }

        // Phase 2: Particles coalesce into sphere (1500-2000ms)
        animationProgress.animateTo(
            targetValue = 0.55f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )

        // Phase 3: Sphere expands and bursts to reveal logo (2000-2800ms)
        animationProgress.animateTo(
            targetValue = 0.75f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )

        logoScale.animateTo(1.3f, animationSpec = tween(400))
        logoScale.animateTo(1f, animationSpec = tween(200))

        // Phase 4: Promotion text appears (2800-4000ms)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200)
        )

        promotionAlpha.animateTo(1f, animationSpec = tween(800))

        // Navigate after complete animation
        kotlinx.coroutines.delay(500) // Small pause after animation
        onNavigate(postSplashDestination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F23)), // Deep space blue
        contentAlignment = Alignment.Center
    ) {
        // Dynamic particle whirlpool background
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            if (animationProgress.value < 0.5f) {
                // Draw particles
                particles.forEach { particle ->
                    val x = centerX + particle.x.value
                    val y = centerY + particle.y.value
                    
                    drawCircle(
                        color = Color(particle.color),
                        radius = particle.size,
                        center = Offset(x, y),
                        alpha = minOf(1f, animationProgress.value * 2.5f)
                    )
                }
            } else {
                // Draw coalescing sphere
                val sphereRadius = 40f + (animationProgress.value - 0.5f) * 100f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF6B35), // Orange
                            Color(0xFFFF8C42), // Light orange
                            Color(0xFF4ADE80), // Green
                            Color(0xFF22C55E), // Emerald
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    radius = sphereRadius,
                    center = Offset(centerX, centerY),
                    alpha = minOf(1f, (animationProgress.value - 0.5f) * 4f)
                )

                // Add glowing effect
                drawCircle(
                    color = Color(0xFFFF6B35).copy(alpha = 0.3f),
                    radius = sphereRadius * 1.2f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Main logo that emerges from the sphere
            if (animationProgress.value > 0.6f) {
                Text(
                    text = "ExcelL",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(minOf(1f, (animationProgress.value - 0.6f) * 4f))
                )

                // Tagline appears with logo
                if (animationProgress.value > 0.65f) {
                    Text(
                        text = "Excellence Beyond Boundaries",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(minOf(1f, (animationProgress.value - 0.65f) * 6f))
                    )
                }
            }

            // Promotion text appears at the bottom (adaptable to screen size)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 48.dp), // Ensure not too close to bottom
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Promoted by Excellencia",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = promotionAlpha.value),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
