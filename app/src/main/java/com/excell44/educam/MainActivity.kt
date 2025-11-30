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
import com.excell44.educam.domain.model.AuthState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val mainViewModel: com.excell44.educam.ui.viewmodel.MainViewModel = hiltViewModel()
            val themeIndex by mainViewModel.themeIndex.collectAsState()

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
    android.util.Log.d("🔴 MAIN_ACTIVITY", "🚀 appContent() STARTED - Initializing app components")

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    // ✅ Inject NavigationViewModel here to attach controller early
    val navigationViewModel: com.excell44.educam.ui.navigation.NavigationViewModel = hiltViewModel()
    
    val navController = rememberNavController()
    
    // ✅ Attach NavController IMMEDIATELY via SideEffect to avoid race conditions
    androidx.compose.runtime.SideEffect {
        android.util.Log.d("🔴 MAIN_ACTIVITY", "🔗 Attaching NavController via SideEffect")
        navigationViewModel.setNavController(navController)
    }

    android.util.Log.d("🔴 MAIN_ACTIVITY", "📱 NavController created: $navController")
    android.util.Log.d("🔴 MAIN_ACTIVITY", "📱 AuthViewModel injected successfully")

    val mainViewModel: com.excell44.educam.ui.viewmodel.MainViewModel = hiltViewModel()
    android.util.Log.d("🔴 MAIN_ACTIVITY", "📊 MainViewModel injected")

    // Determine start destination based on AuthState
    val startDestination = when (val state = authState) {
        is com.excell44.educam.domain.model.AuthState.Authenticated -> Screen.Home.route
        else -> Screen.Login.route
    }

    android.util.Log.d("🔴 MAIN_ACTIVITY", "🎯 Initial start destination calculated: $startDestination (AuthState: $authState)")

    // Always show splash first, then navigate based on auth state

    LaunchedEffect(startDestination) {
        android.util.Log.d("🔴 MAIN_ACTIVITY", "🔄 LaunchedEffect triggered - Start Destination: $startDestination (State: $authState)")
    }

    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        // Offline Indicator at the top
        com.excell44.educam.ui.components.OfflineIndicator(networkObserver = mainViewModel.networkObserver)
        
        // Start with splash, then the splash composable will navigate to startDestination
        NavGraph(
            navController = navController, 
            startDestination = com.excell44.educam.ui.navigation.Screen.Splash.route, 
            postSplashDestination = startDestination,
            navigationViewModel = navigationViewModel // ✅ Pass the instance with attached controller
        )
    }
}
