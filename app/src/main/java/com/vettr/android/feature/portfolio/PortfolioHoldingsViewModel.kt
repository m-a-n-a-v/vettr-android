package com.vettr.android.feature.portfolio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.PortfolioRepository
import com.vettr.android.core.model.PortfolioHolding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PortfolioHoldingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    val portfolioId: String = savedStateHandle.get<String>("portfolioId") ?: ""

    private val _holdings = MutableStateFlow<List<PortfolioHolding>>(emptyList())
    val holdings: StateFlow<List<PortfolioHolding>> = _holdings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        if (portfolioId.isNotEmpty()) {
            loadHoldings()
        }
    }

    private fun loadHoldings() {
        viewModelScope.launch {
            portfolioRepository.getHoldings(portfolioId)
                .catch { e ->
                    Timber.e(e, "Error loading holdings")
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { list ->
                    _holdings.value = list
                    _isLoading.value = false
                }
        }
    }

    fun addHolding(ticker: String, quantity: Double, avgCost: Double) {
        viewModelScope.launch {
            val result = portfolioRepository.addHolding(portfolioId, ticker, quantity, avgCost)
            result.onFailure { e ->
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteHolding(holdingId: String) {
        viewModelScope.launch {
            val result = portfolioRepository.deleteHolding(portfolioId, holdingId)
            result.onFailure { e ->
                _errorMessage.value = e.message
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            portfolioRepository.refreshHoldings(portfolioId)
            _isLoading.value = false
        }
    }
}
