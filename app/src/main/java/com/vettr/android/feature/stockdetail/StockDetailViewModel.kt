package com.vettr.android.feature.stockdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.VetrScoreResult
import com.vettr.android.core.data.repository.ExecutiveRepository
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.PeerComparison
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.data.repository.VetrScoreRepository
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import com.vettr.android.core.util.HapticService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tab options for Stock Detail screen.
 */
enum class StockDetailTab {
    OVERVIEW,
    PEDIGREE,
    RED_FLAGS
}

/**
 * Time range options for the price chart.
 */
enum class TimeRange {
    ONE_DAY,
    ONE_WEEK,
    ONE_MONTH,
    ONE_YEAR
}

/**
 * ViewModel for the Stock Detail screen.
 * Manages UI state for stock details, filings, and tab navigation.
 */
@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val filingRepository: FilingRepository,
    private val executiveRepository: ExecutiveRepository,
    private val vetrScoreRepository: VetrScoreRepository,
    val hapticService: HapticService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stockId: String = savedStateHandle.get<String>("stockId") ?: ""

    private val _stock = MutableStateFlow<Stock?>(null)
    val stock: StateFlow<Stock?> = _stock.asStateFlow()

    private val _filings = MutableStateFlow<List<Filing>>(emptyList())
    val filings: StateFlow<List<Filing>> = _filings.asStateFlow()

    private val _executives = MutableStateFlow<List<Executive>>(emptyList())
    val executives: StateFlow<List<Executive>> = _executives.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedTab = MutableStateFlow(StockDetailTab.OVERVIEW)
    val selectedTab: StateFlow<StockDetailTab> = _selectedTab.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.ONE_DAY)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _vetrScoreResult = MutableStateFlow<VetrScoreResult?>(null)
    val vetrScoreResult: StateFlow<VetrScoreResult?> = _vetrScoreResult.asStateFlow()

    private val _scoreHistory = MutableStateFlow<List<Int>>(emptyList())
    val scoreHistory: StateFlow<List<Int>> = _scoreHistory.asStateFlow()

    private val _scoreTrend = MutableStateFlow<String>("Stable")
    val scoreTrend: StateFlow<String> = _scoreTrend.asStateFlow()

    private val _peerComparison = MutableStateFlow<PeerComparison?>(null)
    val peerComparison: StateFlow<PeerComparison?> = _peerComparison.asStateFlow()

    init {
        loadStock()
    }

    /**
     * Load stock details and associated filings from repositories.
     */
    fun loadStock() {
        if (stockId.isEmpty()) {
            _errorMessage.value = "Invalid stock ID"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Load stock details
                launch {
                    stockRepository.getStock(stockId)
                        .catch { error ->
                            _errorMessage.value = "Failed to load stock: ${error.message}"
                        }
                        .collect { stockData ->
                            _stock.value = stockData
                        }
                }

                // Load filings for this stock
                launch {
                    filingRepository.getFilingsForStock(stockId)
                        .catch { error ->
                            _errorMessage.value = "Failed to load filings: ${error.message}"
                        }
                        .collect { filingList ->
                            _filings.value = filingList
                        }
                }

                // Load executives for this stock
                launch {
                    executiveRepository.getExecutivesForStock(stockId)
                        .catch { error ->
                            _errorMessage.value = "Failed to load executives: ${error.message}"
                        }
                        .collect { executiveList ->
                            _executives.value = executiveList
                        }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle favorite status for the current stock.
     */
    fun toggleFavorite(view: android.view.View?) {
        if (stockId.isEmpty()) return

        viewModelScope.launch {
            try {
                stockRepository.toggleFavorite(stockId)
                // Medium haptic for successful favorite toggle
                hapticService.medium(view)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle favorite: ${e.message}"
                // Error haptic for failure
                hapticService.error(view)
            }
        }
    }

    /**
     * Select a tab on the stock detail screen.
     * @param tab The tab to select
     */
    fun selectTab(tab: StockDetailTab) {
        _selectedTab.value = tab
    }

    /**
     * Select a time range for the price chart.
     * @param timeRange The time range to select
     */
    fun selectTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
    }

    /**
     * Refresh all stock detail data (pull-to-refresh).
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadStock()
            _isRefreshing.value = false
        }
    }

    /**
     * Load VETR Score details for the current stock.
     * Fetches score calculation, history, trend, and peer comparison.
     */
    fun loadVetrScoreDetails() {
        val currentStock = _stock.value
        if (currentStock == null) {
            _errorMessage.value = "Stock not loaded"
            return
        }

        viewModelScope.launch {
            try {
                // Calculate current score
                launch {
                    val scoreResult = vetrScoreRepository.calculateScore(currentStock.ticker)
                    _vetrScoreResult.value = scoreResult
                }

                // Load score history (last 30 days)
                launch {
                    vetrScoreRepository.getScoreHistory(currentStock.ticker, months = 1)
                        .catch { error ->
                            _errorMessage.value = "Failed to load score history: ${error.message}"
                        }
                        .collect { history ->
                            _scoreHistory.value = history.map { it.overallScore }
                        }
                }

                // Get score trend
                launch {
                    val trend = vetrScoreRepository.getScoreTrend(currentStock.ticker)
                    _scoreTrend.value = when (trend.direction) {
                        com.vettr.android.core.data.repository.ScoreTrendDirection.IMPROVING -> "Trending up"
                        com.vettr.android.core.data.repository.ScoreTrendDirection.DECLINING -> "Trending down"
                        com.vettr.android.core.data.repository.ScoreTrendDirection.STABLE -> "Stable"
                    }
                }

                // Get peer comparison
                launch {
                    val comparison = vetrScoreRepository.compareScores(currentStock.ticker)
                    _peerComparison.value = comparison
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load VETR score details: ${e.message}"
            }
        }
    }
}
