package com.excell44.educam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.ui.navigation.NavGraph
import com.excell44.educam.ui.navigation.Screen
import com.excell44.educam.ui.theme.EduCamTheme
import com.excell44.educam.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduCamTheme {
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
    
    val startDestination = remember(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) Screen.Home.route else Screen.Login.route
    }

    // Start with splash, then the splash composable will navigate to startDestination
    NavGraph(navController = navController, startDestination = com.excell44.educam.ui.navigation.Screen.Splash.route, postSplashDestination = startDestination)
}