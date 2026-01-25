package com.suled.app.data.local.dao

import androidx.room.*
import com.suled.app.data.local.entity.TrackedPairEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tracked pairs
 */
@Dao
interface TrackedPairDao {
    
    @Query("SELECT * FROM tracked_pairs ORDER BY addedDate DESC")
    fun observeAllTrackedPairs(): Flow<List<TrackedPairEntity>>
    
    @Query("SELECT * FROM tracked_pairs")
    suspend fun getAllTrackedPairs(): List<TrackedPairEntity>
    
    @Query("SELECT * FROM tracked_pairs WHERE tournamentId = :tournamentId AND pairId = :pairId")
    suspend fun getTrackedPair(tournamentId: String, pairId: Int): TrackedPairEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM tracked_pairs WHERE tournamentId = :tournamentId AND pairId = :pairId)")
    suspend fun isTracked(tournamentId: String, pairId: Int): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM tracked_pairs WHERE tournamentId = :tournamentId AND pairId = :pairId)")
    fun observeIsTracked(tournamentId: String, pairId: Int): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackedPair(pair: TrackedPairEntity): Long
    
    @Delete
    suspend fun deleteTrackedPair(pair: TrackedPairEntity)
    
    @Query("DELETE FROM tracked_pairs WHERE tournamentId = :tournamentId AND pairId = :pairId")
    suspend fun deleteTrackedPair(tournamentId: String, pairId: Int)
    
    @Query("DELETE FROM tracked_pairs")
    suspend fun deleteAllTrackedPairs()
}
