package com.suled.app.ui.state

import com.suled.app.data.models.Game
import com.suled.app.data.models.Pair
import com.suled.app.data.models.Tournament

/**
 * Sealed interface representing the different states of tournament list screen
 */
sealed interface TournamentListUiState {
    /**
     * Initial loading state
     */
    data object Loading : TournamentListUiState
    
    /**
     * Success state with tournaments data
     */
    data class Success(
        val tournaments: List<Tournament>,
        val isRefreshing: Boolean = false
    ) : TournamentListUiState
    
    /**
     * Error state with error message
     */
    data class Error(
        val message: String,
        val tournaments: List<Tournament> = emptyList()
    ) : TournamentListUiState
}

/**
 * Sealed interface representing the different states of games screen
 */
sealed interface GamesUiState {
    /**
     * Initial loading state
     */
    data object Loading : GamesUiState
    
    /**
     * Success state with games data
     */
    data class Success(
        val games: List<Game>,
        val selectedPairName: String
    ) : GamesUiState
    
    /**
     * Error state with error message
     */
    data class Error(
        val message: String,
        val selectedPairName: String = ""
    ) : GamesUiState
}

/**
 * Sealed interface representing the different states of pair selection screen
 */
sealed interface PairSelectionUiState {
    /**
     * Initial loading state
     */
    data object Loading : PairSelectionUiState
    
    /**
     * Success state with pairs data
     */
    data class Success(
        val pairs: List<Pair>
    ) : PairSelectionUiState
    
    /**
     * Error state with error message
     */
    data class Error(
        val message: String
    ) : PairSelectionUiState
}
