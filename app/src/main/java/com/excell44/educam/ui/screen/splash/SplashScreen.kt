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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    postSplashDestination: String,
    onNavigate: (String) -> Unit,
    authViewModel: com.excell44.educam.ui.viewmodel.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    // State pour l'apparition progressive des textes
    var showLogo by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showPromo by remember { mutableStateOf(false) }
    
    // Animation du fond (infinie)
    val infinitePulse = rememberInfiniteTransition(label = "bgPulse")
    val backgroundPulse by infinitePulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Séquence d'apparition
    LaunchedEffect(Unit) {
        // Les cercles sont affichés dès le début (via le composable directement)
        delay(500) // Petit délai initial
        showLogo = true
        delay(1000) // Le logo apparait, on attend un peu
        showTagline = true
        delay(800) // La tagline apparait
        showPromo = true
        delay(2000) // On laisse tout affiché un moment

        // Attendre que l'auth soit déterminé (pas en Loading) - timeout 10s
        var waitTime = 0
        while (authState is com.excell44.educam.domain.model.AuthState.Loading && waitTime < 10000) {
            delay(100) // Attendre 100ms et vérifier à nouveau
            waitTime += 100
        }

        // Si toujours en Loading après 10s, forcer vers Login
        if (authState is com.excell44.educam.domain.model.AuthState.Loading) {
            android.util.Log.w("SplashScreen", "Auth still loading after 10s timeout, defaulting to login")
        }

        // Déterminer la destination finale basée sur l'état d'auth actuel
        val finalDestination = when (authState) {
            is com.excell44.educam.domain.model.AuthState.Authenticated -> com.excell44.educam.ui.navigation.Screen.Home.route
            else -> com.excell44.educam.ui.navigation.Screen.Login.route
        }

        android.util.Log.d("SplashScreen", "Navigation to: $finalDestination (authState: $authState)")
        onNavigate(finalDestination)
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
            
            // Cercles qui pulsent en continu
            PulsingCircles()

            Spacer(modifier = Modifier.height(32.dp))

            // Logo "ExcelL"
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(animationSpec = tween(1000)) + expandVertically(expandFrom = Alignment.CenterVertically)
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

            // Tagline "Excellence Beyond Boundaries"
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 20 })
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

            // Promo "Promoted by Excellencia"
            AnimatedVisibility(
                visible = showPromo,
                enter = fadeIn(animationSpec = tween(1000))
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

@Composable
fun PulsingCircles() {
    val infiniteTransition = rememberInfiniteTransition(label = "circlePulse")
    
    // Animation de scale pour l'effet de battement de coeur
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Animation d'alpha pour l'effet de respiration
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .semantics { contentDescription = "Pulsing animation" },
        contentAlignment = Alignment.Center
    ) {
        // Cercle central
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .alpha(alpha)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
        
        // Cercle extérieur (décalé légèrement ou juste plus grand)
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale * 0.9f)
                .alpha(alpha * 0.7f)
                .background(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = CircleShape
                )
        )
    }
}
