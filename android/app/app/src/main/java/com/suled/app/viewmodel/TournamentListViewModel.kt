package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.data.models.Tournament
import com.suled.app.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TournamentListUiState(
    val tournaments: List<Tournament> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TournamentListViewModel @Inject constructor(
    private val repository: TournamentRepository
) : ViewModel() {
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Observe tournaments from database (offline-first)
    val uiState: StateFlow<TournamentListUiState> = repository
        .observeTournamentsByStatus("Upcoming")
        .map { tournaments ->
            TournamentListUiState(
                tournaments = tournaments,
                isLoading = false,
                error = null
            )
        }
        .catch { exception ->
            emit(TournamentListUiState(
                tournaments = emptyList(),
                isLoading = false,
                error = exception.message ?: "Unknown error"
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TournamentListUiState(isLoading = true)
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
            _error.value = null
            
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
                .onFailure { exception ->
                    _isRefreshing.value = false
                    _error.value = exception.message ?: "Unknown error"
                }
        }
    }

    fun retry() {
        refreshTournaments()
    }
    
    fun clearError() {
        _error.value = null
    }
}
