package com.vettr.android.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.remote.AutocompleteResultDto
import com.vettr.android.core.data.remote.PublicApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Guest Landing screen.
 * Provides debounced stock autocomplete search using the public (unauthenticated) API.
 *
 * Search is debounced to 300ms to avoid excessive API calls while the user types.
 */
@HiltViewModel
class GuestLandingViewModel @Inject constructor(
    private val publicApi: PublicApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuestLandingUiState())
    val uiState: StateFlow<GuestLandingUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    companion object {
        private const val DEBOUNCE_MS = 300L
        private const val MIN_QUERY_LENGTH = 1
    }

    /**
     * Called when the search query changes.
     * Debounces the input by 300ms before executing the autocomplete API call.
     *
     * @param query The current search query text
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Cancel previous search job
        searchJob?.cancel()

        if (query.length < MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(autocompleteResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            performSearch(query)
        }
    }

    /**
     * Clears the search query and results.
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = "",
                autocompleteResults = emptyList(),
                isSearching = false
            )
        }
    }

    /**
     * Execute the autocomplete search against the public API.
     */
    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true) }

        try {
            val response = publicApi.autocomplete(query = query, limit = 8)
            if (response.success) {
                _uiState.update {
                    it.copy(
                        autocompleteResults = response.data ?: emptyList(),
                        isSearching = false
                    )
                }
            } else {
                _uiState.update { it.copy(autocompleteResults = emptyList(), isSearching = false) }
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(autocompleteResults = emptyList(), isSearching = false) }
        }
    }
}

/**
 * UI state for the Guest Landing screen.
 */
@androidx.compose.runtime.Immutable
data class GuestLandingUiState(
    val searchQuery: String = "",
    val autocompleteResults: List<AutocompleteResultDto> = emptyList(),
    val isSearching: Boolean = false
)
