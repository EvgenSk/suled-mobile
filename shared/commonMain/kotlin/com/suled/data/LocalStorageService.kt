package com.suled.data

import com.suled.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.datetime.*

/**
 * Common interface for local storage operations
 * Platform-specific implementations in androidMain and iosMain
 */
interface LocalStorageProvider {
    fun getString(key: String, defaultValue: String? = null): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun clear()
}

/**
 * Local storage service for tracking pairs and caching tournaments
 * Works on both phone and watch
 */
class LocalStorageService(private val provider: LocalStorageProvider) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = false
    }
    
    companion object {
        private const val TRACKED_PAIRS_KEY = "suled_tracked_pairs"
        private const val TOURNAMENTS_CACHE_PREFIX = "suled_tournament_"
    }
    
    /**
     * Get all tracked pairs
     */
    fun getTrackedPairs(): List<TrackedPair> {
        return try {
            val jsonString = provider.getString(TRACKED_PAIRS_KEY) ?: return emptyList()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Add a tracked pair
     */
    fun addTrackedPair(pair: TrackedPair) {
        val pairs = getTrackedPairs().toMutableList()
        
        // Check if already tracked
        val existing = pairs.find { 
            it.tournamentId == pair.tournamentId && it.pairId == pair.pairId 
        }
        
        if (existing == null) {
            pairs.add(pair)
            provider.putString(TRACKED_PAIRS_KEY, json.encodeToString(pairs))
        }
    }
    
    /**
     * Remove a tracked pair
     */
    fun removeTrackedPair(tournamentId: String, pairId: Int) {
        val pairs = getTrackedPairs().toMutableList()
        pairs.removeAll { it.tournamentId == tournamentId && it.pairId == pairId }
        provider.putString(TRACKED_PAIRS_KEY, json.encodeToString(pairs))
    }
    
    /**
     * Check if a pair is tracked
     */
    fun isTracked(tournamentId: String, pairId: Int): Boolean {
        return getTrackedPairs().any { 
            it.tournamentId == tournamentId && it.pairId == pairId 
        }
    }
    
    /**
     * Clear all tracked pairs
     */
    fun clearAllTrackedPairs() {
        provider.remove(TRACKED_PAIRS_KEY)
    }
    
    /**
     * Cache tournament data
     */
    fun cacheTournament(tournament: TournamentData) {
        try {
            val cached = CachedTournament(
                tournament = tournament,
                cachedAt = Clock.System.now().toString()
            )
            val key = "$TOURNAMENTS_CACHE_PREFIX${tournament.id}"
            provider.putString(key, json.encodeToString(cached))
        } catch (e: Exception) {
            // Ignore cache errors
        }
    }
    
    /**
     * Get cached tournament
     */
    fun getCachedTournament(tournamentId: String): TournamentData? {
        return try {
            val key = "$TOURNAMENTS_CACHE_PREFIX$tournamentId"
            val jsonString = provider.getString(key) ?: return null
            val cached: CachedTournament = json.decodeFromString(jsonString)
            
            // Check if cache is still valid (24 hours)
            val cachedAt = Instant.parse(cached.cachedAt)
            val now = Clock.System.now()
            val hoursSinceCached = (now - cachedAt).inWholeHours
            
            if (hoursSinceCached > 24) {
                // Cache expired
                provider.remove(key)
                return null
            }
            
            cached.tournament
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get all upcoming games for tracked pairs
     */
    fun getUpcomingGames(): List<UpcomingGame> {
        val trackedPairs = getTrackedPairs()
        val upcomingGames = mutableListOf<UpcomingGame>()
        val now = Clock.System.now()
        
        for (tracked in trackedPairs) {
            val tournament = getCachedTournament(tracked.tournamentId) ?: continue
            val pair = tournament.pairs.find { it.id == tracked.pairId } ?: continue
            
            val tournamentDate = tournament.startDate?.let { 
                try {
                    LocalDate.parse(it).atStartOfDayIn(TimeZone.currentSystemDefault())
                } catch (e: Exception) {
                    null
                }
            } ?: continue
            
            // Process each game for this pair
            for (game in pair.games) {
                val round = tournament.rounds.find { it.roundNumber == game.round } ?: continue
                
                // Parse round start time (format: HH:mm:ss)
                val timeParts = round.startTime.split(":")
                if (timeParts.size < 2) continue
                
                val hours = timeParts[0].toIntOrNull() ?: continue
                val minutes = timeParts[1].toIntOrNull() ?: continue
                val seconds = if (timeParts.size > 2) timeParts[2].toIntOrNull() ?: 0 else 0
                
                // Calculate game time
                val gameTime = tournamentDate.plus(hours, DateTimeUnit.HOUR)
                    .plus(minutes, DateTimeUnit.MINUTE)
                    .plus(seconds, DateTimeUnit.SECOND)
                
                // Only include upcoming games (within next 24 hours)
                val duration = gameTime - now
                if (duration.inWholeHours in 0..24) {
                    upcomingGames.add(
                        UpcomingGame(
                            tournamentId = tournament.id,
                            tournamentName = tournament.name,
                            pairId = tracked.pairId,
                            pairDisplayName = tracked.pairDisplayName,
                            round = game.round,
                            courtNumber = game.courtNumber,
                            opponentPairName = game.opponentName,
                            scheduledTime = gameTime.toString(),
                            status = when (game.status) {
                                0 -> "Scheduled"
                                1 -> "InProgress"
                                2 -> "Completed"
                                else -> "Unknown"
                            }
                        )
                    )
                }
            }
        }
        
        // Sort by scheduled time
        return upcomingGames.sortedBy { 
            try {
                Instant.parse(it.scheduledTime)
            } catch (e: Exception) {
                Instant.DISTANT_FUTURE
            }
        }
    }
    
    /**
     * Get the next game (soonest upcoming game)
     */
    fun getNextGame(): NextGameInfo? {
        val upcomingGames = getUpcomingGames()
        if (upcomingGames.isEmpty()) return null
        
        val nextGame = upcomingGames.first()
        val now = Clock.System.now()
        
        val scheduledTime = try {
            Instant.parse(nextGame.scheduledTime)
        } catch (e: Exception) {
            return null
        }
        
        val minutesUntilStart = ((scheduledTime - now).inWholeSeconds / 60).toInt()
        
        return NextGameInfo(
            tournamentName = nextGame.tournamentName,
            pairDisplayName = nextGame.pairDisplayName,
            round = nextGame.round,
            courtNumber = nextGame.courtNumber,
            opponentPairName = nextGame.opponentPairName,
            scheduledTime = nextGame.scheduledTime,
            minutesUntilStart = minutesUntilStart
        )
    }
    
    /**
     * Clear old cached tournaments (older than 24 hours)
     */
    fun clearOldCache() {
        // This would require listing all keys, which is platform-specific
        // For now, we handle expiration on read
    }
}
