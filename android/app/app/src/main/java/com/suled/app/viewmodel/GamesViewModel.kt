package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.data.models.Game
import com.suled.app.data.models.TournamentDetail
import com.suled.app.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GamesUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPairName: String = ""
)

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val repository: TournamentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    fun loadGames(tournamentId: String, pairId: String, pairName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedPairName = pairName
            )
            
            val result = repository.getTournamentDetail(tournamentId)
            when {
                result.isSuccess -> {
                    val tournament = result.getOrNull()
                    if (tournament != null) {
                        val pair = tournament.pairs.find { it.id == pairId }
                        if (pair != null) {
                            val games = pair.games.map { pairGame ->
                                Game(
                                    id = pairGame.id,
                                    round = pairGame.round,
                                    courtNumber = pairGame.courtNumber,
                                    status = getStatusString(pairGame.status),
                                    pair1 = pair.displayName,
                                    pair2 = pairGame.opponentPair.displayName,
                                    isOurGame = true,
                                    scheduledTime = null
                                )
                            }
                            android.util.Log.i("GamesViewModel", "Found ${games.size} games for pair $pairId")
                            _uiState.value = _uiState.value.copy(
                                games = games,
                                isLoading = false
                            )
                        } else {
                            android.util.Log.e("GamesViewModel", "Pair $pairId not found in tournament")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Pair not found in tournament"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Tournament not found"
                        )
                    }
                }
                result.isFailure -> {
                    val exception = result.exceptionOrNull()
                    android.util.Log.e("GamesViewModel", "Error loading games", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Unknown error"
                    )
                }
            }
        }
    }
    
    private fun getStatusString(status: Int): String {
        return when (status) {
            0 -> "Scheduled"
            1 -> "InProgress"
            2 -> "Completed"
            3 -> "Cancelled"
            else -> "Unknown"
        }
    }

    fun retry(tournamentId: String, pairId: String, pairName: String) {
        loadGames(tournamentId, pairId, pairName)
    }
}
