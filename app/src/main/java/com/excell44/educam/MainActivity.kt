package com.excell44.educam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.ui.navigation.NavGraph
import com.excell44.educam.ui.navigation.Screen
import com.excell44.educam.ui.theme.BacXTheme
import com.excell44.educam.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No longer need manual DB initialization - questions are generated on-demand
        enableEdgeToEdge()
        setContent {
            // Charger le thème sauvegardé (défaut = 0: Focus Clair)
            val prefs = getSharedPreferences("educam_prefs", MODE_PRIVATE)
            val themeIndex = prefs.getInt("theme_index", 0)

            BacXTheme(themeIndex = themeIndex) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    appContent()
                }
            }
        }
    }
}

@Composable
fun appContent() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    
    // ✅ Guard: Afficher Splash pendant chargement asynchrone
    if (uiState.isLoading) {
        com.excell44.educam.ui.screen.splash.SplashScreen(
            postSplashDestination = "",
            onNavigate = {} // No-op pendant chargement
        )
        return
    }
    
    // ✅ Reactive destination - recalculates on every isLoggedIn change
    val startDestination = if (uiState.isLoggedIn) Screen.Home.route else Screen.Login.route
    
    LaunchedEffect(startDestination) {
        android.util.Log.d("MainActivity", "Start Destination: $startDestination (isLoggedIn=${uiState.isLoggedIn})")
    }

    // ✅ SUPPRIMÉ: LaunchedEffect(uiState.isLoggedIn) - Navigation gérée par NavGraph uniquement

    // Start with splash, then the splash composable will navigate to startDestination
    NavGraph(navController = navController, startDestination = com.excell44.educam.ui.navigation.Screen.Splash.route, postSplashDestination = startDestination)
}
