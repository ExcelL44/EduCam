package com.excell44.educam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.excell44.educam.core.network.NetworkObserver

@Composable
fun OfflineIndicator(
    networkObserver: NetworkObserver
) {
    val isOnline by networkObserver.networkStatus.collectAsState(initial = true)
    
    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFA000)) // Amber 700
                .statusBarsPadding() // ✅ Fix: Avoid status bar overlap
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mode Hors Ligne - Synchronisation en attente",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}
