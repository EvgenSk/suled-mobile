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
     * Get all upcoming games for tracked pairs, sorted by round number.
     * Single-day tournaments only: round number is the natural time order.
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
                    val datePart = if (it.length > 10) it.substring(0, 10) else it
                    LocalDate.parse(datePart).atStartOfDayIn(TimeZone.currentSystemDefault())
                } catch (e: Exception) { null }
            }

            for (game in pair.games) {
                // Skip already-completed games
                if (game.status == 2) continue

                val round = tournament.rounds.find { it.roundNumber == game.round }

                // Best-effort scheduled time calculation; null = we don't know yet
                val scheduledInstant: Instant? = if (tournamentDate != null && round != null) {
                    try {
                        val parts = round.startTime.split(":")
                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        // Fractional seconds from C# TimeOnly ("00.0000000") – take integer part only
                        val s = parts.getOrNull(2)?.substringBefore('.')?.toIntOrNull() ?: 0
                        tournamentDate
                            .plus(h, DateTimeUnit.HOUR)
                            .plus(m, DateTimeUnit.MINUTE)
                            .plus(s, DateTimeUnit.SECOND)
                    } catch (e: Exception) { null }
                } else null

                // Skip games that started more than 10 minutes ago (not yet marked complete by backend)
                if (scheduledInstant != null && (now - scheduledInstant).inWholeMinutes > 10) continue

                upcomingGames.add(
                    UpcomingGame(
                        tournamentId = tournament.id,
                        tournamentName = tournament.name,
                        pairId = tracked.pairId,
                        pairDisplayName = tracked.pairDisplayName,
                        round = game.round,
                        courtNumber = game.courtNumber,
                        opponentPairName = game.opponentName,
                        scheduledTime = scheduledInstant?.toString() ?: "",
                        status = when (game.status) {
                            0 -> "Scheduled"
                            1 -> "InProgress"
                            else -> "Unknown"
                        }
                    )
                )
            }
        }

        // Sort by round number — reliable time order for single-day tournaments
        return upcomingGames.sortedBy { it.round }
    }

    /**
     * Get the next game to display on the watch.
     * Prefers the first game that has not yet started so the watch shows "Next"
     * rather than "Now" for an already-in-progress round.
     * Falls back to the in-progress game only when no future game is available.
     */
    fun getNextGame(): NextGameInfo? {
        val allGames = getUpcomingGames()
        if (allGames.isEmpty()) return null
        val now = Clock.System.now()

        // Prefer a game that hasn't started yet; fall back to in-progress if none.
        val gameToShow = allGames.firstOrNull { game ->
            game.scheduledTime.isEmpty() ||
                try { Instant.parse(game.scheduledTime) > now } catch (_: Exception) { false }
        } ?: allGames.first()

        val minutesUntilStart = if (gameToShow.scheduledTime.isNotEmpty()) {
            try {
                ((Instant.parse(gameToShow.scheduledTime) - now).inWholeSeconds / 60).toInt()
            } catch (e: Exception) { 0 }
        } else 0

        return NextGameInfo(
            tournamentName = gameToShow.tournamentName,
            pairDisplayName = gameToShow.pairDisplayName,
            round = gameToShow.round,
            courtNumber = gameToShow.courtNumber,
            opponentPairName = gameToShow.opponentPairName,
            scheduledTime = gameToShow.scheduledTime,
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
