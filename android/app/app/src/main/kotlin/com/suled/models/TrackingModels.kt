package com.suled.models

import kotlinx.serialization.Serializable

/**
 * Represents a tracked pair in a tournament
 * Stored locally on device (phone and watch)
 */
@Serializable
data class TrackedPair(
    val tournamentId: String,
    val tournamentName: String,
    val pairId: Int,
    val pairDisplayName: String,
    val addedDate: String // ISO 8601 format
)

/**
 * Information about the next upcoming game
 * Calculated from tracked pairs and cached tournaments
 */
@Serializable
data class NextGameInfo(
    val tournamentName: String,
    val pairDisplayName: String,
    val round: Int,
    val courtNumber: Int,
    val opponentPairName: String,
    val scheduledTime: String, // ISO 8601 format
    val minutesUntilStart: Int
)

/**
 * Upcoming game details
 */
@Serializable
data class UpcomingGame(
    val tournamentId: String,
    val tournamentName: String,
    val pairId: Int,
    val pairDisplayName: String,
    val round: Int,
    val courtNumber: Int,
    val opponentPairName: String,
    val scheduledTime: String, // ISO 8601 format
    val status: String
)

/**
 * Cached tournament data
 */
@Serializable
data class CachedTournament(
    val tournament: TournamentData,
    val cachedAt: String // ISO 8601 format
)

/**
 * Simplified tournament data for local storage
 */
@Serializable
data class TournamentData(
    val id: String,
    val name: String,
    val startDate: String?, // ISO 8601 format
    val rounds: List<RoundData>,
    val pairs: List<PairData>
)

/**
 * Round information
 */
@Serializable
data class RoundData(
    val roundNumber: Int,
    val startTime: String, // HH:mm:ss format
    val endTime: String,
    val gameCount: Int
)

/**
 * Pair information with games
 */
@Serializable
data class PairData(
    val id: Int,
    val displayName: String,
    val player1Name: String,
    val player2Name: String,
    val games: List<GameData>
)

/**
 * Game information (compact format)
 */
@Serializable
data class GameData(
    val round: Int,
    val courtNumber: Int,
    val opponentId: Int,
    val opponentName: String,
    val status: Int = 0 // 0 = Scheduled, 1 = InProgress, 2 = Completed
)
