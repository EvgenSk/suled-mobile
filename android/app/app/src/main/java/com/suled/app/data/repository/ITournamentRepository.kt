package com.suled.app.data.repository

import com.suled.app.data.local.entity.TrackedPairEntity
import com.suled.app.data.models.Game
import com.suled.app.data.models.Pair
import com.suled.app.data.models.Tournament
import com.suled.app.data.models.TournamentDetail
import kotlinx.coroutines.flow.Flow

/**
 * Interface for tournament data repository.
 * Defines contract for tournament data access.
 * Implementations should use offline-first pattern: local cache first, then network.
 */
interface ITournamentRepository {
    
    // ========== Tournament Operations ==========
    
    /**
     * Observe all tournaments from local database.
     * Updates happen through refresh methods.
     * @return Flow emitting list of tournaments as they change
     */
    fun observeTournaments(): Flow<List<Tournament>>
    
    /**
     * Observe tournaments filtered by status from local database.
     * @param status Tournament status to filter by (e.g., "Upcoming", "InProgress", "Completed")
     * @return Flow emitting filtered list of tournaments
     */
    fun observeTournamentsByStatus(status: String): Flow<List<Tournament>>
    
    /**
     * Get tournament by ID from local database.
     * @param tournamentId Unique tournament identifier
     * @return Tournament if found, null otherwise
     */
    suspend fun getTournamentById(tournamentId: String): Tournament?
    
    /**
     * Refresh tournaments from network and update local cache.
     * @param startDateFrom Filter by start date from (ISO format)
     * @param startDateTo Filter by start date to (ISO format)
     * @param location Filter by location
     * @param division Filter by division
     * @param status Filter by status (default: "Upcoming")
     * @param maxResults Maximum number of results (default: 100)
     * @return Result containing Unit on success or exception on failure
     */
    suspend fun refreshTournaments(
        startDateFrom: String? = null,
        startDateTo: String? = null,
        location: String? = null,
        division: String? = null,
        status: String? = null,
        maxResults: Int? = 100
    ): Result<Unit>
    
    /**
     * Get tournament detail with full pair and game data.
     * @param tournamentId Unique tournament identifier
     * @return Result containing TournamentDetail on success or exception on failure
     */
    suspend fun getTournamentDetail(tournamentId: String): Result<TournamentDetail>
    
    // ========== Pair Operations ==========
    
    /**
     * Get all pairs from network.
     * @return Result containing list of pairs on success or exception on failure
     */
    suspend fun getPairs(): Result<List<Pair>>
    
    /**
     * Get pairs for a specific tournament.
     * @param tournamentId Unique tournament identifier
     * @return Result containing list of pairs on success or exception on failure
     */
    suspend fun getPairsForTournament(tournamentId: String): Result<List<Pair>>
    
    // ========== Game Operations ==========
    
    /**
     * Get games for a specific pair.
     * @param pairId Unique pair identifier
     * @return Result containing list of games on success or exception on failure
     */
    suspend fun getGamesForPair(pairId: String): Result<List<Game>>
    
    // ========== Tracked Pairs Operations ==========
    
    /**
     * Observe all tracked pairs.
     * @return Flow emitting list of tracked pairs as they change
     */
    fun observeTrackedPairs(): Flow<List<TrackedPairEntity>>
    
    /**
     * Check if a pair is currently tracked.
     * @param tournamentId Unique tournament identifier
     * @param pairId Pair identifier within tournament
     * @return true if tracked, false otherwise
     */
    suspend fun isTracked(tournamentId: String, pairId: Int): Boolean
    
    /**
     * Observe tracked status of a specific pair.
     * @param tournamentId Unique tournament identifier
     * @param pairId Pair identifier within tournament
     * @return Flow emitting tracked status as it changes
     */
    fun observeIsTracked(tournamentId: String, pairId: Int): Flow<Boolean>
    
    /**
     * Start tracking a pair for updates.
     * @param tournamentId Unique tournament identifier
     * @param tournamentName Human-readable tournament name
     * @param pairId Pair identifier within tournament
     * @param pairDisplayName Human-readable pair name
     * @return Result containing Unit on success or exception on failure
     */
    suspend fun trackPair(
        tournamentId: String,
        tournamentName: String,
        pairId: Int,
        pairDisplayName: String
    ): Result<Unit>
    
    /**
     * Stop tracking a pair.
     * @param tournamentId Unique tournament identifier
     * @param pairId Pair identifier within tournament
     * @return Result containing Unit on success or exception on failure
     */
    suspend fun untrackPair(tournamentId: String, pairId: Int): Result<Unit>
    
    // ========== Cache Management ==========
    
    /**
     * Clear old cached tournaments (older than 7 days).
     */
    suspend fun clearOldCache()
}
