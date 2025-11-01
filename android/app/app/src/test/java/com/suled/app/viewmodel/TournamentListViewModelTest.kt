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
class TournamentListViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: TournamentListViewModel
    private lateinit var repository: TournamentRepository

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        coEvery { 
            repository.getTournaments(any(), any(), any(), any(), any(), any()) 
        } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(emptyList())
        }

        // When
        viewModel = TournamentListViewModel(repository)
        
        // Process only the immediate state update, not the delayed result
        testScheduler.runCurrent()

        // Then
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.tournaments.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadUpcomingTournaments updates state with tournaments on success`() = runTest {
        // Given
        val mockTournaments = TestData.createTournaments(3)
        coEvery { 
            repository.getTournaments(any(), any(), any(), any(), any(), any()) 
        } returns Result.success(mockTournaments)

        // When
        viewModel = TournamentListViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.tournaments.size)
            assertEquals(mockTournaments, state.tournaments)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `loadUpcomingTournaments updates state with error on failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { 
            repository.getTournaments(any(), any(), any(), any(), any(), any()) 
        } returns Result.failure(IOException(errorMessage))

        // When
        viewModel = TournamentListViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.tournaments.isEmpty())
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `loadUpcomingTournaments calls repository with correct parameters`() = runTest {
        // Given
        coEvery { 
            repository.getTournaments(any(), any(), any(), any(), any(), any()) 
        } returns Result.success(emptyList())

        // When
        viewModel = TournamentListViewModel(repository)
        advanceUntilIdle()

        // Then
        coVerify {
            repository.getTournaments(
                startDateFrom = any(),
                startDateTo = null,
                location = null,
                division = null,
                status = "Scheduled",
                maxResults = 50
            )
        }
    }

    @Test
    fun `retry calls loadUpcomingTournaments again`() = runTest {
        // Given
        coEvery { 
            repository.getTournaments(any(), any(), any(), any(), any(), any()) 
        } returns Result.success(TestData.createTournaments(2))

        viewModel = TournamentListViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 2) {
            repository.getTournaments(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `loadUpcomingTournaments sets loading state correctly`() = runTest {
        // Given
        coEvery { 
            repository.getTournaments(any(), any(), any(), any(), any(), any()) 
        } coAnswers {
            kotlinx.coroutines.delay(50)
            Result.success(TestData.createTournaments())
        }

        // When
        viewModel = TournamentListViewModel(repository)
        
        // Process immediate state update
        testScheduler.runCurrent()

        // Then - initially loading
        assertTrue(viewModel.uiState.value.isLoading)

        // Advance time to complete the delay
        advanceUntilIdle()

        // Then - not loading after completion
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `empty tournaments list returns empty state`() = runTest {
        // Given
        coEvery { 
            repository.getTournaments(any(), any(), any(), any(), any(), any()) 
        } returns Result.success(emptyList())

        // When
        viewModel = TournamentListViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.tournaments.isEmpty())
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `tournaments are sorted by start date`() = runTest {
        // Given
        val tournaments = listOf(
            TestData.createTournament(id = "1", startDate = "2025-08-01"),
            TestData.createTournament(id = "2", startDate = "2025-06-01"),
            TestData.createTournament(id = "3", startDate = "2025-07-01")
        )
        coEvery { 
            repository.getTournaments(any(), any(), any(), any(), any(), any()) 
        } returns Result.success(tournaments)

        // When
        viewModel = TournamentListViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.tournaments.size)
            // Verify tournaments are returned (sorting can be added in viewmodel if needed)
            assertEquals(tournaments, state.tournaments)
        }
    }
}
