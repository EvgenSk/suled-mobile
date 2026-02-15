package com.suled.app.data.repository

import com.suled.app.common.Constants
import com.suled.app.data.api.TournamentApiService
import com.suled.app.data.local.dao.TournamentDao
import com.suled.app.data.local.dao.TrackedPairDao
import com.suled.app.data.local.entity.TrackedPairEntity
import com.suled.app.data.local.entity.toDomain
import com.suled.app.data.local.entity.toEntity
import com.suled.app.data.models.Game
import com.suled.app.data.models.Pair
import com.suled.app.data.models.Tournament
import com.suled.app.data.models.TournamentDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for tournament data.
 * Implements offline-first pattern: tries local cache first, then network.
 */
@Singleton
class TournamentRepository @Inject constructor(
    private val apiService: TournamentApiService,
    private val tournamentDao: TournamentDao,
    private val trackedPairDao: TrackedPairDao
) : ITournamentRepository {

    /**
     * Observe tournaments from local database
     * Updates happen through refresh methods
     */
    override fun observeTournaments(): Flow<List<Tournament>> {
        return tournamentDao.observeAllTournaments().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Observe tournaments by status from local database
     */
    override fun observeTournamentsByStatus(status: String): Flow<List<Tournament>> {
        return tournamentDao.observeTournamentsByStatus(status).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Get tournament by ID from local database
     */
    override suspend fun getTournamentById(tournamentId: String): Tournament? = withContext(Dispatchers.IO) {
        tournamentDao.getTournamentById(tournamentId)?.toDomain()
    }
    
    /**
     * Refresh tournaments from network and update local cache
     */
    override suspend fun refreshTournaments(
        startDateFrom: String?,
        startDateTo: String?,
        location: String?,
        division: String?,
        status: String?,
        maxResults: Int?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTournaments(
                startDateFrom, startDateTo, location, division, status, maxResults
            )
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    // Extract tournaments from the ApiResponse.data field
                    val tournaments = apiResponse.data
                    if (tournaments.isEmpty()) {
                        Timber.i("No tournaments returned from API")
                    } else {
                        Timber.i("Received ${tournaments.size} tournaments from API")
                    }
                    // Save to local database
                    val entities = tournaments.map { it.toEntity() }
                    tournamentDao.insertTournaments(entities)
                    Result.success(Unit)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMsg = "Error: ${response.code()} - ${response.message()}"
                Timber.e(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception refreshing tournaments")
            Result.failure(e)
        }
    }

    override suspend fun getPairs(): Result<List<Pair>> = withContext(Dispatchers.IO) {
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

    /**
     * Get pairs for a specific tournament
     */
    override suspend fun getPairsForTournament(tournamentId: String): Result<List<Pair>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTournamentById(tournamentId)
            if (response.isSuccessful) {
                response.body()?.let { tournament ->
                    // Convert TournamentPair to Pair
                    val pairs = tournament.pairs.map { tournamentPair ->
                        Pair(
                            id = tournamentPair.id,
                            displayName = tournamentPair.displayName,
                            player1 = "", // Not available in detail response
                            player2 = "", // Not available in detail response
                            gameCount = tournamentPair.gameCount
                        )
                    }
                    Timber.i("Got ${pairs.size} pairs for tournament $tournamentId")
                    Result.success(pairs)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMsg = "Error: ${response.code()} - ${response.message()}"
                Timber.e(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception getting pairs for tournament")
            Result.failure(e)
        }
    }
    
    /**
     * Get tournament detail with full pair and game data
     */
    override suspend fun getTournamentDetail(tournamentId: String): Result<TournamentDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTournamentById(tournamentId)
            if (response.isSuccessful) {
                response.body()?.let { tournament ->
                    Timber.i("Got tournament detail with ${tournament.pairs.size} pairs")
                    Result.success(tournament)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMsg = "Error: ${response.code()} - ${response.message()}"
                Timber.e(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception getting tournament detail")
            Result.failure(e)
        }
    }

    override suspend fun getGamesForPair(pairId: String): Result<List<Game>> = withContext(Dispatchers.IO) {
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
    override fun observeTrackedPairs(): Flow<List<TrackedPairEntity>> {
        return trackedPairDao.observeAllTrackedPairs()
    }
    
    /**
     * Check if pair is tracked
     */
    override suspend fun isTracked(tournamentId: String, pairId: Int): Boolean {
        return trackedPairDao.isTracked(tournamentId, pairId)
    }
    
    /**
     * Observe if pair is tracked
     */
    override fun observeIsTracked(tournamentId: String, pairId: Int): Flow<Boolean> {
        return trackedPairDao.observeIsTracked(tournamentId, pairId)
    }
    
    /**
     * Track a pair
     */
    override suspend fun trackPair(
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
    override suspend fun untrackPair(tournamentId: String, pairId: Int): Result<Unit> = withContext(Dispatchers.IO) {
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
    override suspend fun clearOldCache() = withContext(Dispatchers.IO) {
        val cacheCutoff = System.currentTimeMillis() - Constants.Database.CACHE_TTL_MILLIS
        tournamentDao.deleteOldTournaments(cacheCutoff)
    }
}
