package com.suled.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Suled app
 * Enables Hilt dependency injection
 */
@HiltAndroidApp
class SuledApplication : Application()
