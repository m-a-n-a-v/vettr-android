package com.vettr.android.feature.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Stock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Discovery screen.
 * Manages UI state for stock discovery, filtering, and search functionality.
 */
@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val filingRepository: FilingRepository
) : ViewModel() {

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    private val _sectors = MutableStateFlow<List<String>>(
        listOf(
            "Critical Minerals",
            "AI Technology",
            "Energy Juniors",
            "Clean Tech"
        )
    )
    val sectors: StateFlow<List<String>> = _sectors.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DiscoveryFilter.WATCHLIST)
    val selectedFilter: StateFlow<DiscoveryFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load stocks data from repository based on current filter.
     */
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (_selectedFilter.value) {
                    DiscoveryFilter.WATCHLIST -> {
                        stockRepository.getFavorites()
                            .catch { error ->
                                _errorMessage.value = "Failed to load watchlist: ${error.message}"
                            }
                            .collect { stockList ->
                                _stocks.value = stockList
                            }
                    }
                    DiscoveryFilter.ALERTS -> {
                        // For now, just load all stocks
                        // TODO: Filter by stocks with active alerts when AlertRepository is available
                        stockRepository.getStocks()
                            .catch { error ->
                                _errorMessage.value = "Failed to load alerts: ${error.message}"
                            }
                            .collect { stockList ->
                                _stocks.value = stockList
                            }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle between Watchlist and Alerts filters.
     */
    fun toggleFilter() {
        _selectedFilter.update { currentFilter ->
            when (currentFilter) {
                DiscoveryFilter.WATCHLIST -> DiscoveryFilter.ALERTS
                DiscoveryFilter.ALERTS -> DiscoveryFilter.WATCHLIST
            }
        }
        loadData()
    }

    /**
     * Search stocks by query string.
     * @param query Search query (ticker or name)
     */
    fun searchStocks(query: String) {
        _searchQuery.value = query

        if (query.isEmpty()) {
            loadData()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                stockRepository.searchStocks(query)
                    .catch { error ->
                        _errorMessage.value = "Search failed: ${error.message}"
                    }
                    .collect { searchResults ->
                        _stocks.value = searchResults
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh data by reloading from repositories.
     */
    fun refresh() {
        loadData()
    }
}

/**
 * Discovery filter options.
 */
enum class DiscoveryFilter {
    WATCHLIST,
    ALERTS
}
