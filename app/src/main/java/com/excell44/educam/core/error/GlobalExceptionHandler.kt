package com.excell44.educam.core.error

import android.content.Context
import android.content.Intent
import com.excell44.educam.core.fallback.EmergencyFallback
import kotlin.system.exitProcess

/**
 * Gestionnaire global des exceptions non capturées.
 * Capture les crashs avant qu'Android ne ferme l'app brutalement.
 */
class GlobalExceptionHandler private constructor(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    private val emergencyFallback = EmergencyFallback.getInstance(context)

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // 1. Enregistrer le crash dans le système d'urgence
            emergencyFallback.recordCrash()
            
            // 2. Logger le crash
            logCrash(throwable)
            
            // 2. Sauvegarder l'état de l'app
            saveAppState()
            
            // 3. Afficher un écran de crash élégant au lieu de crasher brutalement
            launchCrashActivity(throwable)
            
            // 4. Terminer le processus proprement
            exitProcess(0)
        } catch (e: Exception) {
            // Si notre handler échoue, utiliser le handler par défaut
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun logCrash(throwable: Throwable) {
        val crashReport = buildString {
            appendLine("=== CRASH REPORT ===")
            appendLine("Time: ${System.currentTimeMillis()}")
            appendLine("Thread: ${Thread.currentThread().name}")
            appendLine("Exception: ${throwable.javaClass.simpleName}")
            appendLine("Message: ${throwable.message}")
            appendLine("\nStack Trace:")
            appendLine(throwable.stackTraceToString())
        }
        
        // Logger dans logcat
        println(crashReport)
        
        // TODO: Envoyer à Firebase Crashlytics en production
        // FirebaseCrashlytics.getInstance().recordException(throwable)
        
        // Sauvegarder localement
        saveCrashReportLocally(crashReport)
    }

    private fun saveCrashReportLocally(report: String) {
        try {
            val prefs = context.getSharedPreferences("crash_reports", Context.MODE_PRIVATE)
            val existingReports = prefs.getStringSet("reports", mutableSetOf()) ?: mutableSetOf()
            existingReports.add(report)
            
            // Garder seulement les 5 derniers crashs
            if (existingReports.size > 5) {
                existingReports.toMutableList().sortedDescending().drop(5)
            }
            
            prefs.edit().putStringSet("reports", existingReports).apply()
        } catch (e: Exception) {
            println("Failed to save crash report: ${e.message}")
        }
    }

    private fun saveAppState() {
        try {
            val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("last_crash_time", System.currentTimeMillis())
                .putBoolean("crashed", true)
                .apply()
        } catch (e: Exception) {
            println("Failed to save app state: ${e.message}")
        }
    }

    private fun launchCrashActivity(throwable: Throwable) {
        try {
            val intent = Intent(context, CrashActivity::class.java).apply {
                putExtra("error_message", throwable.message ?: "Unknown error")
                putExtra("stack_trace", throwable.stackTraceToString())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            println("Failed to launch crash activity: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var instance: GlobalExceptionHandler? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
                        instance = GlobalExceptionHandler(
                            context.applicationContext,
                            defaultHandler
                        )
                        Thread.setDefaultUncaughtExceptionHandler(instance)
                    }
                }
            }
        }

        /**
         * Vérifie si l'app a crashé lors du dernier lancement.
         */
        fun hasCrashedRecently(context: Context): Boolean {
            val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
            val lastCrash = prefs.getLong("last_crash_time", 0)
            val now = System.currentTimeMillis()
            
            // Consider "recently" as within last 5 minutes
            return (now - lastCrash) < 5 * 60 * 1000
        }

        /**
         * Marque l'app comme récupérée d'un crash.
         */
        fun markAsRecovered(context: Context) {
            val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("crashed", false)
                .apply()
        }
    }
}
