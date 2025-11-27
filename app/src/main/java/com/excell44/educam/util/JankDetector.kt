package com.excell44.educam.util

import android.os.Build
import android.view.Choreographer
import androidx.compose.runtime.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Détecteur de jank (frame drops) pour identifier les problèmes de performance.
 * 
 * Un jank est détecté quand le temps entre deux frames dépasse 16ms (60 FPS).
 * Utile pour détecter les problèmes de performance en temps réel.
 */
object JankDetector {
    
    private const val TARGET_FRAME_TIME_MS = 16.0 // 60 FPS
    private const val JANK_THRESHOLD_MS = 32.0 // 2 frames manquées
    
    private var lastFrameTimeNanos = 0L
    private val jankEvents = Channel<JankEvent>(Channel.BUFFERED)
    
    data class JankEvent(
        val frameTimeMs: Double,
        val droppedFrames: Int,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Démarre la détection de jank.
     * Retourne un Flow d'événements de jank détectés.
     */
    fun startMonitoring(): Flow<JankEvent> = flow {
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameTimeNanos != 0L) {
                    val frameTimeMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000.0
                    
                    if (frameTimeMs > JANK_THRESHOLD_MS) {
                        val droppedFrames = (frameTimeMs / TARGET_FRAME_TIME_MS).toInt() - 1
                        val event = JankEvent(frameTimeMs, droppedFrames)
                        jankEvents.trySend(event)
                    }
                }
                
                lastFrameTimeNanos = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        
        Choreographer.getInstance().postFrameCallback(callback)
        
        for (event in jankEvents) {
            emit(event)
        }
    }
    
    /**
     * Version simple qui log les janks dans la console.
     */
    fun logJanks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    if (lastFrameTimeNanos != 0L) {
                        val frameTimeMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000.0
                        
                        if (frameTimeMs > JANK_THRESHOLD_MS) {
                            val droppedFrames = (frameTimeMs / TARGET_FRAME_TIME_MS).toInt() - 1
                            println("⚠️ JANK DETECTED: ${frameTimeMs.toInt()}ms ($droppedFrames frames dropped)")
                        }
                    }
                    
                    lastFrameTimeNanos = frameTimeNanos
                    Choreographer.getInstance().postFrameCallback(this)
                }
            })
        }
    }
}

/**
 * Composable pour monitorer automatiquement les janks dans un écran.
 */
@Composable
fun MonitorJank(
    screenName: String,
    onJankDetected: ((JankDetector.JankEvent) -> Unit)? = null
) {
    DisposableEffect(screenName) {
        val job = kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            JankDetector.startMonitoring().collect { event ->
                println("🎯 Jank in $screenName: ${event.frameTimeMs.toInt()}ms (${event.droppedFrames} frames)")
                onJankDetected?.invoke(event)
            }
        }
        
        onDispose {
            job.cancel()
        }
    }
}
