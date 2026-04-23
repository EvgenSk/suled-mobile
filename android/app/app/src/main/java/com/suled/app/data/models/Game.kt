package com.suled.app.data.models

import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Represents a beach volleyball game between two pairs.
 */
@Serializable
data class Game(
    val id: String,
    val round: Int,
    val courtNumber: Int,
    val status: String,
    val pair1: String,
    val pair2: String,
    val isOurGame: Boolean,
    val scheduledTime: String? = null
) {
    /**
     * Get formatted time string from tournament rounds
     * @param rounds List of tournament rounds
     * @return Formatted time string (e.g., "9:00 AM - 9:15 AM") or "TBD"
     */
    fun getTimeFromRounds(rounds: List<TournamentRound>?): String {
        if (rounds.isNullOrEmpty()) return "TBD"

        val tournamentRound = rounds.firstOrNull { it.roundNumber == round }
            ?: return "TBD"

        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val displayFormatter = DateTimeFormatter.ofPattern("h:mm a")

            val startTime = LocalTime.parse(tournamentRound.startTime, formatter)
            val endTime = LocalTime.parse(tournamentRound.endTime, formatter)

            "${startTime.format(displayFormatter)} - ${endTime.format(displayFormatter)}"
        } catch (e: Exception) {
            "TBD"
        }
    }
}

@Serializable
data class GamesResponse(
    val pairId: String,
    val games: List<Game>,
    val totalGames: Int
)
