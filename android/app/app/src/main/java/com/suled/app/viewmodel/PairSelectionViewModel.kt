package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.data.repository.TournamentRepository
import com.suled.app.ui.state.PairSelectionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairSelectionViewModel @Inject constructor(
    private val repository: TournamentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PairSelectionUiState>(PairSelectionUiState.Loading)
    val uiState: StateFlow<PairSelectionUiState> = _uiState.asStateFlow()

    fun loadPairsForTournament(tournamentId: String) {
        viewModelScope.launch {
            _uiState.value = PairSelectionUiState.Loading
            
            repository.getPairsForTournament(tournamentId)
                .onSuccess { pairs ->
                    _uiState.value = PairSelectionUiState.Success(pairs = pairs)
                }
                .onFailure { exception ->
                    _uiState.value = PairSelectionUiState.Error(
                        message = exception.message ?: "Unknown error"
                    )
                }
        }
    }

    fun retry() {
        // Retry would need the tournamentId - caller should handle this
    }
}
