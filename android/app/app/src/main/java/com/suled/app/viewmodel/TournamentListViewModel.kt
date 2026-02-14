package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.data.repository.TournamentRepository
import com.suled.app.ui.state.TournamentListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TournamentListViewModel @Inject constructor(
    private val repository: TournamentRepository
) : ViewModel() {
    
    private val _isRefreshing = MutableStateFlow(false)

    // Observe tournaments from database (offline-first)
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
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TournamentListUiState.Loading
        )

    init {
        // Load tournaments on initialization
        refreshTournaments()
    }

    /**
     * Refresh tournaments from network
     */
    fun refreshTournaments() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            // Get tournaments starting from today onwards
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            repository.refreshTournaments(
                startDateFrom = today,
                status = "Upcoming",
                maxResults = 50
            )
                .onSuccess {
                    _isRefreshing.value = false
                }
                .onFailure {
                    _isRefreshing.value = false
                }
        }
    }

    fun retry() {
        refreshTournaments()
    }
}
