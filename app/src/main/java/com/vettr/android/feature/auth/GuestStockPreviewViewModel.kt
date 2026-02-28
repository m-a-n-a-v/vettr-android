package com.vettr.android.feature.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.remote.PublicApi
import com.vettr.android.core.data.remote.StockPreviewDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Guest Stock Preview screen.
 * Loads limited stock preview data from the public (unauthenticated) API
 * for guest users who tapped a search result on the landing page.
 *
 * The ticker is extracted from the navigation argument via SavedStateHandle.
 */
@HiltViewModel
class GuestStockPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val publicApi: PublicApi
) : ViewModel() {

    private val ticker: String = savedStateHandle.get<String>("ticker") ?: ""

    private val _uiState = MutableStateFlow(GuestStockPreviewUiState())
    val uiState: StateFlow<GuestStockPreviewUiState> = _uiState.asStateFlow()

    init {
        if (ticker.isNotBlank()) {
            loadStockPreview()
        } else {
            _uiState.update { it.copy(errorMessage = "Invalid stock ticker") }
        }
    }

    /**
     * Load the stock preview from the public API.
     */
    private fun loadStockPreview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val response = publicApi.stockPreview(ticker)
                if (response.success && response.data != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            stockPreview = response.data
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to load stock data"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load stock preview"
                    )
                }
            }
        }
    }

    /**
     * Retry loading the stock preview.
     */
    fun retry() {
        loadStockPreview()
    }
}

/**
 * UI state for the Guest Stock Preview screen.
 */
@androidx.compose.runtime.Immutable
data class GuestStockPreviewUiState(
    val isLoading: Boolean = false,
    val stockPreview: StockPreviewDto? = null,
    val errorMessage: String? = null
)
