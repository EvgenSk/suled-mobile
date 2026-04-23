package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.common.AppError
import com.suled.app.common.Constants
import com.suled.app.common.connectivity.ConnectivityObserver
import com.suled.app.common.connectivity.ConnectivityStatus
import com.suled.app.data.repository.ITournamentRepository
import com.suled.app.ui.state.TournamentListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for the tournament list screen.
 * Manages tournament data with offline-first approach using local database cache.
 * Also monitors network connectivity state to provide offline indicators.
 *
 * @property repository Repository for accessing tournament data
 * @property connectivityObserver Observer for monitoring network connectivity
 */
@HiltViewModel
class TournamentListViewModel @Inject constructor(
    private val repository: ITournamentRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshError = MutableStateFlow<String?>(null)

    /**
     * Network connectivity state flow.
     * Emits [ConnectivityStatus] whenever network state changes.
     */
    val connectivityStatus: StateFlow<ConnectivityStatus> = connectivityObserver.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(Constants.Flow.STATE_FLOW_TIMEOUT_MILLIS),
            initialValue = ConnectivityStatus.UNKNOWN
        )

    /**
     * UI state flow combining local database tournaments with refresh state.
     * Emits [TournamentListUiState.Loading] initially, then
     * [TournamentListUiState.Success] with tournament list, or
     * [TournamentListUiState.Error] if a network refresh fails and the cache is empty.
     */
    val uiState: StateFlow<TournamentListUiState> = combine(
        repository.observeTournaments(),
        _isRefreshing,
        _refreshError
    ) { tournaments, isRefreshing, refreshError ->
        when {
            tournaments.isNotEmpty() -> TournamentListUiState.Success(
                tournaments = tournaments,
                isRefreshing = isRefreshing
            )
            isRefreshing -> TournamentListUiState.Loading
            refreshError != null -> TournamentListUiState.Error(message = refreshError)
            else -> TournamentListUiState.Success(tournaments = emptyList(), isRefreshing = false)
        }
    }
        .catch { exception ->
            emit(TournamentListUiState.Error(message = exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(Constants.Flow.STATE_FLOW_TIMEOUT_MILLIS),
            initialValue = TournamentListUiState.Loading
        )

    init {
        // Load tournaments on initialization
        refreshTournaments()
    }

    /**
     * Refreshes tournament list from network API.
     * Fetches tournaments starting from today.
     * Updates are automatically propagated through the [uiState] flow.
     * Prevents concurrent refreshes - if already refreshing, returns immediately.
     */
    fun refreshTournaments() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshError.value = null

            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            repository.refreshTournaments(
                startDateFrom = today,
                maxResults = Constants.Api.TOURNAMENT_LIST_LIMIT
            )
                .onSuccess {
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    _isRefreshing.value = false
                    _refreshError.value = (error as? AppError)?.toUserMessage()
                        ?: error.message
                        ?: "Unknown error"
                }
        }
    }

    /**
     * Retries loading tournaments after an error.
     * Delegates to [refreshTournaments].
     */
    fun retry() {
        refreshTournaments()
    }
}
