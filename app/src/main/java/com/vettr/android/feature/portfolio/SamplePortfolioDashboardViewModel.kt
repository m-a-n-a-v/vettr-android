package com.vettr.android.feature.portfolio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.remote.SamplePortfolioResponse
import com.vettr.android.core.data.repository.SamplePortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SamplePortfolioDashboardUiState(
    val portfolio: SamplePortfolioResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SamplePortfolioDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val samplePortfolioRepository: SamplePortfolioRepository
) : ViewModel() {

    private val portfolioId: String = savedStateHandle.get<String>("samplePortfolioId") ?: ""

    private val _uiState = MutableStateFlow(SamplePortfolioDashboardUiState())
    val uiState: StateFlow<SamplePortfolioDashboardUiState> = _uiState.asStateFlow()

    init {
        loadPortfolio()
    }

    private fun loadPortfolio() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = samplePortfolioRepository.getSamplePortfolios()
            result.onSuccess { portfolios ->
                val portfolio = portfolios.find { it.id == portfolioId }
                if (portfolio != null) {
                    _uiState.value = _uiState.value.copy(
                        portfolio = portfolio,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Sample portfolio not found"
                    )
                }
            }.onFailure { e ->
                Timber.e(e, "Failed to load sample portfolio")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load sample portfolio"
                )
            }
        }
    }
}
