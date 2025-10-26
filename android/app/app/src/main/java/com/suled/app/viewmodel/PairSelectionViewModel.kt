package com.suled.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suled.app.data.models.Pair
import com.suled.app.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PairSelectionUiState(
    val pairs: List<Pair> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PairSelectionViewModel : ViewModel() {
    private val repository = TournamentRepository()
    
    private val _uiState = MutableStateFlow(PairSelectionUiState())
    val uiState: StateFlow<PairSelectionUiState> = _uiState.asStateFlow()

    init {
        loadPairs()
    }

    fun loadPairs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getPairs()
                .onSuccess { pairs ->
                    _uiState.value = _uiState.value.copy(
                        pairs = pairs,
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
        loadPairs()
    }
}
