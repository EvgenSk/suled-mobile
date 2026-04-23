package com.suled.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.suled.app.data.local.dao.TournamentDao
import com.suled.app.data.local.dao.TrackedPairDao
import com.suled.app.data.local.entity.TournamentEntity
import com.suled.app.data.local.entity.TrackedPairEntity

/**
 * Room database for Suled app
 * Provides offline-first data persistence
 * 
 * Version History:
 * - Version 1: Initial schema with TournamentEntity and TrackedPairEntity
 * - Version 2: Added startTime to TournamentEntity
 */
@Database(
    entities = [
        TournamentEntity::class,
        TrackedPairEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class SuledDatabase : RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao
    abstract fun trackedPairDao(): TrackedPairDao
}
