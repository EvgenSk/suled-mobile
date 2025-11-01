package com.suled.app.data.models

import com.google.gson.annotations.SerializedName

data class Tournament(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String?,
    
    @SerializedName("location")
    val location: String?,
    
    @SerializedName("division")
    val division: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("gameCount")
    val gameCount: Int,
    
    @SerializedName("createdDate")
    val createdDate: String
)

data class TournamentsResponse(
    @SerializedName("tournaments")
    val tournaments: List<Tournament>,
    
    @SerializedName("totalCount")
    val totalCount: Int
)
