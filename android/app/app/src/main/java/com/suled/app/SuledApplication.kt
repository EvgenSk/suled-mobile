package com.suled.app

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for Suled app
 * Enables Hilt dependency injection
 */
@HiltAndroidApp
class SuledApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // In production, plant a tree that logs to Firebase Crashlytics
            Timber.plant(CrashlyticsTree())
        }
    }
    
    /**
     * Custom Timber tree for release builds that sends logs to Firebase Crashlytics
     * Logs warnings and errors to Crashlytics for remote monitoring
     */
    private class CrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                
                // Log message to Crashlytics
                crashlytics.log("$tag: $message")
                
                // If there's an exception, record it
                t?.let { 
                    crashlytics.recordException(it)
                }
            }
        }
    }
}
