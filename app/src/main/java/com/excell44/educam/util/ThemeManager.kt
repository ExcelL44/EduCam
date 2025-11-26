package com.excell44.educam.util

import android.content.Context
import android.content.SharedPreferences
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

    // 7 gradient-only themes inspired by modern education platforms
    enum class ThemeColor(
        val gradient: Brush,
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val label: String,
        val description: String
    ) {
        // 0. Swiss Purity (White theme - default, clean and minimal)
        SWISS_PURITY(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF8FAFC), // Very light gray-blue
                    Color(0xFFFFFFFF), // Pure white
                    Color(0xFFF1F5F9)  // Light gray
                )
            ),
            Color(0xFFE2E8F0), // Primary: light gray
            Color(0xFFF1F5F9), // Secondary: very light gray
            Color(0xFFFFFFFF), // Tertiary: white
            "Pure White",
            "Clean and minimal default theme"
        ),

        // 1. Ocean Breeze - Calm and focused
        OCEAN_BREEZE(
            Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF0EA5E9), // Ocean blue
                    Color(0xFF38BDF8), // Sky blue
                    Color(0xFF7DD3FC), // Light blue
                    Color(0xFFBAE6FD)  // Very light blue
                )
            ),
            Color(0xFF0EA5E9),
            Color(0xFF38BDF8),
            Color(0xFF7DD3FC),
            "Ocean Breeze",
            "Calm focus for deep learning"
        ),

        // 2. Aurora Dream - Mystical northern lights inspiration
        AURORA_DREAM(
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFF06B6D4), // Cyan
                    Color(0xFF10B981), // Emerald
                    Color(0xFFF59E0B), // Amber
                )
            ),
            Color(0xFF8B5CF6),
            Color(0xFF06B6D4),
            Color(0xFF10B981),
            "Aurora Dream",
            "Mystical inspiration for creative thinking"
        ),

        // 3. Sunset Harmony - Warm and motivating
        SUNSET_HARMONY(
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF6B35), // Coral
                    Color(0xFFF7931E), // Orange
                    Color(0xFFE7A700), // Golden yellow
                    Color(0xFFFBBF24), // Soft yellow
                    Color(0xFFFEF3C7)  // Very light yellow
                )
            ),
            Color(0xFFFF6B35),
            Color(0xFFF7931E),
            Color(0xFFFBBF24),
            "Sunset Glow",
            "Warm motivation for studying"
        ),

        // 4. Forest Serenity - Fresh and natural
        FOREST_SERENITY(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF22C55E), // Emerald
                    Color(0xFF4ADE80), // Green
                    Color(0xFF86EFAC), // Light green
                    Color(0xFFDCFCE7)  // Very light green
                )
            ),
            Color(0xFF22C55E),
            Color(0xFF4ADE80),
            Color(0xFF86EFAC),
            "Forest Dew",
            "Fresh inspiration for growth"
        ),

        // 5. Digital Synergy - Modern tech aesthetic
        DIGITAL_SYNERGY(
            Brush.conicGradient(
                colors = listOf(
                    Color(0xFF6366F1), // Indigo
                    Color(0xFFEC4899), // Pink
                    Color(0xFF10B981), // Emerald
                    Color(0xFFF59E0B)  // Amber
                )
            ),
            Color(0xFF6366F1),
            Color(0xFFEC4899),
            Color(0xFF10B981),
            "Digital Wave",
            "Modern tech aesthetic for innovation"
        ),

        // 6. Scholar Sage - Classic academic wisdom
        SCHOLAR_SAGE(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF7C3AED), // Purple
                    Color(0xFFC4B5FD), // Light purple
                    Color(0xFFDDD6FE), // Very light purple
                    Color(0xFFF3F4F6)  // Gray-white
                )
            ),
            Color(0xFF7C3AED),
            Color(0xFFC4B5FD),
            Color(0xFFDDD6FE),
            "Scholar Sage",
            "Classic wisdom for academic excellence"
        );

        companion object {
            fun fromIndex(index: Int): ThemeColor {
                return values().getOrNull(index) ?: SWISS_PURITY
            }

            // Default theme (White/Pure theme)
            val DEFAULT_THEME_INDEX = 0
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

    /**
     * Reset theme to default (Pure White theme) - used when logging out
     */
    fun resetTheme() {
        prefs.edit().remove("theme_color").apply()
    }

    /**
     * Check if user has saved a personal theme preference (vs default)
     */
    fun hasUserDefinedTheme(): Boolean {
        return prefs.contains("theme_color")
    }
}
