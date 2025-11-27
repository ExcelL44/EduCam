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
import androidx.compose.ui.text.TextStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    postSplashDestination: String,
    onNavigate: (String) -> Unit
) {
    // Animation principale : 0f → 1f sur 2.5s
    val animationProgress by rememberInfiniteTransition(label = "progress").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Restart
        )
    )

    // Détecteur de fin d'animation (une fois)
    LaunchedEffect(animationProgress) {
        if (animationProgress >= 1f) {
            delay(500) // Pause sur le final frame
            onNavigate(postSplashDestination)
        }
    }

    // Fond animé avec dégradé qui pulse
    val backgroundPulse by animateFloatAsState(
        targetValue = 0.4f + (animationProgress * 0.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3D5AFE).copy(alpha = backgroundPulse),
                        Color(0xFF0A0A1F)
                    ),
                    center = Offset(0.5f, 0.3f),
                    radius = 0.8f
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espace flexible en haut
            Spacer(modifier = Modifier.weight(0.3f))

            // ===== PHASE 1 : Cercles concentriques (début à 0.2f) =====
            if (animationProgress > 0.2f) {
                ConcentricCircles(animationProgress = animationProgress)
            }

            // ===== PHASE 2 : Logo avec bounce (début à 0.5f) =====
            if (animationProgress > 0.5f) {
                val logoScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "logoBounce"
                )

                Text(
                    text = "ExcelL",
                    fontSize = 56.sp, // Plus impactant
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .scale(logoScale)
                        .alpha(minOf(1f, (animationProgress - 0.5f) * 3f))
                )
            }

            // ===== PHASE 3 : Tagline avec slide (début à 0.7f) =====
            if (animationProgress > 0.7f) {
                val taglineOffset by animateFloatAsState(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = EaseOutCubic
                    ),
                    label = "taglineSlide"
                )

                Spacer(modifier = Modifier.height(64.dp)) // Distance visuelle cohérente

                Text(
                    text = "Excellence Beyond Boundaries",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(y = taglineOffset.dp)
                        .alpha(minOf(1f, (animationProgress - 0.7f) * 4f))
                )
            }

            // ===== PHASE 4 : Texte promotionnel (début à 0.85f) =====
            if (animationProgress > 0.85f) {
                Spacer(modifier = Modifier.weight(0.4f))

                val promoAlpha by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(400, easing = EaseIn),
                    label = "promoFade"
                )

                Text(
                    text = "Promoted by Excellencia",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = promoAlpha * 0.7f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(32.dp))
            } else {
                // Garde l'espace même quand invisible
                Spacer(modifier = Modifier.weight(0.4f))
            }
        }
    }
}

// Composable pour les cercles qui apparaissent
@Composable
fun ConcentricCircles(animationProgress: Float) {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        (0..2).forEach { index ->
            val circleDelay = index * 0.05f
            val circleAlpha = minOf(
                1f,
                maxOf(0f, (animationProgress - 0.2f - circleDelay) * 2f)
            )

            Box(
                modifier = Modifier
                    .size(120.dp + (index * 30).dp)
                    .scale(0.5f + (circleAlpha * 0.5f))
                    .alpha(circleAlpha * 0.3f)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }
    }
}
