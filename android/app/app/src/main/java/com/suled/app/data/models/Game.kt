package com.suled.app.data.models

import com.google.gson.annotations.SerializedName

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
)

data class GamesResponse(
    @SerializedName("pairId")
    val pairId: String,
    
    @SerializedName("games")
    val games: List<Game>,
    
    @SerializedName("totalGames")
    val totalGames: Int
)
