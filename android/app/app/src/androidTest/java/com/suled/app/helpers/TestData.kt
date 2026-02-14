package com.suled.app.helpers

import com.suled.app.data.models.Game
import com.suled.app.data.models.Pair
import com.suled.app.data.models.Tournament

/**
 * Test data factory for Android instrumented tests
 */
object TestData {
    
    fun createPair(
        id: String = "pair-1",
        displayName: String = "Team A",
        player1: String = "John Doe",
        player2: String = "Jane Smith",
        gameCount: Int = 5
    ) = Pair(
        id = id,
        displayName = displayName,
        player1 = player1,
        player2 = player2,
        gameCount = gameCount
    )
    
    fun createPairs(count: Int = 3): List<Pair> {
        return (1..count).map { index ->
            createPair(
                id = "pair-$index",
                displayName = "Team ${('A'.code + index - 1).toChar()}",
                player1 = "Player ${index * 2 - 1}",
                player2 = "Player ${index * 2}",
                gameCount = 5 + index
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
    
    fun createTournament(
        id: String = "tournament-1",
        name: String = "Summer Championship 2025",
        startDate: String? = "2025-06-15",
        endDate: String? = "2025-06-17",
        location: String = "Central Arena",
        division: String = "Division A",
        description: String = "Annual summer tournament",
        status: String = "Upcoming",
        gameCount: Int = 24,
        createdDate: String = "2025-05-01T10:00:00Z"
    ) = Tournament(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
        location = location,
        division = division,
        description = description,
        status = status,
        gameCount = gameCount,
        createdDate = createdDate
    )
    
    fun createTournaments(count: Int = 3): List<Tournament> {
        return (1..count).map { index ->
            createTournament(
                id = "tournament-$index",
                name = "Tournament $index",
                startDate = "2025-0${5 + index}-15",
                location = "Arena $index",
                division = "Division ${('A'.code + index - 1).toChar()}",
                gameCount = 20 + index * 4
            )
        }
    }
}
