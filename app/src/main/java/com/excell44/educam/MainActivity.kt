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
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val navController = rememberNavController()
    
    // Inject NetworkObserver (normally would be provided by Hilt to a ViewModel, but here we need it for UI)
    // For simplicity in this refactor, we can get it from EntryPoint or pass it down.
    // Ideally, MainViewModel should expose isOnline state.
    // Let's use a quick Hilt EntryPoint workaround or better: inject into MainViewModel.
    
    val mainViewModel: com.excell44.educam.ui.viewmodel.MainViewModel = hiltViewModel()
    // We need to update MainViewModel to expose NetworkObserver or isOnline
    
    // Determine start destination based on AuthState
    val startDestination = when (val state = authState) {
        is com.excell44.educam.domain.model.AuthState.Authenticated -> Screen.Home.route
        else -> Screen.Login.route
    }
    
    // Show Splash while loading
    if (authState is com.excell44.educam.domain.model.AuthState.Loading) {
        com.excell44.educam.ui.screen.splash.SplashScreen(
            postSplashDestination = "",
            onNavigate = {} 
        )
        return
    }
    
    LaunchedEffect(startDestination) {
        android.util.Log.d("MainActivity", "Start Destination: $startDestination (State: $authState)")
    }

    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        // Offline Indicator at the top
        com.excell44.educam.ui.components.OfflineIndicator(networkObserver = mainViewModel.networkObserver)
        
        // Start with splash, then the splash composable will navigate to startDestination
        NavGraph(navController = navController, startDestination = com.excell44.educam.ui.navigation.Screen.Splash.route, postSplashDestination = startDestination)
    }
}
