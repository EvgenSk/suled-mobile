package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
     * [TournamentListUiState.Error] if data loading fails.
     */
    val uiState: StateFlow<TournamentListUiState> = combine(
        repository.observeTournamentsByStatus("Upcoming"),
        _isRefreshing
    ) { tournaments, isRefreshing ->
        if (tournaments.isNotEmpty()) {
            TournamentListUiState.Success(
                tournaments = tournaments,
                isRefreshing = isRefreshing
            )
        } else if (!isRefreshing) {
            TournamentListUiState.Success(
                tournaments = emptyList(),
                isRefreshing = false
            )
        } else {
            TournamentListUiState.Loading
        }
    }
        .catch { exception ->
            emit(
                TournamentListUiState.Error(
                    message = exception.message ?: "Unknown error",
                    tournaments = emptyList()
                )
            )
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
     * Fetches tournaments starting from today with "Upcoming" status.
     * Updates are automatically propagated through the [uiState] flow.
     * Prevents concurrent refreshes - if already refreshing, returns immediately.
     */
    fun refreshTournaments() {
        // Prevent concurrent refreshes
        if (_isRefreshing.value) {
            return
        }
        
        viewModelScope.launch {
            _isRefreshing.value = true
            
            // Get tournaments starting from today onwards
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            repository.refreshTournaments(
                startDateFrom = today,
                status = "Upcoming",
                maxResults = Constants.Api.TOURNAMENT_LIST_LIMIT
            )
                .onSuccess {
                    _isRefreshing.value = false
                }
                .onFailure {
                    _isRefreshing.value = false
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
