package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.common.AppError
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

/**
 * ViewModel for the games list screen.
 * Manages game data for a specific pair within a tournament.
 * 
 * @property repository Repository for accessing tournament and game data
 */
@HiltViewModel
class GamesViewModel @Inject constructor(
    private val repository: ITournamentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GamesUiState>(GamesUiState.Loading)
    private var currentTournamentId: String? = null
    private var currentPairId: String? = null
    private var currentPairName: String? = null
    
    /**
     * UI state flow for games screen.
     * Emits [GamesUiState.Loading] while fetching,
     * [GamesUiState.Success] with game list, or
     * [GamesUiState.Error] if loading fails.
     */
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    /**
     * Loads games for a specific pair in a tournament.
     * Fetches tournament details and extracts games for the specified pair.
     * 
     * @param tournamentId Unique tournament identifier
     * @param pairId Unique pair identifier within the tournament
     * @param pairName Display name of the pair (for error messages)
     */
    fun loadGames(tournamentId: String, pairId: String, pairName: String) {
        // Validate inputs
        if (tournamentId.isBlank()) {
            _uiState.value = GamesUiState.Error(
                message = "Invalid tournament ID",
                selectedPairName = pairName
            )
            return
        }
        
        if (pairId.isBlank()) {
            _uiState.value = GamesUiState.Error(
                message = "Invalid pair ID",
                selectedPairName = pairName
            )
            return
        }
        
        // Store current parameters for retry
        currentTournamentId = tournamentId
        currentPairId = pairId
        currentPairName = pairName
        
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
                    val message = (exception as? AppError)?.toUserMessage()
                        ?: exception?.message
                        ?: "Unknown error"
                    _uiState.value = GamesUiState.Error(
                        message = message,
                        selectedPairName = pairName
                    )
                }
            }
        }
    }

    /**
     * Retries loading games after an error.
     * Uses stored parameters from the previous load attempt.
     * If parameters are provided, they override the stored ones.
     * 
     * @param tournamentId Optional tournament identifier (uses stored if null)
     * @param pairId Optional pair identifier (uses stored if null)
     * @param pairName Optional pair name (uses stored if null)
     */
    fun retry(tournamentId: String? = null, pairId: String? = null, pairName: String? = null) {
        val tid = tournamentId ?: currentTournamentId
        val pid = pairId ?: currentPairId
        val pname = pairName ?: currentPairName
        
        if (tid != null && pid != null && pname != null) {
            loadGames(tid, pid, pname)
        }
    }
}
