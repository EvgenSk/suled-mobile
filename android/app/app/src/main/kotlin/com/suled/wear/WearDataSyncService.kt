package com.suled.wear

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.suled.data.createLocalStorageService
import com.suled.models.TrackedPair
import com.suled.models.TournamentData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Service to sync tracked pairs and tournament data from phone to watch
 * Uses Wear OS Data Layer API
 */
class WearDataSyncService(private val context: Context) {
    
    private val dataClient: DataClient by lazy {
        Wearable.getDataClient(context)
    }
    
    private val localStorage by lazy {
        createLocalStorageService(context)
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = false
    }
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val TRACKED_PAIRS_PATH = "/suled/tracked_pairs"
        private const val TOURNAMENT_PATH_PREFIX = "/suled/tournament/"
    }
    
    /**
     * Sync all tracked pairs to watch
     * Call this whenever tracked pairs change
     */
    fun syncTrackedPairs() {
        scope.launch {
            try {
                val pairs = localStorage.getTrackedPairs()
                val pairsJson = json.encodeToString(pairs)
                
                val putDataReq = PutDataMapRequest.create(TRACKED_PAIRS_PATH).apply {
                    dataMap.putString("pairs", pairsJson)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }.asPutDataRequest()
                    .setUrgent() // High priority for immediate sync
                
                Tasks.await(dataClient.putDataItem(putDataReq))
                
                // Also sync tournaments for these pairs
                syncTournamentsForTrackedPairs(pairs)
                
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Sync a specific tournament to watch
     */
    fun syncTournament(tournament: TournamentData) {
        scope.launch {
            try {
                // First cache it locally
                localStorage.cacheTournament(tournament)
                
                // Then sync to watch
                val tournamentJson = json.encodeToString(tournament)
                val path = "$TOURNAMENT_PATH_PREFIX${tournament.id}"
                
                val putDataReq = PutDataMapRequest.create(path).apply {
                    dataMap.putString("tournament", tournamentJson)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }.asPutDataRequest()
                    .setUrgent()
                
                Tasks.await(dataClient.putDataItem(putDataReq))
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Sync tournaments for all tracked pairs
     */
    private suspend fun syncTournamentsForTrackedPairs(pairs: List<TrackedPair>) {
        try {
            // Get unique tournament IDs
            val tournamentIds = pairs.map { it.tournamentId }.distinct()
            
            // Sync each tournament
            for (tournamentId in tournamentIds) {
                val tournament = localStorage.getCachedTournament(tournamentId)
                if (tournament != null) {
                    syncTournament(tournament)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Initialize sync - call when app starts
     */
    fun initializeSync() {
        scope.launch {
            try {
                // Sync current state to watch
                syncTrackedPairs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Check if watch is connected
     */
    suspend fun isWatchConnected(): Boolean {
        return try {
            val nodes = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
            nodes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
