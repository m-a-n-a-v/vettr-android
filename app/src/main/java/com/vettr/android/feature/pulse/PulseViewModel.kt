package com.vettr.android.feature.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import com.vettr.android.core.util.NetworkMonitor
import com.vettr.android.core.util.ObservabilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Pulse screen.
 * Manages UI state for market overview, strategic events, and trending stocks.
 */
@HiltViewModel
class PulseViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val filingRepository: FilingRepository,
    private val observabilityService: ObservabilityService,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    private val _filings = MutableStateFlow<List<Filing>>(emptyList())
    val filings: StateFlow<List<Filing>> = _filings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastUpdatedAt = MutableStateFlow<Long?>(null)
    val lastUpdatedAt: StateFlow<Long?> = _lastUpdatedAt.asStateFlow()

    // Expose network connectivity state
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private var lastRefreshTime: Long = 0
    private val refreshDebounceMs = 10_000L // 10 seconds
    private var screenLoadStartTime: Long = 0

    init {
        screenLoadStartTime = System.currentTimeMillis()
        loadData()
        observeNetworkState()
    }

    /**
     * Observe network state changes and auto-refresh when network returns.
     */
    private fun observeNetworkState() {
        var wasOffline = false
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline && wasOffline) {
                    // Network has returned, trigger refresh
                    loadData()
                }
                wasOffline = !isOnline
            }
        }
    }

    /**
     * Load stocks and filings data from repositories.
     * Combines both flows and updates UI state accordingly.
     */
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Collect watchlisted stocks only (favorites)
                launch {
                    stockRepository.getFavorites()
                        .catch { error ->
                            _errorMessage.value = "Failed to load stocks: ${error.message}"
                        }
                        .collect { stockList ->
                            _stocks.value = stockList
                            _lastUpdatedAt.value = System.currentTimeMillis()

                            // Track screen load time on first data load
                            if (screenLoadStartTime > 0) {
                                val loadTime = System.currentTimeMillis() - screenLoadStartTime
                                observabilityService.trackScreenLoadTime("Pulse", loadTime)
                                screenLoadStartTime = 0 // Only track once
                            }
                        }
                }

                // Collect latest filings and filter to watchlist stocks only
                launch {
                    filingRepository.getLatestFilings(limit = 10)
                        .catch { error ->
                            _errorMessage.value = "Failed to load filings: ${error.message}"
                        }
                        .collect { allFilings ->
                            // Filter filings to only those matching watchlisted stock IDs
                            val watchlistedStockIds = _stocks.value.map { it.id }.toSet()
                            _filings.value = allFilings.filter { it.stockId in watchlistedStockIds }
                        }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh data by reloading from repositories.
     * Implements debounce logic to prevent more than 1 refresh per 10 seconds.
     */
    fun refresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < refreshDebounceMs) {
            // Skip refresh if within debounce window
            return
        }
        lastRefreshTime = currentTime
        loadData()
    }
}
