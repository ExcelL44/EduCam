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
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                        Color.Black
                    ),
                    center = Offset.Infinite
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
                            x = (particle.startX * (1 - progress) + particle.targetX * progress).dp,
                            y = (particle.startY * (1 - progress) + particle.targetY * progress).dp
                        )
                        .size(4.dp)
                        .scale(progress)
                        .alpha(progress)
                        .background(
                            color = particle.color,
                            shape = CircleShape
                        )
                )
            }
        }

        // Nexus portal background
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(portalScale.value)
                .alpha(portalAlpha.value)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Brilliant skill points/XP bar (gamification style)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .width((xpAnim.value * 200).dp)
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                        .alpha(currentXp / maxXp)
                )
            }

            Text(
                text = "${currentXp.toInt()}/${maxXp.toInt()} Brilliance Points",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Logo with brilliant transformation
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.rotate(logoRotate.value)
        ) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale.value * 1.2f)
                    .alpha(glowAlpha.value * 0.4f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Inner brilliant core
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Brilliance wave
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale.value)
                    .rotate(waveAnim.value)
                    .alpha((waveAnim.value / 360f) * 0.8f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Text with premium academic styling
            Text(
                text = "ExcelL",
                fontSize = (logoScale.value * 48).sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(logoScale.value)
            )
        }

        // Brilliant wisdom quotes that fade in and out
        val quotes = listOf(
            "Brilliance is earned, not given",
            "Every question unlocks knowledge",
            "Excellence through persistence"
        )
        var currentQuote by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1200)
                currentQuote = (currentQuote + 1) % quotes.size
            }
        }

        Text(
            text = quotes[currentQuote],
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )

        // Footer with elegance
        Text(
            text = "Excellence Beyond Boundaries",
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}

// Brilliant particle system inspired by mathematical elegance
data class Particle(
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    val color: Color
) {
    companion object {
        fun random(): Particle {
            val angle = (Math.random() * 2 * PI).toFloat()
            val radius = (50 + Math.random() * 100).toFloat()
            val startRadius = (150 + Math.random() * 50).toFloat()

            return Particle(
                startX = cos(angle) * startRadius,
                startY = sin(angle) * startRadius,
                targetX = cos(angle) * radius * (0.8f + Math.random().toFloat() * 0.4f),
                targetY = sin(angle) * radius * (0.8f + Math.random().toFloat() * 0.4f),
                color = listOf(
                    Color(0xFF6366F1), // Indigo
                    Color(0xFF8B5CF6), // Violet
                    Color(0xFF06B6D4), // Cyan
                    Color(0xFF10B981), // Emerald
                    Color(0xFFF59E0B), // Amber
                    Color(0xFFEF4444),  // Red
                ).random()
            )
        }
    }
}
