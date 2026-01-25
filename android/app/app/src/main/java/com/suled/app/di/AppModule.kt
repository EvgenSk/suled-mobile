package com.suled.app.di

import android.content.Context
import com.suled.app.data.local.PreferencesManager
import com.suled.wear.WearDataSyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for app-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
    
    @Provides
    @Singleton
    fun provideWearDataSyncService(
        @ApplicationContext context: Context
    ): WearDataSyncService {
        return WearDataSyncService(context)
    }
}
