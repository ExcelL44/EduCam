package com.excell44.educam

import android.app.Application
import com.excell44.educam.core.error.GlobalExceptionHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EduCamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize global crash handler to prevent brutal app crashes
        GlobalExceptionHandler.initialize(this)
    }
}
