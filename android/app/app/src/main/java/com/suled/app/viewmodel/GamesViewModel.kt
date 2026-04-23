package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.common.AppError
import com.suled.app.data.models.Game
import com.suled.app.data.models.TournamentDetail
import com.suled.app.data.repository.ITournamentRepository
import com.suled.app.ui.state.GamesUiState
import com.suled.wear.WearSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for the games list screen.
 * Manages game data for a specific pair within a tournament.
 *
 * @property repository Repository for accessing tournament and game data
 * @property wearSyncService Service for syncing data to the watch
 */
@HiltViewModel
class GamesViewModel @Inject constructor(
    private val repository: ITournamentRepository,
    private val wearSyncService: WearSyncService
) : ViewModel() {

    private val _uiState = MutableStateFlow<GamesUiState>(GamesUiState.Loading)
    private val _isTracked = MutableStateFlow(false)
    private var currentTournamentId: String? = null
    private var currentPairId: String? = null
    private var currentPairName: String? = null
    private var currentTournamentDetail: TournamentDetail? = null

    /**
     * UI state flow for games screen.
     * Emits [GamesUiState.Loading] while fetching,
     * [GamesUiState.Success] with game list, or
     * [GamesUiState.Error] if loading fails.
     */
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    /** Whether the current pair is being tracked for watch notifications. */
    val isTracked: StateFlow<Boolean> = _isTracked.asStateFlow()

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

        // Store current parameters for retry / tracking
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
                        currentTournamentDetail = tournament

                        // Check tracking status and sync to watch
                        val pairIdInt = pairId.toIntOrNull() ?: pairId.hashCode()
                        _isTracked.value = repository.isTracked(tournamentId, pairIdInt)
                        wearSyncService.syncTournamentDetail(tournament)

                        val pair = tournament.pairs.find { it.id == pairId }
                        if (pair != null) {
                            val games = pair.games.map { pairGame ->
                                val round = tournament.rounds.firstOrNull { it.roundNumber == pairGame.round }
                                Game(
                                    id = pairGame.id,
                                    round = pairGame.round,
                                    courtNumber = pairGame.courtNumber,
                                    status = computeGameStatus(
                                        tournament.startDate,
                                        round?.startTime,
                                        round?.endTime,
                                        pairGame.status
                                    ),
                                    pair1 = pair.displayName,
                                    pair2 = pairGame.opponentPair.displayName,
                                    isOurGame = true,
                                    scheduledTime = round?.startTime
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
     * Start tracking the current pair for watch notifications.
     * Persists to Room DB and immediately syncs to the watch.
     */
    fun trackCurrentPair() {
        val tid = currentTournamentId ?: return
        val pid = currentPairId ?: return
        val pname = currentPairName ?: return
        val pairIdInt = pid.toIntOrNull() ?: pid.hashCode()
        val tournamentName = currentTournamentDetail?.name ?: return

        viewModelScope.launch {
            repository.trackPair(
                tournamentId = tid,
                tournamentName = tournamentName,
                pairId = pairIdInt,
                pairDisplayName = pname
            )
            _isTracked.value = true
            wearSyncService.syncTrackedPairs()
        }
    }

    /**
     * Stop tracking the current pair.
     * Removes from Room DB and immediately syncs updated list to the watch.
     */
    fun untrackCurrentPair() {
        val tid = currentTournamentId ?: return
        val pid = currentPairId ?: return
        val pairIdInt = pid.toIntOrNull() ?: pid.hashCode()

        viewModelScope.launch {
            repository.untrackPair(tid, pairIdInt)
            _isTracked.value = false
            wearSyncService.syncTrackedPairs()
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

    private fun computeGameStatus(
        tournamentStartDate: String?,
        roundStartTime: String?,
        roundEndTime: String?,
        backendStatus: String
    ): String {
        // Trust backend for terminal states
        val statusLower = backendStatus.lowercase()
        if (statusLower == "cancelled") return backendStatus

        if (roundStartTime == null) return backendStatus

        return try {
            val now = LocalDateTime.now()
            val date = tournamentStartDate?.let {
                try {
                    LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
                } catch (e: Exception) {
                    LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                }
            } ?: LocalDate.now()

            val startTime = LocalTime.parse(roundStartTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val gameStart = LocalDateTime.of(date, startTime)

            if (now.isBefore(gameStart)) return "Scheduled"

            if (roundEndTime != null) {
                val endTime = LocalTime.parse(roundEndTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                val gameEnd = LocalDateTime.of(date, endTime)
                if (now.isBefore(gameEnd)) return "InProgress"
            }

            "Completed"
        } catch (e: Exception) {
            backendStatus
        }
    }
}

