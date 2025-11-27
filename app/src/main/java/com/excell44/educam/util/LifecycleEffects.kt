package com.excell44.educam.util

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.platform.LocalLifecycleOwner

/**
 * Effect pour observer les changements de lifecycle et nettoyer proprement les ressources.
 * Utilise DisposableEffect pour garantir le cleanup.
 * 
 * @param onCreate Appelé quand le composable est créé (ON_CREATE)
 * @param onStart Appelé quand le composable est visible (ON_START)
 * @param onResume Appelé quand le composable est actif (ON_RESUME)
 * @param onPause Appelé quand le composable est mis en pause (ON_PAUSE)
 * @param onStop Appelé quand le composable n'est plus visible (ON_STOP)
 * @param onDestroy Appelé quand le composable est détruit (ON_DESTROY)
 */
@Composable
fun LifecycleAwareEffect(
    onCreate: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> onCreate?.invoke()
                Lifecycle.Event.ON_START -> onStart?.invoke()
                Lifecycle.Event.ON_RESUME -> onResume?.invoke()
                Lifecycle.Event.ON_PAUSE -> onPause?.invoke()
                Lifecycle.Event.ON_STOP -> onStop?.invoke()
                Lifecycle.Event.ON_DESTROY -> onDestroy?.invoke()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

/**
 * Alternative simplifiée pour des actions sur pause/resume uniquement.
 * Utile pour pauser/reprendre des animations, timers, etc.
 */
@Composable
fun OnPauseResumeEffect(
    onResume: () -> Unit,
    onPause: () -> Unit
) {
    LifecycleAwareEffect(
        onResume = onResume,
        onPause = onPause
    )
}

/**
 * Effect pour exécuter du code une seule fois au démarrage.
 * Plus sûr que LaunchedEffect(Unit) car garantit une seule exécution.
 */
@Composable
fun OnFirstComposition(
    block: () -> Unit
) {
    val executed = remember { mutableStateOf(false) }
    
    if (!executed.value) {
        block()
        executed.value = true
    }
}
