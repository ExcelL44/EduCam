package com.excell44.educam.ui.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    postSplashDestination: String,
    onNavigate: (String) -> Unit
) {
    // Animation principale : 0f → 1f sur 5.0s (UNE SEULE FOIS)
    val animationProgress = remember { Animatable(0f) }

    // Lancer l'animation au démarrage
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 5000,
                easing = EaseInOutQuart
            )
        )
        // Navigation à exactement 5.0s
        onNavigate(postSplashDestination)
    }

    // Fond animé avec dégradé qui pulse (lié à l'animation principale)
    val backgroundPulse = 0.4f + (animationProgress.value * 0.3f)

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espace flexible en haut
            Spacer(modifier = Modifier.weight(0.3f))

            // ===== PHASE 1 : Cercles concentriques (début à 1.5s = 0.3f) =====
            if (animationProgress.value > 0.3f) {
                ConcentricCircles(animationProgress = animationProgress.value)
            }

            // ===== PHASE 2 : Logo avec bounce (début à 2.25s = 0.45f) =====
            if (animationProgress.value > 0.45f) {
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
                        .alpha(minOf(1f, (animationProgress.value - 0.45f) * 3f))
                )
            }

            // ===== PHASE 3 : Tagline avec slide (début à 3.75s = 0.75f) =====
            if (animationProgress.value > 0.75f) {
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
                        .alpha(minOf(1f, (animationProgress.value - 0.75f) * 4f))
                )
            }

            // ===== PHASE 4 : Texte promotionnel (début à 4.1s = 0.82f) =====
            if (animationProgress.value > 0.82f) {
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
