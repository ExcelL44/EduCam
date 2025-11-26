package com.excell44.educam.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("educam_prefs", Context.MODE_PRIVATE)

    // Enhanced gradient themes inspired by modern education platforms
    enum class ThemeColor(
        val gradient: Brush,
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val label: String
    ) {
        // 1. Ocean Breeze - Calm and focused
        OCEAN_BREEZE(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0EA5E9), // Ocean blue
                    Color(0xFF38BDF8), // Sky blue
                    Color(0xFF7DD3FC)  // Light blue
                )
            ),
            Color(0xFF0EA5E9),
            Color(0xFF38BDF8),
            Color(0xFF7DD3FC),
            "Ocean Breeze"
        ),

        // 2. Sunset Glow - Warm and motivating
        SUNSET_GLOW(
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF6B35), // Coral
                    Color(0xFFF7931E), // Orange
                    Color(0xFFE7A700), // Golden yellow
                    Color(0xFFFBBF24)  // Soft yellow
                )
            ),
            Color(0xFFFF6B35),
            Color(0xFFF7931E),
            Color(0xFFFBBF24),
            "Sunset Glow"
        ),

        // 3. Forest Dew - Fresh and natural
        FOREST_DEW(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF22C55E), // Emerald
                    Color(0xFF4ADE80), // Green
                    Color(0xFF86EFAC)  // Light green
                )
            ),
            Color(0xFF22C55E),
            Color(0xFF4ADE80),
            Color(0xFF86EFAC),
            "Forest Dew"
        );

        companion object {
            fun fromIndex(index: Int): ThemeColor {
                return values().getOrNull(index) ?: OCEAN_BREEZE
            }
        }
    }

    // Theme animation parameters
    data class ThemeAnimation(
        val pulseScale: Animatable<Float, AnimationVector1D>,
        val colorTransition: Animatable<Float, AnimationVector1D>,
        val gradientOffset: Animatable<Float, AnimationVector1D>
    )

    @Composable
    fun getCurrentAnimation(): ThemeAnimation {
        val pulseScale = remember { Animatable(1f) }
        val colorTransition = remember { Animatable(0f) }
        val gradientOffset = remember { Animatable(0f) }

        // Pulsing animation for lively feel
        LaunchedEffect(Unit) {
            pulseScale.animateTo(
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        // Gradient shifting for depth
        LaunchedEffect(Unit) {
            gradientOffset.animateTo(
                targetValue = 100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        return ThemeAnimation(pulseScale, colorTransition, gradientOffset)
    }

    fun saveThemeColor(colorIndex: Int) {
        prefs.edit().putInt("theme_color", colorIndex).apply()
    }

    fun getThemeColor(): ThemeColor {
        val index = prefs.getInt("theme_color", 0)
        return ThemeColor.fromIndex(index)
    }

    fun getThemeColorIndex(): Int {
        return prefs.getInt("theme_color", 0)
    }

    fun getAvailableThemes(): List<ThemeColor> {
        return ThemeColor.values().toList()
    }

    // For backwards compatibility with single color usage
    fun getPrimaryColor(): Color {
        return getThemeColor().primary
    }

    fun getSecondaryColor(): Color {
        return getThemeColor().secondary
    }

    fun getTertiaryColor(): Color {
        return getThemeColor().tertiary
    }
}
