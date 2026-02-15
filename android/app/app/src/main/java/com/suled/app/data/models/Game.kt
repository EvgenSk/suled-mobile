package com.suled.app.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Represents a beach volleyball game between two pairs.
 */
data class Game(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("round")
    val round: Int,
    
    @SerializedName("courtNumber")
    val courtNumber: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("pair1")
    val pair1: String,
    
    @SerializedName("pair2")
    val pair2: String,
    
    @SerializedName("isOurGame")
    val isOurGame: Boolean,
    
    @SerializedName("scheduledTime")
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

data class GamesResponse(
    @SerializedName("pairId")
    val pairId: String,
    
    @SerializedName("games")
    val games: List<Game>,
    
    @SerializedName("totalGames")
    val totalGames: Int
)
