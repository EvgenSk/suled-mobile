package com.suled.app

import android.app.Application
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
            // In production, plant a tree that logs to Crashlytics or a custom logging service
            Timber.plant(ReleaseTree())
        }
    }
    
    /**
     * Custom Timber tree for release builds
     * Logs only warnings and errors, can be extended to send to crash reporting
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                // TODO: Send to crash reporting service (e.g., Firebase Crashlytics)
                // For now, we still log but could be filtered out by ProGuard
            }
        }
    }
}
