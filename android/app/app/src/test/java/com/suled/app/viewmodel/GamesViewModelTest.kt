package com.suled.app.viewmodel

import com.suled.app.data.models.OpponentPairInfo
import com.suled.app.data.models.PairGame
import com.suled.app.data.models.TournamentDetail
import com.suled.app.data.models.TournamentPair
import com.suled.app.data.repository.TournamentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() {
        // Given
        val repository = mockk<TournamentRepository>()
        val viewModel = GamesViewModel(repository)

        // Then
        assertTrue(viewModel.uiState.value.games.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertEquals("", viewModel.uiState.value.selectedPairName)
    }

    @Test
    fun `loadGames updates state with games on success`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>()
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        val tournament = TournamentDetail(
            id = tournamentId,
            name = "Tournament 1",
            startDate = "2024-01-01",
            endDate = "2024-01-02",
            status = "Upcoming",
            createdDate = "2023-12-01",
            pairs = listOf(
                TournamentPair(
                    id = pairId,
                    displayName = pairName,
                    gameCount = 2,
                    games = listOf(
                        PairGame(
                            id = "game-1",
                            tournamentId = tournamentId,
                            round = 1,
                            courtNumber = 1,
                            opponentPair = OpponentPairInfo(id = "opp-1", displayName = "Team B"),
                            status = 0
                        ),
                        PairGame(
                            id = "game-2",
                            tournamentId = tournamentId,
                            round = 2,
                            courtNumber = 2,
                            opponentPair = OpponentPairInfo(id = "opp-2", displayName = "Team C"),
                            status = 0
                        )
                    )
                )
            )
        )
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.success(tournament)
        val viewModel = GamesViewModel(repository)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.games.size)
        assertEquals(pairName, state.selectedPairName)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadGames sets loading state correctly`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>()
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        val tournament = TournamentDetail(
            id = tournamentId,
            name = "Tournament 1",
            startDate = "2024-01-01",
            endDate = "2024-01-02",
            status = "Upcoming",
            createdDate = "2023-12-01",
            pairs = listOf(
                TournamentPair(
                    id = pairId,
                    displayName = pairName,
                    gameCount = 0,
                    games = emptyList()
                )
            )
        )
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.success(tournament)
        val viewModel = GamesViewModel(repository)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadGames updates state with error on failure`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>()
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        val errorMessage = "Failed to load tournament"
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.failure(IOException(errorMessage))
        val viewModel = GamesViewModel(repository)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.games.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals(pairName, state.selectedPairName)
    }

    @Test
    fun `loadGames handles empty games list`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>()
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        val tournament = TournamentDetail(
            id = tournamentId,
            name = "Tournament 1",
            startDate = "2024-01-01",
            endDate = "2024-01-02",
            status = "Upcoming",
            createdDate = "2023-12-01",
            pairs = listOf(
                TournamentPair(
                    id = pairId,
                    displayName = pairName,
                    gameCount = 0,
                    games = emptyList()
                )
            )
        )
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.success(tournament)
        val viewModel = GamesViewModel(repository)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.games.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadGames handles exception without message`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>()
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.failure(RuntimeException())
        val viewModel = GamesViewModel(repository)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Unknown error", state.error)
    }

    @Test
    fun `retry calls loadGames with same parameters`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>()
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        val tournament = TournamentDetail(
            id = tournamentId,
            name = "Tournament 1",
            startDate = "2024-01-01",
            endDate = "2024-01-02",
            status = "Upcoming",
            createdDate = "2023-12-01",
            pairs = listOf(
                TournamentPair(
                    id = pairId,
                    displayName = pairName,
                    gameCount = 0,
                    games = emptyList()
                )
            )
        )
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.success(tournament)
        val viewModel = GamesViewModel(repository)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)
        viewModel.retry(tournamentId, pairId, pairName)

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `pair not found shows error`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>()
        val tournamentId = "tournament-1"
        val pairId = "pair-not-found"
        val pairName = "Unknown Team"
        val tournament = TournamentDetail(
            id = tournamentId,
            name = "Tournament 1",
            startDate = "2024-01-01",
            endDate = "2024-01-02",
            status = "Upcoming",
            createdDate = "2023-12-01",
            pairs = listOf(
                TournamentPair(
                    id = "pair-1",
                    displayName = "Team A",
                    gameCount = 0,
                    games = emptyList()
                )
            )
        )
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.success(tournament)
        val viewModel = GamesViewModel(repository)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Pair not found in tournament", state.error)
        assertTrue(state.games.isEmpty())
    }

    @Test
    fun `status conversion works correctly`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>()
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        val tournament = TournamentDetail(
            id = tournamentId,
            name = "Tournament 1",
            startDate = "2024-01-01",
            endDate = "2024-01-02",
            status = "Upcoming",
            createdDate = "2023-12-01",
            pairs = listOf(
                TournamentPair(
                    id = pairId,
                    displayName = pairName,
                    gameCount = 4,
                    games = listOf(
                        PairGame(id = "g1", tournamentId = tournamentId, round = 1, courtNumber = 1,
                            opponentPair = OpponentPairInfo(id = "o1", displayName = "Opp 1"), status = 0),
                        PairGame(id = "g2", tournamentId = tournamentId, round = 2, courtNumber = 2,
                            opponentPair = OpponentPairInfo(id = "o2", displayName = "Opp 2"), status = 1),
                        PairGame(id = "g3", tournamentId = tournamentId, round = 3, courtNumber = 3,
                            opponentPair = OpponentPairInfo(id = "o3", displayName = "Opp 3"), status = 2),
                        PairGame(id = "g4", tournamentId = tournamentId, round = 4, courtNumber = 4,
                            opponentPair = OpponentPairInfo(id = "o4", displayName = "Opp 4"), status = 3)
                    )
                )
            )
        )
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.success(tournament)
        val viewModel = GamesViewModel(repository)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val games = viewModel.uiState.value.games
        assertEquals(4, games.size)
        assertEquals("Scheduled", games[0].status)
        assertEquals("InProgress", games[1].status)
        assertEquals("Completed", games[2].status)
        assertEquals("Cancelled", games[3].status)
    }
}
