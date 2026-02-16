package com.vettr.android.feature.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import com.vettr.android.core.util.ObservabilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Discovery screen.
 * Loads stocks and filings from repositories, supports search and sector filtering.
 */
@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val filingRepository: FilingRepository,
    private val observabilityService: ObservabilityService
) : ViewModel() {

    // All stocks from API (unfiltered source of truth)
    private val _allStocks = MutableStateFlow<List<Stock>>(emptyList())

    // All filings from API (unfiltered source of truth)
    private val _allFilings = MutableStateFlow<List<Filing>>(emptyList())

    // Filtered stocks visible to the UI
    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    // Filtered filings visible to the UI
    private val _filings = MutableStateFlow<List<Filing>>(emptyList())
    val filings: StateFlow<List<Filing>> = _filings.asStateFlow()

    // Dynamic sector list extracted from stock data
    private val _sectors = MutableStateFlow<List<String>>(emptyList())
    val sectors: StateFlow<List<String>> = _sectors.asStateFlow()

    // Currently selected sectors (empty set = "All" = no filter)
    private val _selectedSectors = MutableStateFlow<Set<String>>(emptySet())
    val selectedSectors: StateFlow<Set<String>> = _selectedSectors.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastUpdatedAt = MutableStateFlow<Long?>(null)
    val lastUpdatedAt: StateFlow<Long?> = _lastUpdatedAt.asStateFlow()

    private var lastRefreshTime: Long = 0
    private val refreshDebounceMs = 10_000L // 10 seconds
    private var screenLoadStartTime: Long = 0

    init {
        screenLoadStartTime = System.currentTimeMillis()
        loadData()
    }

    /**
     * Load stocks and filings data from repositories.
     */
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Collect stocks
                launch {
                    stockRepository.getStocks()
                        .catch { error ->
                            _errorMessage.value = "Failed to load stocks: ${error.message}"
                        }
                        .collect { stockList ->
                            _allStocks.value = stockList
                            _lastUpdatedAt.value = System.currentTimeMillis()

                            // Extract unique sectors dynamically
                            val uniqueSectors = stockList
                                .map { it.sector }
                                .distinct()
                                .sorted()
                            _sectors.value = listOf("All") + uniqueSectors

                            // Apply current filters
                            applyFilters()

                            // Track screen load time on first data load
                            if (screenLoadStartTime > 0) {
                                val loadTime = System.currentTimeMillis() - screenLoadStartTime
                                observabilityService.trackScreenLoadTime("Discovery", loadTime)
                                screenLoadStartTime = 0
                            }
                        }
                }

                // Collect latest filings
                launch {
                    filingRepository.getLatestFilings(limit = 20)
                        .catch { error ->
                            _errorMessage.value = "Failed to load filings: ${error.message}"
                        }
                        .collect { filingList ->
                            _allFilings.value = filingList
                            applyFilters()
                        }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update search query and refilter.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /**
     * Toggle a sector in the multi-select filter.
     */
    fun toggleSector(sector: String) {
        _selectedSectors.value = if (sector in _selectedSectors.value) {
            _selectedSectors.value - sector
        } else {
            _selectedSectors.value + sector
        }
        applyFilters()
    }

    /**
     * Clear all sector selections (equivalent to "All").
     */
    fun clearSectors() {
        _selectedSectors.value = emptySet()
        applyFilters()
    }

    /**
     * Apply search query and sector filter to stocks and filings.
     */
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase().trim()
        val sectors = _selectedSectors.value
        val allStocks = _allStocks.value
        val allFilings = _allFilings.value

        // Filter stocks by multiple sectors
        val sectorFiltered = if (sectors.isEmpty()) allStocks
        else allStocks.filter { stock -> stock.sector in sectors }

        // Filter stocks by search query (ticker or name)
        val filteredStocks = if (query.isEmpty()) sectorFiltered
        else sectorFiltered.filter {
            it.ticker.lowercase().contains(query) ||
                it.name.lowercase().contains(query) ||
                it.sector.lowercase().contains(query)
        }

        _stocks.value = filteredStocks

        // Build a set of stock IDs matching the sector filter
        val sectorStockIds = sectorFiltered.map { it.id }.toSet()

        // Filter filings: match sector via stockId, and search query in title
        val filteredFilings = allFilings.filter { filing ->
            val matchesSector = sectors.isEmpty() || filing.stockId in sectorStockIds
            val matchesQuery = query.isEmpty() || run {
                val stock = allStocks.find { it.id == filing.stockId }
                filing.title.lowercase().contains(query) ||
                    filing.type.lowercase().contains(query) ||
                    (stock?.ticker?.lowercase()?.contains(query) == true) ||
                    (stock?.name?.lowercase()?.contains(query) == true)
            }
            matchesSector && matchesQuery
        }

        _filings.value = filteredFilings.sortedByDescending { it.date }.take(10)
    }

    /**
     * Refresh data by reloading from repositories.
     * Implements debounce logic to prevent more than 1 refresh per 10 seconds.
     */
    fun refresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < refreshDebounceMs) {
            return
        }
        lastRefreshTime = currentTime
        loadData()
    }
}
