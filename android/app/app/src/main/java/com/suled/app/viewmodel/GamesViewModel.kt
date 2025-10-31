package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.data.models.Game
import com.suled.app.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GamesUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPairName: String = ""
)

class GamesViewModel(
    private val repository: TournamentRepository = TournamentRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    fun loadGames(pairId: String, pairName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedPairName = pairName
            )
            
            repository.getGamesForPair(pairId)
                .onSuccess { games ->
                    _uiState.value = _uiState.value.copy(
                        games = games,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error"
                    )
                }
        }
    }

    fun retry(pairId: String, pairName: String) {
        loadGames(pairId, pairName)
    }
}
