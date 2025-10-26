package com.suled.app.data.models

import com.google.gson.annotations.SerializedName

data class Pair(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("displayName")
    val displayName: String,
    
    @SerializedName("player1")
    val player1: String,
    
    @SerializedName("player2")
    val player2: String
)

data class PairsResponse(
    @SerializedName("pairs")
    val pairs: List<Pair>,
    
    @SerializedName("totalPairs")
    val totalPairs: Int
)
