package com.suled.app.viewmodel

import com.suled.app.data.models.OpponentPairInfo
import com.suled.app.data.models.PairGame
import com.suled.app.data.models.TournamentDetail
import com.suled.app.data.models.TournamentPair
import com.suled.app.data.repository.TournamentRepository
import com.suled.app.ui.state.GamesUiState
import com.suled.wear.WearSyncService
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
    private val wearSyncService = mockk<WearSyncService>(relaxed = true)

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
        val repository = mockk<TournamentRepository>(relaxed = true)
        val viewModel = GamesViewModel(repository, wearSyncService)

        // Then - initial state is Loading
        assertTrue(viewModel.uiState.value is GamesUiState.Loading)
    }

    @Test
    fun `loadGames updates state with games on success`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>(relaxed = true)
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
                            status = "Scheduled"
                        ),
                        PairGame(
                            id = "game-2",
                            tournamentId = tournamentId,
                            round = 2,
                            courtNumber = 2,
                            opponentPair = OpponentPairInfo(id = "opp-2", displayName = "Team C"),
                            status = "Scheduled"
                        )
                    )
                )
            )
        )
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.success(tournament)
        val viewModel = GamesViewModel(repository, wearSyncService)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is GamesUiState.Success)
        assertEquals(2, (state as GamesUiState.Success).games.size)
        assertEquals(pairName, state.selectedPairName)
    }

    @Test
    fun `loadGames sets loading state correctly`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>(relaxed = true)
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
        val viewModel = GamesViewModel(repository, wearSyncService)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is GamesUiState.Success)
    }

    @Test
    fun `loadGames updates state with error on failure`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>(relaxed = true)
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        val errorMessage = "Failed to load tournament"
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.failure(IOException(errorMessage))
        val viewModel = GamesViewModel(repository, wearSyncService)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is GamesUiState.Error)
        assertNotNull((state as GamesUiState.Error).message)
        assertEquals(pairName, state.selectedPairName)
    }

    @Test
    fun `loadGames handles empty games list`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>(relaxed = true)
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
        val viewModel = GamesViewModel(repository, wearSyncService)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is GamesUiState.Success)
        assertTrue((state as GamesUiState.Success).games.isEmpty())
    }

    @Test
    fun `loadGames handles exception without message`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>(relaxed = true)
        val tournamentId = "tournament-1"
        val pairId = "pair-1"
        val pairName = "Team A"
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.failure(RuntimeException())
        val viewModel = GamesViewModel(repository, wearSyncService)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is GamesUiState.Error)
        assertEquals("Unknown error", (state as GamesUiState.Error).message)
    }

    @Test
    fun `retry calls loadGames with same parameters`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>(relaxed = true)
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
        val viewModel = GamesViewModel(repository, wearSyncService)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)
        viewModel.retry(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is GamesUiState.Success)
    }

    @Test
    fun `pair not found shows error`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>(relaxed = true)
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
        val viewModel = GamesViewModel(repository, wearSyncService)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is GamesUiState.Error)
        assertEquals("Pair not found in tournament", (state as GamesUiState.Error).message)
    }

    @Test
    fun `status conversion works correctly`() = runTest(testDispatcher) {
        // Given
        val repository = mockk<TournamentRepository>(relaxed = true)
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
                            opponentPair = OpponentPairInfo(id = "o1", displayName = "Opp 1"), status = "Scheduled"),
                        PairGame(id = "g2", tournamentId = tournamentId, round = 2, courtNumber = 2,
                            opponentPair = OpponentPairInfo(id = "o2", displayName = "Opp 2"), status = "InProgress"),
                        PairGame(id = "g3", tournamentId = tournamentId, round = 3, courtNumber = 3,
                            opponentPair = OpponentPairInfo(id = "o3", displayName = "Opp 3"), status = "Completed"),
                        PairGame(id = "g4", tournamentId = tournamentId, round = 4, courtNumber = 4,
                            opponentPair = OpponentPairInfo(id = "o4", displayName = "Opp 4"), status = "Cancelled")
                    )
                )
            )
        )
        coEvery { repository.getTournamentDetail(tournamentId) } returns Result.success(tournament)
        val viewModel = GamesViewModel(repository, wearSyncService)

        // When
        viewModel.loadGames(tournamentId, pairId, pairName)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is GamesUiState.Success)
        val games = (state as GamesUiState.Success).games
        assertEquals(4, games.size)
        assertEquals("Scheduled", games[0].status)
        assertEquals("InProgress", games[1].status)
        assertEquals("Completed", games[2].status)
        assertEquals("Cancelled", games[3].status)
    }
}
