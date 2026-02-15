package com.suled.app.di

import android.content.Context
import androidx.room.Room
import com.suled.app.BuildConfig
import com.suled.app.common.Constants
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
 * 
 * Migration Strategy:
 * - Debug builds: Use fallbackToDestructiveMigration for faster development
 * - Release builds: Use .addMigrations() with proper Migration objects
 * 
 * When adding migrations:
 * 1. Increment database version in SuledDatabase
 * 2. Add Migration object in DatabaseMigrations
 * 3. Add migration to provideDatabase() below
 * 4. Test with MigrationTestHelper
 * 5. Export schema with ./gradlew kaptDebugKotlin
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
            Constants.Database.DATABASE_NAME
        )
            // Add migrations here as database evolves:
            // .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            // .addMigrations(DatabaseMigrations.MIGRATION_2_3)
            
            // For development, allow destructive migration
            // For production, remove this and use proper migrations above
            .apply {
                if (BuildConfig.DEBUG) {
                    fallbackToDestructiveMigration()
                }
            }
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
