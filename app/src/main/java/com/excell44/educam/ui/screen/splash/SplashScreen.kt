package com.excell44.educam.ui.screen.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    totalDurationMs: Int = 3000
) {
    // Word to reveal
    val word = "ExcelL"
    val steps = word.length
    val interval = (totalDurationMs - 400) / steps // leave small tail for settling

    // per-letter animatables for scale and alpha
    val scales = remember { List(steps) { Animatable(0.85f) } }
    val alphas = remember { List(steps) { Animatable(0f) } }
    var progressVisible by remember { mutableStateOf(true) }

    // subtle pulsing behind the word
    val infinite = rememberInfiniteTransition()
    val pulse by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse)
    )

    LaunchedEffect(Unit) {
        // stagger each letter with a lively easing
        for (i in 0 until steps) {
            // launch parallel animations per letter for smoothness
            launch {
                scales[i].animateTo(
                    targetValue = 1.12f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
                scales[i].animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
            }
            launch {
                alphas[i].animateTo(1f, animationSpec = tween(durationMillis = 300))
            }
            delay(interval.toLong())
        }

        // short tail to complete totalDurationMs and hide spinner
        delay(300)
        progressVisible = false
        // navigate
        onNavigate(postSplashDestination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // pulsating subtle circle behind the text to give life
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentWidth(Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulse)
                        .alpha(0.06f)
                        .background(color = MaterialTheme.colorScheme.tertiary, shape = CircleShape)
                )

                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 0 until steps) {
                        val s = scales[i].value
                        val a = alphas[i].value
                        Text(
                            text = word[i].toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(2.dp)
                                .scale(s)
                                .alpha(a),
                            // keep typography but no explicit text shadow to ensure compatibility
                            // (subtle backdrop circle and scale/alpha give depth)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (progressVisible) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.tertiary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // bottom label
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Promote By Excellencia Corp",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
