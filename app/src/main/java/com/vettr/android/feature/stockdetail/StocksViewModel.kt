package com.vettr.android.feature.stockdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Stock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Stocks list screen.
 * Manages stock list data, search functionality, and loading state.
 */
@HiltViewModel
class StocksViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _allStocks = MutableStateFlow<List<Stock>>(emptyList())
    val allStocks: StateFlow<List<Stock>> = _allStocks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Computed filtered list based on search query.
     * Filters stocks by ticker or name matching the search query.
     */
    val filteredStocks: StateFlow<List<Stock>> = combine(
        _allStocks,
        _searchQuery
    ) { stocks, query ->
        if (query.isBlank()) {
            stocks
        } else {
            stocks.filter { stock ->
                stock.ticker.contains(query, ignoreCase = true) ||
                stock.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadStocks()
    }

    /**
     * Load all stocks from repository.
     * Updates allStocks StateFlow with fetched data.
     */
    fun loadStocks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                stockRepository.getStocks()
                    .catch { error ->
                        // Handle error silently or emit error state if needed
                        _isLoading.value = false
                    }
                    .collect { stockList ->
                        _allStocks.value = stockList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update search query to filter stocks.
     * @param query Search query string
     */
    fun searchStocks(query: String) {
        _searchQuery.value = query
    }
}
