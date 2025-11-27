package com.excell44.educam.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIConfigDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("ai_config", Context.MODE_PRIVATE)
    
    private val _isAIEnabled = MutableStateFlow(prefs.getBoolean("ai_enabled", true))
    val isAIEnabled: StateFlow<Boolean> = _isAIEnabled.asStateFlow()
    
    fun setAIEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ai_enabled", enabled).apply()
        _isAIEnabled.value = enabled
    }
}
