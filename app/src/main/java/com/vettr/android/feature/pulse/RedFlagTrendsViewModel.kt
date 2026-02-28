package com.vettr.android.feature.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.remote.RedFlagTrendPointDto
import com.vettr.android.core.data.remote.RedFlagTrendResponse
import com.vettr.android.core.data.remote.VettrApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Time period options for red flag trend view.
 */
enum class TrendPeriod(val label: String, val apiValue: String) {
    ONE_WEEK("1W", "7d"),
    ONE_MONTH("1M", "30d"),
    THREE_MONTHS("3M", "90d"),
    SIX_MONTHS("6M", "180d")
}

data class RedFlagTrendsUiState(
    val trendResponse: RedFlagTrendResponse? = null,
    val selectedPeriod: TrendPeriod = TrendPeriod.ONE_MONTH,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for the Red Flag Global Trends screen.
 * Fetches market-wide red flag trend data with time period selection.
 */
@HiltViewModel
class RedFlagTrendsViewModel @Inject constructor(
    private val vettrApi: VettrApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedFlagTrendsUiState())
    val uiState: StateFlow<RedFlagTrendsUiState> = _uiState.asStateFlow()

    init {
        loadTrends()
    }

    fun selectPeriod(period: TrendPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadTrends()
    }

    fun loadTrends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = vettrApi.getRedFlagTrends(
                    period = _uiState.value.selectedPeriod.apiValue
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        trendResponse = response.body(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load trends"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load trends"
                )
            }
        }
    }
}
