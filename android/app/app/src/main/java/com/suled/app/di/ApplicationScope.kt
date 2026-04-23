package com.suled.app.di

import javax.inject.Qualifier

/**
 * Qualifier for the application-lifetime [kotlinx.coroutines.CoroutineScope].
 * Inject this scope where long-running operations tied to the app lifecycle are needed.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
