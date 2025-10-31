package com.suled.app.viewmodel

import app.cash.turbine.test
import com.suled.app.data.repository.TournamentRepository
import com.suled.app.helpers.CoroutineTestRule
import com.suled.app.helpers.TestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: GamesViewModel
    private lateinit var repository: TournamentRepository

    @Before
    fun setup() {
        repository = mockk()
        viewModel = GamesViewModel(repository)
    }

    @Test
    fun `initial state is empty`() {
        // Then
        assertTrue(viewModel.uiState.value.games.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertEquals("", viewModel.uiState.value.selectedPairName)
    }

    @Test
    fun `loadGames updates state with games on success`() = runTest {
        // Given
        val pairId = "pair-1"
        val pairName = "Team A"
        val mockGames = TestData.createGames(5, ourGames = 2)
        coEvery { repository.getGamesForPair(pairId) } returns Result.success(mockGames)

        // When
        viewModel.loadGames(pairId, pairName)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(5, state.games.size)
            assertEquals(mockGames, state.games)
            assertEquals(pairName, state.selectedPairName)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `loadGames sets loading state correctly`() = runTest {
        // Given
        val pairId = "pair-1"
        val pairName = "Team A"
        val mockGames = TestData.createGames()
        coEvery { repository.getGamesForPair(pairId) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(mockGames)
        }

        // When
        viewModel.loadGames(pairId, pairName)

        // Then - Initially loading
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(pairName, viewModel.uiState.value.selectedPairName)

        // Then - After completion
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadGames updates state with error on failure`() = runTest {
        // Given
        val pairId = "pair-1"
        val pairName = "Team A"
        val errorMessage = "Failed to load games"
        coEvery { repository.getGamesForPair(pairId) } returns Result.failure(IOException(errorMessage))

        // When
        viewModel.loadGames(pairId, pairName)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.games.isEmpty())
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals(errorMessage, state.error)
            assertEquals(pairName, state.selectedPairName)
        }
    }

    @Test
    fun `loadGames handles empty games list`() = runTest {
        // Given
        val pairId = "pair-1"
        val pairName = "Team A"
        coEvery { repository.getGamesForPair(pairId) } returns Result.success(emptyList())

        // When
        viewModel.loadGames(pairId, pairName)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.games.isEmpty())
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `loadGames handles exception without message`() = runTest {
        // Given
        val pairId = "pair-1"
        val pairName = "Team A"
        coEvery { repository.getGamesForPair(pairId) } returns Result.failure(RuntimeException())

        // When
        viewModel.loadGames(pairId, pairName)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertEquals("Unknown error", state.error)
        }
    }

    @Test
    fun `retry calls loadGames with same parameters`() = runTest {
        // Given
        val pairId = "pair-1"
        val pairName = "Team A"
        val mockGames = TestData.createGames()
        coEvery { repository.getGamesForPair(pairId) } returns Result.success(mockGames)

        // When
        viewModel.loadGames(pairId, pairName)
        advanceUntilIdle()
        viewModel.retry(pairId, pairName)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 2) { repository.getGamesForPair(pairId) }
    }

    @Test
    fun `retry after error clears error state`() = runTest {
        // Given - First call fails
        val pairId = "pair-1"
        val pairName = "Team A"
        coEvery { repository.getGamesForPair(pairId) } returns Result.failure(IOException("Network error"))
        
        viewModel.loadGames(pairId, pairName)
        advanceUntilIdle()

        // Verify error state
        assertTrue(viewModel.uiState.value.error != null)

        // When - Second call succeeds
        val mockGames = TestData.createGames()
        coEvery { repository.getGamesForPair(pairId) } returns Result.success(mockGames)
        viewModel.retry(pairId, pairName)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.error)
            assertEquals(mockGames, state.games)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `loadGames for different pairs updates state correctly`() = runTest {
        // Given
        val pair1Id = "pair-1"
        val pair1Name = "Team A"
        val pair2Id = "pair-2"
        val pair2Name = "Team B"
        val games1 = TestData.createGames(3)
        val games2 = TestData.createGames(7)
        
        coEvery { repository.getGamesForPair(pair1Id) } returns Result.success(games1)
        coEvery { repository.getGamesForPair(pair2Id) } returns Result.success(games2)

        // When - Load games for first pair
        viewModel.loadGames(pair1Id, pair1Name)
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.games.size)
        assertEquals(pair1Name, viewModel.uiState.value.selectedPairName)

        // When - Load games for second pair
        viewModel.loadGames(pair2Id, pair2Name)
        advanceUntilIdle()

        // Then
        assertEquals(7, viewModel.uiState.value.games.size)
        assertEquals(pair2Name, viewModel.uiState.value.selectedPairName)
    }

    @Test
    fun `loadGames filters our games correctly`() = runTest {
        // Given
        val pairId = "pair-1"
        val pairName = "Team A"
        val mockGames = TestData.createGames(10, ourGames = 3)
        coEvery { repository.getGamesForPair(pairId) } returns Result.success(mockGames)

        // When
        viewModel.loadGames(pairId, pairName)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(10, state.games.size)
        assertEquals(3, state.games.count { it.isOurGame })
    }

    @Test
    fun `error state contains pair name`() = runTest {
        // Given
        val pairId = "pair-1"
        val pairName = "Team A"
        coEvery { repository.getGamesForPair(pairId) } returns Result.failure(IOException("Error"))

        // When
        viewModel.loadGames(pairId, pairName)
        advanceUntilIdle()

        // Then
        assertEquals(pairName, viewModel.uiState.value.selectedPairName)
        assertNotNull(viewModel.uiState.value.error)
    }
}
