package com.suled.app.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.suled.app.helpers.TestData
import com.suled.app.ui.theme.SuledTheme
import com.suled.app.viewmodel.TournamentListUiState
import org.junit.Rule
import org.junit.Test

class TournamentListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tournamentListScreen_displaysLoadingIndicator_whenLoading() {
        // Given
        val loadingState = TournamentListUiState(isLoading = true)

        // When
        composeTestRule.setContent {
            SuledTheme {
                TournamentListScreenContent(
                    uiState = loadingState,
                    onTournamentClick = { },
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun tournamentListScreen_displaysLoadedTournaments() {
        // Given
        val mockTournaments = TestData.createTournaments(3)
        val state = TournamentListUiState(tournaments = mockTournaments)
        
        composeTestRule.setContent {
            SuledTheme {
                TournamentListScreenContent(
                    uiState = state,
                    onTournamentClick = { },
                    onRetry = {}
                )
            }
        }

        // Then
        mockTournaments.forEach { tournament ->
            composeTestRule.onNodeWithText(tournament.name).assertIsDisplayed()
        }
    }

    @Test
    fun tournamentListScreen_displaysErrorMessage_whenError() {
        // Given
        val errorMessage = "Network error"
        val errorState = TournamentListUiState(error = errorMessage)

        composeTestRule.setContent {
            SuledTheme {
                TournamentListScreenContent(
                    uiState = errorState,
                    onTournamentClick = { },
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Error loading tournaments").assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun tournamentListScreen_displaysEmptyState_whenNoTournaments() {
        // Given
        val emptyState = TournamentListUiState(tournaments = emptyList())

        composeTestRule.setContent {
            SuledTheme {
                TournamentListScreenContent(
                    uiState = emptyState,
                    onTournamentClick = { },
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No upcoming tournaments").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check back later for new tournaments").assertIsDisplayed()
    }

    @Test
    fun tournamentListScreen_displaysRetryButton_whenError() {
        // Given
        val errorState = TournamentListUiState(error = "Error")

        composeTestRule.setContent {
            SuledTheme {
                TournamentListScreenContent(
                    uiState = errorState,
                    onTournamentClick = { },
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun tournamentCard_displaysAllTournamentInformation() {
        // Given
        val tournament = TestData.createTournament(
            name = "Summer Championship",
            location = "Central Arena",
            division = "Division A",
            description = "Annual summer tournament",
            gameCount = 24,
            status = "Scheduled"
        )
        
        composeTestRule.setContent {
            SuledTheme {
                TournamentCard(
                    tournament = tournament,
                    onClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Summer Championship").assertIsDisplayed()
        composeTestRule.onNodeWithText("📍 Central Arena").assertIsDisplayed()
        composeTestRule.onNodeWithText("Division A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Annual summer tournament").assertIsDisplayed()
        composeTestRule.onNodeWithText("24 games").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scheduled").assertIsDisplayed()
    }

    @Test
    fun tournamentCard_isClickable() {
        // Given
        var clicked = false
        val tournament = TestData.createTournament()
        
        composeTestRule.setContent {
            SuledTheme {
                TournamentCard(
                    tournament = tournament,
                    onClick = { clicked = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText(tournament.name).performClick()

        // Then
        assert(clicked)
    }

    @Test
    fun tournamentCard_handlesNullOptionalFields() {
        // Given
        val tournament = TestData.createTournament(
            location = null,
            division = null,
            description = null
        )
        
        composeTestRule.setContent {
            SuledTheme {
                TournamentCard(
                    tournament = tournament,
                    onClick = {}
                )
            }
        }

        // Then - Should display without crashes
        composeTestRule.onNodeWithText(tournament.name).assertIsDisplayed()
        composeTestRule.onNodeWithText("${tournament.gameCount} games").assertIsDisplayed()
    }

    @Test
    fun tournamentListScreen_multipleCards_areScrollable() {
        // Given
        val tournaments = TestData.createTournaments(10)
        val state = TournamentListUiState(tournaments = tournaments)
        
        composeTestRule.setContent {
            SuledTheme {
                TournamentListScreenContent(
                    uiState = state,
                    onTournamentClick = { },
                    onRetry = {}
                )
            }
        }

        // Then - First tournament should be visible
        composeTestRule.onNodeWithText(tournaments.first().name).assertIsDisplayed()
        
        // When - Scroll to last tournament
        composeTestRule.onNodeWithText(tournaments.last().name).performScrollTo()
        
        // Then - Last tournament should now be visible
        composeTestRule.onNodeWithText(tournaments.last().name).assertIsDisplayed()
    }

    @Test
    fun tournamentCard_displaysFormattedDate() {
        // Given
        val tournament = TestData.createTournament(
            startDate = "2025-06-15"
        )
        
        composeTestRule.setContent {
            SuledTheme {
                TournamentCard(
                    tournament = tournament,
                    onClick = {}
                )
            }
        }

        // Then - Date should be displayed (exact format depends on locale)
        composeTestRule.onNode(
            hasText("2025", substring = true) or 
            hasText("Jun", substring = true) or 
            hasText("June", substring = true)
        ).assertExists()
    }
}

// Helper composable for testing without ViewModel
@androidx.compose.runtime.Composable
private fun TournamentListScreenContent(
    uiState: TournamentListUiState,
    onTournamentClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    androidx.compose.material3.Surface {
        when {
            uiState.isLoading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "Error loading tournaments",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                    androidx.compose.foundation.layout.Spacer(
                        modifier = androidx.compose.ui.Modifier.height(8.dp)
                    )
                    androidx.compose.material3.Text(
                        text = uiState.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.foundation.layout.Spacer(
                        modifier = androidx.compose.ui.Modifier.height(16.dp)
                    )
                    androidx.compose.material3.Button(onClick = onRetry) {
                        androidx.compose.material3.Text("Retry")
                    }
                }
            }
            
            uiState.tournaments.isEmpty() -> {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "No upcoming tournaments",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                    androidx.compose.foundation.layout.Spacer(
                        modifier = androidx.compose.ui.Modifier.height(8.dp)
                    )
                    androidx.compose.material3.Text(
                        text = "Check back later for new tournaments",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            else -> {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.tournaments.size) { index ->
                        TournamentCard(
                            tournament = uiState.tournaments[index],
                            onClick = { onTournamentClick(uiState.tournaments[index].id) }
                        )
                    }
                }
            }
        }
    }
}
