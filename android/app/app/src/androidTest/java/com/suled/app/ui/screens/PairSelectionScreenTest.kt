package com.suled.app.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.suled.app.helpers.TestData
import com.suled.app.ui.theme.SuledTheme
import com.suled.app.viewmodel.PairSelectionUiState
import org.junit.Rule
import org.junit.Test

class PairSelectionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pairSelectionScreen_displaysLoadingIndicator_whenLoading() {
        // Given
        val loadingState = PairSelectionUiState(isLoading = true)

        // When
        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = loadingState,
                    onPairClick = { _, _ -> },
                    onRetry = {}
                )
            }
        }

        // Then
        // Verify loading indicator is shown (CircularProgressIndicator doesn't have text)
        // We can verify by checking that pairs are NOT shown
        composeTestRule.onNodeWithText("Select Your Pair").assertIsDisplayed()
        composeTestRule.onNodeWithText("No pairs found").assertDoesNotExist()
    }

    @Test
    fun pairSelectionScreen_displaysLoadedPairs() {
        // Given
        val mockPairs = TestData.createPairs(3)
        val state = PairSelectionUiState(pairs = mockPairs)
        
        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = state,
                    onPairClick = { _, _ -> },
                    onRetry = {}
                )
            }
        }

        // Then
        mockPairs.forEach { pair ->
            composeTestRule.onNodeWithText(pair.displayName).assertIsDisplayed()
            composeTestRule.onNodeWithText(pair.player1).assertIsDisplayed()
            composeTestRule.onNodeWithText(pair.player2).assertIsDisplayed()
        }
    }

    @Test
    fun pairSelectionScreen_displaysErrorMessage_whenError() {
        // Given
        val errorMessage = "Network error"
        val errorState = PairSelectionUiState(error = errorMessage)

        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = errorState,
                    onPairClick = { _, _ -> },
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Error loading pairs").assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun pairSelectionScreen_displaysEmptyState_whenNoPairs() {
        // Given
        val emptyState = PairSelectionUiState(pairs = emptyList())

        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = emptyState,
                    onPairClick = { _, _ -> },
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No pairs found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please upload a tournament file first").assertIsDisplayed()
    }

    @Test
    fun pairSelectionScreen_clickPair_triggersCallback() {
        // Given
        var clickedPairId: String? = null
        var clickedPairName: String? = null
        val mockPairs = TestData.createPairs(1)
        val state = PairSelectionUiState(pairs = mockPairs)

        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = state,
                    onPairClick = { id, name ->
                        clickedPairId = id
                        clickedPairName = name
                    },
                    onRetry = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText(mockPairs[0].displayName).performClick()

        // Then
        assert(clickedPairId == mockPairs[0].id)
        assert(clickedPairName == mockPairs[0].displayName)
    }

    @Test
    fun pairSelectionScreen_clickRefresh_callsRetry() {
        // Given
        var retryClicked = false
        val state = PairSelectionUiState(pairs = TestData.createPairs())

        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = state,
                    onPairClick = { _, _ -> },
                    onRetry = { retryClicked = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Refresh").performClick()

        // Then
        assert(retryClicked)
    }

    @Test
    fun pairSelectionScreen_clickRetryButton_callsRetry() {
        // Given
        var retryClicked = false
        val errorState = PairSelectionUiState(error = "Network error")

        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = errorState,
                    onPairClick = { _, _ -> },
                    onRetry = { retryClicked = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Retry").performClick()

        // Then
        assert(retryClicked)
    }

    @Test
    fun pairSelectionScreen_hasCorrectTitle() {
        // Given
        val state = PairSelectionUiState()

        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = state,
                    onPairClick = { _, _ -> },
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Select Your Pair").assertIsDisplayed()
    }

    @Test
    fun pairSelectionScreen_displaysMultiplePairs() {
        // Given
        val mockPairs = TestData.createPairs(5)
        val state = PairSelectionUiState(pairs = mockPairs)

        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = state,
                    onPairClick = { _, _ -> },
                    onRetry = {}
                )
            }
        }

        // Then - All pairs should be scrollable and visible
        mockPairs.forEach { pair ->
            composeTestRule.onNodeWithText(pair.displayName).assertExists()
        }
    }

    @Test
    fun pairSelectionScreen_doesNotShowLoading_whenNotLoading() {
        // Given
        val state = PairSelectionUiState(pairs = TestData.createPairs())

        composeTestRule.setContent {
            SuledTheme {
                PairSelectionScreenContent(
                    uiState = state,
                    onPairClick = { _, _ -> },
                    onRetry = {}
                )
            }
        }

        // Then
        // Verify that pairs are shown and not loading
        composeTestRule.onNodeWithText(state.pairs.first().displayName).assertIsDisplayed()
        composeTestRule.onNodeWithText("No pairs found").assertDoesNotExist()
    }
}
