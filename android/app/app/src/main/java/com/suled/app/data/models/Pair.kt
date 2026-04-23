package com.suled.app.data.models

import kotlinx.serialization.Serializable

/**
 * Represents a pair (team) of two players in a tournament.
 */
@Serializable
data class Pair(
    val id: String,
    val displayName: String,
    val player1: String,
    val player2: String,
    val gameCount: Int = 0
)

@Serializable
data class PairsResponse(
    val pairs: List<Pair>,
    val totalPairs: Int
)
