package com.vettr.android.feature.portfolio

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

data class SamplePortfolioPickerUiState(
    val portfolios: List<SamplePortfolioResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SamplePortfolioPickerViewModel @Inject constructor(
    private val samplePortfolioRepository: SamplePortfolioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SamplePortfolioPickerUiState())
    val uiState: StateFlow<SamplePortfolioPickerUiState> = _uiState.asStateFlow()

    init {
        loadSamplePortfolios()
    }

    fun loadSamplePortfolios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = samplePortfolioRepository.getSamplePortfolios()
            result.onSuccess { portfolios ->
                _uiState.value = _uiState.value.copy(
                    portfolios = portfolios,
                    isLoading = false
                )
            }.onFailure { e ->
                Timber.e(e, "Failed to load sample portfolios")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load sample portfolios"
                )
            }
        }
    }
}
