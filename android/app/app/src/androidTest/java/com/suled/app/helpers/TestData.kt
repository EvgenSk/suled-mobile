package com.suled.app.helpers

import com.suled.app.data.models.Game
import com.suled.app.data.models.Pair

/**
 * Test data factory for Android instrumented tests
 */
object TestData {
    
    fun createPair(
        id: String = "pair-1",
        displayName: String = "Team A",
        player1: String = "John Doe",
        player2: String = "Jane Smith"
    ) = Pair(
        id = id,
        displayName = displayName,
        player1 = player1,
        player2 = player2
    )
    
    fun createPairs(count: Int = 3): List<Pair> {
        return (1..count).map { index ->
            createPair(
                id = "pair-$index",
                displayName = "Team ${('A'.code + index - 1).toChar()}",
                player1 = "Player ${index * 2 - 1}",
                player2 = "Player ${index * 2}"
            )
        }
    }
    
    fun createGame(
        id: String = "game-1",
        round: Int = 1,
        courtNumber: Int = 1,
        status: String = "scheduled",
        pair1: String = "Team A",
        pair2: String = "Team B",
        isOurGame: Boolean = true,
        scheduledTime: String? = "10:00"
    ) = Game(
        id = id,
        round = round,
        courtNumber = courtNumber,
        status = status,
        pair1 = pair1,
        pair2 = pair2,
        isOurGame = isOurGame,
        scheduledTime = scheduledTime
    )
    
    fun createGames(count: Int = 5, ourGames: Int = 2): List<Game> {
        return (1..count).map { index ->
            createGame(
                id = "game-$index",
                round = (index + 1) / 2,
                courtNumber = index,
                pair1 = "Team ${('A'.code + index - 1).toChar()}",
                pair2 = "Team ${('A'.code + index).toChar()}",
                isOurGame = index <= ourGames
            )
        }
    }
}
