package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.common.AppError
import com.suled.app.data.repository.ITournamentRepository
import com.suled.app.ui.state.PairSelectionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the pair selection screen.
 * Manages loading and displaying pairs available in a tournament.
 * 
 * @property repository Repository for accessing tournament and pair data
 */
@HiltViewModel
class PairSelectionViewModel @Inject constructor(
    private val repository: ITournamentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PairSelectionUiState>(PairSelectionUiState.Loading)
    private var currentTournamentId: String? = null
    
    /**
     * UI state flow for pair selection screen.
     * Emits [PairSelectionUiState.Loading] while fetching,
     * [PairSelectionUiState.Success] with pair list, or
     * [PairSelectionUiState.Error] if loading fails.
     */
    val uiState: StateFlow<PairSelectionUiState> = _uiState.asStateFlow()

    /**
     * Loads all pairs available in the specified tournament.
     * 
     * @param tournamentId Unique tournament identifier
     */
    fun loadPairsForTournament(tournamentId: String) {
        // Validate input
        if (tournamentId.isBlank()) {
            _uiState.value = PairSelectionUiState.Error(message = "Invalid tournament ID")
            return
        }
        
        currentTournamentId = tournamentId
        
        viewModelScope.launch {
            _uiState.value = PairSelectionUiState.Loading
            
            repository.getPairsForTournament(tournamentId)
                .onSuccess { pairs ->
                    _uiState.value = PairSelectionUiState.Success(pairs = pairs)
                }
                .onFailure { exception ->
                    val message = (exception as? AppError)?.toUserMessage()
                        ?: exception.message
                        ?: "Unknown error"
                    _uiState.value = PairSelectionUiState.Error(message = message)
                }
        }
    }

    /**
     * Retries loading pairs after an error.
     * Uses the stored tournament ID from the previous load attempt.
     */
    fun retry() {
        currentTournamentId?.let { tournamentId ->
            loadPairsForTournament(tournamentId)
        }
    }
}
