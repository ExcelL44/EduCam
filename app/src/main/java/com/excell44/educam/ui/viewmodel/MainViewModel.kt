package com.excell44.educam.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.excell44.educam.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.excell44.educam.core.network.NetworkObserver
import com.excell44.educam.data.repository.AuthRepository
import com.excell44.educam.data.local.SecurePrefs
import kotlinx.coroutines.flow.first

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val networkObserver: NetworkObserver,
    private val authRepository: AuthRepository,
    private val securePrefs: SecurePrefs
) : ViewModel() {

    private val _themeIndex = MutableStateFlow(0)
    val themeIndex: StateFlow<Int> = _themeIndex.asStateFlow()

    init {
        loadTheme()
    }

    private fun loadTheme() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("bacx_prefs", Context.MODE_PRIVATE)
                val savedTheme = prefs.getInt("theme_index", 0)
                _themeIndex.value = savedTheme
                Logger.d("MainViewModel", "Theme loaded: $savedTheme")
            } catch (e: Exception) {
                Logger.e("MainViewModel", "Error loading theme", e)
            }
        }
    }

    fun updateTheme(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✅ FIX: Check if user is PASSIVE before allowing theme change
                val userId = securePrefs.getUserId()
                if (userId != null) {
                    val user = authRepository.getCurrentUser(userId).first()
                    
                    if (user != null && user.role == "PASSIVE") {
                        Logger.w("MainViewModel", "Theme change blocked for PASSIVE user: ${user.pseudo}")
                        android.util.Log.d("🎨 THEME", "❌ Theme change BLOCKED - User is in PASSIVE mode (trial)")
                        return@launch // ✅ Block theme change for passive users
                    }
                }
                
                // Save to prefs
                val prefs = context.getSharedPreferences("bacx_prefs", Context.MODE_PRIVATE)
                prefs.edit().putInt("theme_index", index).apply()
                
                // Update state (Main thread for UI)
                launch(Dispatchers.Main) {
                    _themeIndex.value = index
                    Logger.d("MainViewModel", "Theme updated to: $index")
                }
            } catch (e: Exception) {
                Logger.e("MainViewModel", "Error updating theme", e)
            }
        }
    }
}
