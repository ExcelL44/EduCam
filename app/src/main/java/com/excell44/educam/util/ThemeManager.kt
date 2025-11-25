package com.excell44.educam.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("educam_prefs", Context.MODE_PRIVATE)

    // Modern teen-friendly theme colors
    enum class ThemeColor(val color: Color, val label: String) {
        SOFT_PINK(Color(0xFFFFB3D9), "Rose Doux"),
        SKY_BLUE(Color(0xFF87CEEB), "Bleu Ciel"),
        MINT_GREEN(Color(0xFF98FF98), "Vert Menthe"),
        LAVENDER(Color(0xFFE6E6FA), "Lavande"),
        CORAL(Color(0xFFFF7F50), "Corail"),
        TEAL(Color(0xFF40E0D0), "Turquoise"),
        PEACH(Color(0xFFFFDAB9), "Pêche");

        companion object {
            fun fromIndex(index: Int): ThemeColor {
                return values().getOrNull(index) ?: SOFT_PINK
            }
        }
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
}
