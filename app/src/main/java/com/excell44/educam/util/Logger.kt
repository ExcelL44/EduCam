package com.excell44.educam.util

import android.util.Log

/**
 * Système de logging structuré avec niveaux.
 * Prêt pour Timber ou Firebase Crashlytics.
 */
object Logger {
    
    enum class Level {
        VERBOSE, DEBUG, INFO, WARNING, ERROR, CRITICAL
    }
    
    private const val TAG_PREFIX = "EduCam"
    
    // Enable/disable logging per level
    private val enabledLevels = mutableSetOf(
        Level.WARNING, Level.ERROR, Level.CRITICAL
    )
    
    /**
     * Active tous les niveaux de log (DEBUG mode).
     */
    fun enableDebugMode() {
        enabledLevels.addAll(Level.values())
    }
    
    /**
     * Désactive les logs verbeux (RELEASE mode).
     */
    fun enableReleaseMode() {
        enabledLevels.clear()
        enabledLevels.addAll(listOf(Level.WARNING, Level.ERROR, Level.CRITICAL))
    }
    
    /**
     * Log verbeux (détails techniques).
     */
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (Level.VERBOSE in enabledLevels) {
            Log.v("$TAG_PREFIX:$tag", message, throwable)
        }
    }
    
    /**
     * Log debug (informations de développement).
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (Level.DEBUG in enabledLevels) {
            Log.d("$TAG_PREFIX:$tag", message, throwable)
        }
    }
    
    /**
     * Log info (informations générales).
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (Level.INFO in enabledLevels) {
            Log.i("$TAG_PREFIX:$tag", message, throwable)
        }
    }
    
    /**
     * Log warning (avertissements non-bloquants).
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (Level.WARNING in enabledLevels) {
            Log.w("$TAG_PREFIX:$tag", message, throwable)
        }
    }
    
    /**
     * Log error (erreurs gérées).
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (Level.ERROR in enabledLevels) {
            Log.e("$TAG_PREFIX:$tag", message, throwable)
            // TODO: Send to Firebase Crashlytics
            // FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }
    
    /**
     * Log critique (erreurs critiques).
     */
    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        if (Level.CRITICAL in enabledLevels) {
            Log.wtf("$TAG_PREFIX:$tag", message, throwable)
            // TODO: Send to Firebase Crashlytics with high priority
        }
    }
    
    /**
     * Log une navigation.
     */
    fun logNavigation(from: String, to: String) {
        d("Navigation", "From: $from → To: $to")
    }
    
    /**
     * Log une action utilisateur.
     */
    fun logUserAction(action: String, details: Map<String, Any> = emptyMap()) {
        val detailsStr = details.entries.joinToString(", ") { "${it.key}=${it.value}" }
        i("UserAction", "$action | $detailsStr")
    }
    
    /**
     * Log une métrique de performance.
     */
    fun logPerformance(operation: String, durationMs: Long) {
        if (durationMs > 1000) {
            w("Performance", "$operation took ${durationMs}ms (slow!)")
        } else {
            d("Performance", "$operation: ${durationMs}ms")
        }
    }
    
    /**
     * Log une opération de données.
     */
    fun logDataOperation(operation: String, table: String, success: Boolean) {
        val status = if (success) "SUCCESS" else "FAILED"
        i("Database", "$operation on $table: $status")
    }
    
    /**
     * Crée un log structuré en JSON (pour parsing).
     */
    fun logStructured(
        event: String,
        data: Map<String, Any>,
        level: Level = Level.INFO
    ) {
        val json = buildString {
            append("{")
            append("\"timestamp\": ${System.currentTimeMillis()},")
            append("\"event\": \"$event\",")
            append("\"level\": \"$level\",")
            data.entries.forEachIndexed { index, entry ->
                append("\"${entry.key}\": \"${entry.value}\"")
                if (index < data.size - 1) append(",")
            }
            append("}")
        }
        
        when (level) {
            Level.VERBOSE -> v("StructuredLog", json)
            Level.DEBUG -> d("StructuredLog", json)
            Level.INFO -> i("StructuredLog", json)
            Level.WARNING -> w("StructuredLog", json)
            Level.ERROR -> e("StructuredLog", json)
            Level.CRITICAL -> wtf("StructuredLog", json)
        }
    }
}

/**
 * Extension pour logger facilement depuis n'importe où.
 */
inline fun <reified T> T.logDebug(message: String) {
    Logger.d(T::class.java.simpleName, message)
}

inline fun <reified T> T.logInfo(message: String) {
    Logger.i(T::class.java.simpleName, message)
}

inline fun <reified T> T.logWarning(message: String, throwable: Throwable? = null) {
    Logger.w(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logError(message: String, throwable: Throwable? = null) {
    Logger.e(T::class.java.simpleName, message, throwable)
}
