package com.excell44.educam.core.error

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Gestionnaire global des exceptions dans les coroutines.
 * Empêche qu'une coroutine qui crash fasse crasher toute l'app.
 */
object GlobalCoroutineExceptionHandler {

    /**
     * Handler global pour toutes les coroutines.
     * Capture et log les exceptions au lieu de les propager.
     */
    val handler = CoroutineExceptionHandler { context, throwable ->
        println("=== COROUTINE EXCEPTION ===")
        println("Context: $context")
        println("Exception: ${throwable.javaClass.simpleName}")
        println("Message: ${throwable.message}")
        println("Stack trace:")
        throwable.printStackTrace()
        
        // TODO: Envoyer à Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    /**
     * Crée un CoroutineScope supervisé avec gestion d'erreurs.
     * Utilise SupervisorJob pour isoler les erreurs entre coroutines.
     */
    fun createSupervisedScope(
        context: CoroutineContext
    ): CoroutineScope {
        return CoroutineScope(
            context + SupervisorJob() + handler
        )
    }
}

/**
 * Extension pour exécuter du code critique qui ne doit jamais être annulé.
 * Utilise NonCancellable pour garantir l'exécution même en cas d'annulation.
 */
suspend fun <T> criticalSection(block: suspend () -> T): T {
    return kotlinx.coroutines.NonCancellable.run {
        block()
    }
}
