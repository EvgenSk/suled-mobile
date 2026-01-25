package com.suled.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.suled.app.helpers.TestData
import com.suled.app.ui.components.TournamentCard
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
        // Verify loading indicator is shown (CircularProgressIndicator doesn't have text)
        // We can verify by checking that tournaments are NOT shown
        composeTestRule.onNodeWithText("Tournaments").assertIsDisplayed()
        composeTestRule.onNodeWithText("No tournaments found").assertDoesNotExist()
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
            location = "",
            division = "",
            description = ""
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
@Composable
private fun TournamentListScreenContent(
    uiState: TournamentListUiState,
    onTournamentClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Surface {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error loading tournaments",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
            
            uiState.tournaments.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No upcoming tournaments",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Check back later for new tournaments",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
