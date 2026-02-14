package com.suled.app.data.models

import com.google.gson.annotations.SerializedName

data class Tournament(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("startDate")
    val startDate: String?,
    
    @SerializedName("endDate")
    val endDate: String?,
    
    @SerializedName("location")
    val location: String = "",
    
    @SerializedName("division")
    val division: String = "",
    
    @SerializedName("description")
    val description: String = "",
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("gameCount")
    val gameCount: Int,
    
    @SerializedName("createdDate")
    val createdDate: String
)

data class TournamentRound(
    @SerializedName("roundNumber")
    val roundNumber: Int,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String,
    
    @SerializedName("gameCount")
    val gameCount: Int
)

data class TournamentDetail(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("startDate")
    val startDate: String?,
    
    @SerializedName("endDate")
    val endDate: String?,
    
    @SerializedName("location")
    val location: String = "",
    
    @SerializedName("division")
    val division: String = "",
    
    @SerializedName("description")
    val description: String = "",
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("createdDate")
    val createdDate: String,
    
    @SerializedName("rounds")
    val rounds: List<TournamentRound> = emptyList(),
    
    @SerializedName("pairs")
    val pairs: List<TournamentPair> = emptyList()
)

data class TournamentPair(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("displayName")
    val displayName: String,
    
    @SerializedName("gameCount")
    val gameCount: Int,
    
    @SerializedName("games")
    val games: List<PairGame> = emptyList()
)

data class PairGame(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("tournamentId")
    val tournamentId: String,
    
    @SerializedName("round")
    val round: Int,
    
    @SerializedName("courtNumber")
    val courtNumber: Int,
    
    @SerializedName("opponentPair")
    val opponentPair: OpponentPairInfo,
    
    @SerializedName("status")
    val status: Int
)

data class OpponentPairInfo(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("displayName")
    val displayName: String
)

// API Response wrapper that matches backend's ApiResponse<T> format
data class ApiResponse<T>(
    @SerializedName("data")
    val data: T,
    
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: String? = null
)

// Type aliases for specific API responses
typealias TournamentsResponse = ApiResponse<List<Tournament>>
