package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.data.models.Tournament
import com.suled.app.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TournamentListUiState(
    val tournaments: List<Tournament> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TournamentListViewModel(
    private val repository: TournamentRepository = TournamentRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TournamentListUiState())
    val uiState: StateFlow<TournamentListUiState> = _uiState.asStateFlow()

    init {
        loadUpcomingTournaments()
    }

    fun loadUpcomingTournaments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Get tournaments starting from today onwards
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            repository.getTournaments(
                startDateFrom = today,
                status = "Scheduled",
                maxResults = 50
            )
                .onSuccess { tournaments ->
                    _uiState.value = _uiState.value.copy(
                        tournaments = tournaments,
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

    fun retry() {
        loadUpcomingTournaments()
    }
}
