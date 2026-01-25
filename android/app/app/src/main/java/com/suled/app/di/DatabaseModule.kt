package com.suled.app.di

import android.content.Context
import androidx.room.Room
import com.suled.app.data.local.SuledDatabase
import com.suled.app.data.local.dao.TournamentDao
import com.suled.app.data.local.dao.TrackedPairDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SuledDatabase {
        return Room.databaseBuilder(
            context,
            SuledDatabase::class.java,
            "suled_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideTournamentDao(database: SuledDatabase): TournamentDao {
        return database.tournamentDao()
    }
    
    @Provides
    fun provideTrackedPairDao(database: SuledDatabase): TrackedPairDao {
        return database.trackedPairDao()
    }
}
