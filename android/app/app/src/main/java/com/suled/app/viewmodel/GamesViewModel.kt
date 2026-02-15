package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.data.models.Game
import com.suled.app.data.repository.ITournamentRepository
import com.suled.app.ui.state.GamesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val repository: ITournamentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GamesUiState>(GamesUiState.Loading)
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    fun loadGames(tournamentId: String, pairId: String, pairName: String) {
        viewModelScope.launch {
            _uiState.value = GamesUiState.Loading
            
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
                                    status = pairGame.status,  // Already a string from backend
                                    pair1 = pair.displayName,
                                    pair2 = pairGame.opponentPair.displayName,
                                    isOurGame = true,
                                    scheduledTime = null
                                )
                            }
                            Timber.i("Found ${games.size} games for pair $pairId")
                            _uiState.value = GamesUiState.Success(
                                games = games,
                                selectedPairName = pairName
                            )
                        } else {
                            Timber.e("Pair $pairId not found in tournament")
                            _uiState.value = GamesUiState.Error(
                                message = "Pair not found in tournament",
                                selectedPairName = pairName
                            )
                        }
                    } else {
                        _uiState.value = GamesUiState.Error(
                            message = "Tournament not found",
                            selectedPairName = pairName
                        )
                    }
                }
                result.isFailure -> {
                    val exception = result.exceptionOrNull()
                    Timber.e(exception, "Error loading games")
                    _uiState.value = GamesUiState.Error(
                        message = exception?.message ?: "Unknown error",
                        selectedPairName = pairName
                    )
                }
            }
        }
    }

    fun retry(tournamentId: String, pairId: String, pairName: String) {
        loadGames(tournamentId, pairId, pairName)
    }
}
