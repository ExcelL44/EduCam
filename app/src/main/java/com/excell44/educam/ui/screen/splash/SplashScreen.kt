package com.excell44.educam.ui.screen.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

@Composable
fun SplashScreen(
    postSplashDestination: String,
    onNavigate: (String) -> Unit
) {
    // ✅ SOLUTION 1 : State simple pour forcer la visibilité
    var showCircles by remember { mutableStateOf(false) }
    var showLogo by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showPromo by remember { mutableStateOf(false) }
    
    // ✅ SOLUTION 2 : Animation du fond (infinie et immédiate)
    val infinitePulse = rememberInfiniteTransition(label = "bgPulse")
    val backgroundPulse by infinitePulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // ✅ SOLUTION 3 : Lancer TOUT au démarrage sans condition
    LaunchedEffect(Unit) {
        launch {
            // Séquence timingée (pas basée sur progression)
            showCircles = true
            delay(1500) // 1.5s
            showLogo = true
            delay(1300) // 2.25s total
            showTagline = true
            delay(350) // 3.75s total
            showPromo = true
            delay(1450) // 5.2s total
            onNavigate(postSplashDestination)
        }
    }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // ===== PHASE 1 : Cercles =====
            AnimatedVisibility(
                visible = showCircles,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                ConcentricCircles()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== PHASE 2 : Logo =====
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn() + scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Text(
                    text = "ExcelL",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // ===== PHASE 3 : Tagline =====
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(animationSpec = tween(400, easing = EaseOutCubic)) +
                        slideInVertically(initialOffsetY = { 30 })
            ) {
                Text(
                    text = "Excellence Beyond Boundaries",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // ===== PHASE 4 : Promotion =====
            AnimatedVisibility(
                visible = showPromo,
                enter = fadeIn(animationSpec = tween(400, easing = EaseIn))
            ) {
                Text(
                    text = "Promoted by Excellencia",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// SIMPLIFIÉ : Pas besoin de paramètres
@Composable
fun ConcentricCircles() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .semantics { contentDescription = "Loading animation" },
        contentAlignment = Alignment.Center
    ) {
        (0..2).forEach { index ->
            val delay = index * 100L // 100ms entre chaque cercle
            
            LaunchedEffect(Unit) {
                delay(delay) // ✅ Décalage simple
            }
            
            Box(
                modifier = Modifier
                    .size(120.dp + (index * 30).dp)
                    .scale(0.5f + (index * 0.17f)) // ✅ Scale fixe pour chaque cercle
                    .alpha(0.3f)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }
    }
}