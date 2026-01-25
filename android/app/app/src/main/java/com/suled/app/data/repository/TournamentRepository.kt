package com.suled.app.data.repository

import com.suled.app.data.api.TournamentApiService
import com.suled.app.data.local.dao.TournamentDao
import com.suled.app.data.local.dao.TrackedPairDao
import com.suled.app.data.local.entity.TrackedPairEntity
import com.suled.app.data.local.entity.toDomain
import com.suled.app.data.local.entity.toEntity
import com.suled.app.data.models.Game
import com.suled.app.data.models.Pair
import com.suled.app.data.models.Tournament
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for tournament data
 * Implements offline-first pattern: tries local cache first, then network
 */
@Singleton
class TournamentRepository @Inject constructor(
    private val apiService: TournamentApiService,
    private val tournamentDao: TournamentDao,
    private val trackedPairDao: TrackedPairDao
) {

    /**
     * Observe tournaments from local database
     * Updates happen through refresh methods
     */
    fun observeTournaments(): Flow<List<Tournament>> {
        return tournamentDao.observeAllTournaments().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Observe tournaments by status from local database
     */
    fun observeTournamentsByStatus(status: String): Flow<List<Tournament>> {
        return tournamentDao.observeTournamentsByStatus(status).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Get tournament by ID from local database
     */
    suspend fun getTournamentById(tournamentId: String): Tournament? = withContext(Dispatchers.IO) {
        tournamentDao.getTournamentById(tournamentId)?.toDomain()
    }
    
    /**
     * Refresh tournaments from network and update local cache
     */
    suspend fun refreshTournaments(
        startDateFrom: String? = null,
        startDateTo: String? = null,
        location: String? = null,
        division: String? = null,
        status: String? = "Scheduled",
        maxResults: Int? = 100
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTournaments(
                startDateFrom, startDateTo, location, division, status, maxResults
            )
            if (response.isSuccessful) {
                response.body()?.let { tournamentResponse ->
                    // Save to local database
                    val entities = tournamentResponse.tournaments.map { it.toEntity() }
                    tournamentDao.insertTournaments(entities)
                    Result.success(Unit)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPairs(): Result<List<Pair>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPairs()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.pairs)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGamesForPair(pairId: String): Result<List<Game>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGamesForPair(pairId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.games)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== Tracked Pairs ==========
    
    /**
     * Observe all tracked pairs
     */
    fun observeTrackedPairs(): Flow<List<TrackedPairEntity>> {
        return trackedPairDao.observeAllTrackedPairs()
    }
    
    /**
     * Check if pair is tracked
     */
    suspend fun isTracked(tournamentId: String, pairId: Int): Boolean {
        return trackedPairDao.isTracked(tournamentId, pairId)
    }
    
    /**
     * Observe if pair is tracked
     */
    fun observeIsTracked(tournamentId: String, pairId: Int): Flow<Boolean> {
        return trackedPairDao.observeIsTracked(tournamentId, pairId)
    }
    
    /**
     * Track a pair
     */
    suspend fun trackPair(
        tournamentId: String,
        tournamentName: String,
        pairId: Int,
        pairDisplayName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = TrackedPairEntity(
                tournamentId = tournamentId,
                tournamentName = tournamentName,
                pairId = pairId,
                pairDisplayName = pairDisplayName
            )
            trackedPairDao.insertTrackedPair(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Untrack a pair
     */
    suspend fun untrackPair(tournamentId: String, pairId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            trackedPairDao.deleteTrackedPair(tournamentId, pairId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear old cached tournaments (older than 7 days)
     */
    suspend fun clearOldCache() = withContext(Dispatchers.IO) {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        tournamentDao.deleteOldTournaments(sevenDaysAgo)
    }
}
