package com.suled.app.helpers

import com.suled.app.data.models.Game
import com.suled.app.data.models.GamesResponse
import com.suled.app.data.models.Pair
import com.suled.app.data.models.PairsResponse
import com.suled.app.data.models.Tournament
import com.suled.app.data.models.TournamentsResponse

/**
 * Test data factory for creating mock objects in tests
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
    
    fun createPairsResponse(
        pairs: List<Pair> = createPairs(),
        totalPairs: Int = pairs.size
    ) = PairsResponse(
        pairs = pairs,
        totalPairs = totalPairs
    )
    
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
    
    fun createGamesResponse(
        pairId: String = "pair-1",
        games: List<Game> = createGames(),
        totalGames: Int = games.size
    ) = GamesResponse(
        pairId = pairId,
        games = games,
        totalGames = totalGames
    )
    
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
    
    fun createTournamentsResponse(
        tournaments: List<Tournament> = createTournaments()
    ) = TournamentsResponse(
        data = tournaments,
        success = true,
        message = "Retrieved ${tournaments.size} tournament(s)",
        timestamp = "2026-02-12T10:00:00Z"
    )
    
    // JSON response strings for MockWebServer
    object Json {
        fun pairsResponse(pairs: List<Pair> = createPairs()) = """
            {
                "pairs": [
                    ${pairs.joinToString(",\n") { pair ->
            """
                    {
                        "id": "${pair.id}",
                        "displayName": "${pair.displayName}",
                        "player1": "${pair.player1}",
                        "player2": "${pair.player2}",
                        "gameCount": ${pair.gameCount}
                    }
                    """.trimIndent()
        }}
                ],
                "totalPairs": ${pairs.size}
            }
        """.trimIndent()
        
        fun gamesResponse(
            pairId: String = "pair-1",
            games: List<Game> = createGames()
        ) = """
            {
                "pairId": "$pairId",
                "games": [
                    ${games.joinToString(",\n") { game ->
            """
                    {
                        "id": "${game.id}",
                        "round": ${game.round},
                        "courtNumber": ${game.courtNumber},
                        "status": "${game.status}",
                        "pair1": "${game.pair1}",
                        "pair2": "${game.pair2}",
                        "isOurGame": ${game.isOurGame},
                        "scheduledTime": ${game.scheduledTime?.let { "\"$it\"" } ?: "null"}
                    }
                    """.trimIndent()
        }}
                ],
                "totalGames": ${games.size}
            }
        """.trimIndent()
        
        fun tournamentsResponse(tournaments: List<Tournament> = createTournaments()) = """
            {
                "data": [
                    ${tournaments.joinToString(",\n") { tournament ->
            """
                    {
                        "id": "${tournament.id}",
                        "name": "${tournament.name}",
                        "startDate": ${tournament.startDate?.let { "\"$it\"" } ?: "null"},
                        "endDate": ${tournament.endDate?.let { "\"$it\"" } ?: "null"},
                        "location": "${tournament.location}",
                        "division": "${tournament.division}",
                        "description": "${tournament.description}",
                        "status": "${tournament.status}",
                        "gameCount": ${tournament.gameCount},
                        "createdDate": "${tournament.createdDate}"
                    }
                    """.trimIndent()
        }}
                ],
                "success": true,
                "message": "Retrieved ${tournaments.size} tournament(s)",
                "timestamp": "2026-02-12T10:00:00Z"
            }
        """.trimIndent()
        
        const val errorResponse = """
            {
                "error": "Internal server error",
                "message": "Something went wrong"
            }
        """
    }
}
