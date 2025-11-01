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
class PairSelectionViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: PairSelectionViewModel
    private lateinit var repository: TournamentRepository

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        coEvery { repository.getPairs() } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(emptyList())
        }

        // When
        viewModel = PairSelectionViewModel(repository)
        
        // Process only immediate state updates
        testScheduler.runCurrent()

        // Then
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.pairs.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadPairs updates state with pairs on success`() = runTest {
        // Given
        val mockPairs = TestData.createPairs(3)
        coEvery { repository.getPairs() } returns Result.success(mockPairs)

        // When
        viewModel = PairSelectionViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.pairs.size)
            assertEquals(mockPairs, state.pairs)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `loadPairs updates state with error on failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getPairs() } returns Result.failure(IOException(errorMessage))

        // When
        viewModel = PairSelectionViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.pairs.isEmpty())
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `loadPairs handles empty response`() = runTest {
        // Given
        coEvery { repository.getPairs() } returns Result.success(emptyList())

        // When
        viewModel = PairSelectionViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.pairs.isEmpty())
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `loadPairs handles exception without message`() = runTest {
        // Given
        coEvery { repository.getPairs() } returns Result.failure(RuntimeException())

        // When
        viewModel = PairSelectionViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertEquals("Unknown error", state.error)
        }
    }

    @Test
    fun `retry calls loadPairs again`() = runTest {
        // Given
        val mockPairs = TestData.createPairs()
        coEvery { repository.getPairs() } returns Result.success(mockPairs)
        viewModel = PairSelectionViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 2) { repository.getPairs() }
    }

    @Test
    fun `retry after error clears error state`() = runTest {
        // Given - First call fails
        coEvery { repository.getPairs() } returns Result.failure(IOException("Network error"))
        viewModel = PairSelectionViewModel(repository)
        advanceUntilIdle()

        // Verify error state
        assertTrue(viewModel.uiState.value.error != null)

        // When - Second call succeeds
        val mockPairs = TestData.createPairs()
        coEvery { repository.getPairs() } returns Result.success(mockPairs)
        viewModel.retry()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.error)
            assertEquals(mockPairs, state.pairs)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `loading state is set correctly during loading`() = runTest {
        // Given
        val mockPairs = TestData.createPairs()
        coEvery { repository.getPairs() } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(mockPairs)
        }

        // When
        viewModel = PairSelectionViewModel(repository)
        
        // Process immediate state updates
        testScheduler.runCurrent()

        // Then - Initially loading
        assertTrue(viewModel.uiState.value.isLoading)

        // Then - After completion
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `multiple loadPairs calls handle state correctly`() = runTest {
        // Given
        val mockPairs1 = TestData.createPairs(2)
        val mockPairs2 = TestData.createPairs(5)
        coEvery { repository.getPairs() } returnsMany listOf(
            Result.success(mockPairs1),
            Result.success(mockPairs2)
        )

        // When
        viewModel = PairSelectionViewModel(repository)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.pairs.size)

        viewModel.loadPairs()
        advanceUntilIdle()

        // Then
        assertEquals(5, viewModel.uiState.value.pairs.size)
        coVerify(exactly = 2) { repository.getPairs() }
    }
}
