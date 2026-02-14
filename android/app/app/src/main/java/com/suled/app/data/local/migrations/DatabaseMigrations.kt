package com.suled.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for SuledDatabase
 * 
 * Migration Strategy:
 * 1. Always test migrations with real data before deploying
 * 2. Export schema to schemas/ directory for each version
 * 3. Write migration tests using MigrationTestHelper
 * 4. Document breaking changes in release notes
 * 
 * Current Version: 1
 * 
 * Version History:
 * - v1: Initial schema with tournaments and tracked pairs
 */
object DatabaseMigrations {
    
    /**
     * Example migration from version 1 to 2
     * 
     * Usage in DatabaseModule:
     * ```
     * Room.databaseBuilder(...)
     *     .addMigrations(MIGRATION_1_2)
     *     .build()
     * ```
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add a new column to tournaments table
            // database.execSQL("ALTER TABLE tournaments ADD COLUMN new_field TEXT")
            
            // Example: Create a new table
            // database.execSQL("""
            //     CREATE TABLE IF NOT EXISTS new_table (
            //         id TEXT PRIMARY KEY NOT NULL,
            //         name TEXT NOT NULL,
            //         created_at INTEGER NOT NULL
            //     )
            // """.trimIndent())
            
            // Example: Create index for performance
            // database.execSQL("CREATE INDEX IF NOT EXISTS index_tournaments_status ON tournaments(status)")
        }
    }
    
    /**
     * Example migration from version 2 to 3
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Migration logic here
        }
    }
    
    /**
     * Destructive migration fallback
     * Only use during development or when data loss is acceptable
     * In production, prefer writing proper migrations
     */
    fun fallbackToDestructiveMigration() {
        // This is configured in DatabaseModule
        // Room.databaseBuilder(...).fallbackToDestructiveMigration()
    }
}
