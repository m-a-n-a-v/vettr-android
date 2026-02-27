package com.vettr.android.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.PortfolioRepository
import com.vettr.android.core.model.Portfolio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PortfolioManagerViewModel @Inject constructor(
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _portfolios = MutableStateFlow<List<Portfolio>>(emptyList())
    val portfolios: StateFlow<List<Portfolio>> = _portfolios.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPortfolios()
    }

    private fun loadPortfolios() {
        viewModelScope.launch {
            portfolioRepository.getPortfolios()
                .catch { e ->
                    Timber.e(e, "Error loading portfolios")
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { list ->
                    _portfolios.value = list
                    _isLoading.value = false
                }
        }
    }

    fun deletePortfolio(id: String) {
        viewModelScope.launch {
            val result = portfolioRepository.deletePortfolio(id)
            result.onFailure { e ->
                _errorMessage.value = e.message
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            portfolioRepository.refreshPortfolios()
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
