package com.suled.app.data.models

import kotlinx.serialization.Serializable

/**
 * Tournament data model representing a beach volleyball tournament.
 * Contains basic tournament information without detailed pair/game data.
 * For full tournament details including pairs and games, use [TournamentDetail].
 */
@Serializable
data class Tournament(
    val id: String,
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val location: String = "",
    val division: String = "",
    val description: String = "",
    val status: String,
    val startTime: String? = null,
    val gameCount: Int,
    val createdDate: String
)

/**
 * Represents a tournament round with timing and game information.
 */
@Serializable
data class TournamentRound(
    val roundNumber: Int,
    val startTime: String,
    val endTime: String,
    val gameCount: Int
)

@Serializable
data class TournamentDetail(
    val id: String,
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val location: String = "",
    val division: String = "",
    val description: String = "",
    val status: String,
    val createdDate: String,
    val rounds: List<TournamentRound> = emptyList(),
    val pairs: List<TournamentPair> = emptyList()
)

@Serializable
data class TournamentPair(
    val id: String,
    val displayName: String,
    val gameCount: Int,
    val games: List<PairGame> = emptyList()
)

@Serializable
data class PairGame(
    val id: String,
    val tournamentId: String,
    val round: Int,
    val courtNumber: Int,
    val opponentPair: OpponentPairInfo,
    val status: String  // Backend sends: "Scheduled", "InProgress", "Completed", "Cancelled"
)

@Serializable
data class OpponentPairInfo(
    val id: String,
    val displayName: String
)

// API Response wrapper that matches backend's ApiResponse<T> format
@Serializable
data class ApiResponse<T>(
    val data: T,
    val success: Boolean,
    val message: String? = null,
    val timestamp: String? = null
)

// Type aliases for specific API responses
typealias TournamentsResponse = ApiResponse<List<Tournament>>
