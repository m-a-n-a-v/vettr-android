package com.vettr.android.feature.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.local.TokenManager
import com.vettr.android.core.data.remote.PortfolioSummaryResponse
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.PortfolioAlertsRepository
import com.vettr.android.core.data.repository.PortfolioInsightsRepository
import com.vettr.android.core.data.repository.PortfolioRepository
import com.vettr.android.core.data.repository.PulseRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Portfolio
import com.vettr.android.core.model.PortfolioAlert
import com.vettr.android.core.model.PortfolioInsight
import com.vettr.android.core.model.PulseSummary
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
 * Represents the portfolio state for conditional Pulse rendering.
 * - NoPortfolio: User has no real portfolios and no sample selected -> show SamplePortfolioPicker
 * - SampleOnly: User has a selected sample portfolio but no real ones -> show SamplePortfolioDashboard + "Connect" banner
 * - RealPortfolio: User has real portfolio(s) -> show full portfolio dashboard
 */
sealed class PulsePortfolioState {
    data object NoPortfolio : PulsePortfolioState()
    data class SampleOnly(val samplePortfolioId: String) : PulsePortfolioState()
    data object RealPortfolio : PulsePortfolioState()
}

/**
 * ViewModel for the Pulse screen.
 * Manages UI state for market overview, strategic events, trending stocks,
 * pulse summary (watchlist health, sector exposure, red flag categories),
 * portfolio summary, and portfolio-centric conditional rendering.
 */
@HiltViewModel
class PulseViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val filingRepository: FilingRepository,
    private val pulseRepository: PulseRepository,
    private val portfolioRepository: PortfolioRepository,
    private val portfolioAlertsRepository: PortfolioAlertsRepository,
    private val portfolioInsightsRepository: PortfolioInsightsRepository,
    private val observabilityService: ObservabilityService,
    private val networkMonitor: NetworkMonitor,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    private val _filings = MutableStateFlow<List<Filing>>(emptyList())
    val filings: StateFlow<List<Filing>> = _filings.asStateFlow()

    private val _pulseSummary = MutableStateFlow<PulseSummary?>(null)
    val pulseSummary: StateFlow<PulseSummary?> = _pulseSummary.asStateFlow()

    private val _portfolioSummary = MutableStateFlow<PortfolioSummaryResponse?>(null)
    val portfolioSummary: StateFlow<PortfolioSummaryResponse?> = _portfolioSummary.asStateFlow()

    private val _portfolioAlerts = MutableStateFlow<List<PortfolioAlert>>(emptyList())
    val portfolioAlerts: StateFlow<List<PortfolioAlert>> = _portfolioAlerts.asStateFlow()

    private val _unreadAlertCount = MutableStateFlow(0)
    val unreadAlertCount: StateFlow<Int> = _unreadAlertCount.asStateFlow()

    private val _portfolioInsights = MutableStateFlow<List<PortfolioInsight>>(emptyList())
    val portfolioInsights: StateFlow<List<PortfolioInsight>> = _portfolioInsights.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastUpdatedAt = MutableStateFlow<Long?>(null)
    val lastUpdatedAt: StateFlow<Long?> = _lastUpdatedAt.asStateFlow()

    // Portfolio state for conditional rendering
    private val _portfolioState = MutableStateFlow<PulsePortfolioState>(PulsePortfolioState.NoPortfolio)
    val portfolioState: StateFlow<PulsePortfolioState> = _portfolioState.asStateFlow()

    // User's real portfolios from local database
    private val _userPortfolios = MutableStateFlow<List<Portfolio>>(emptyList())
    val userPortfolios: StateFlow<List<Portfolio>> = _userPortfolios.asStateFlow()

    // Expose network connectivity state
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private var lastRefreshTime: Long = 0
    private val refreshDebounceMs = 10_000L // 10 seconds
    private var screenLoadStartTime: Long = 0

    init {
        screenLoadStartTime = System.currentTimeMillis()
        loadData()
        observeNetworkState()
        observePortfolioAlerts()
        observePortfolioInsights()
        observeUserPortfolios()
    }

    /**
     * Observe user's real portfolios to determine portfolio state.
     */
    private fun observeUserPortfolios() {
        viewModelScope.launch {
            portfolioRepository.getPortfolios()
                .catch { /* Non-critical, default to no portfolio */ }
                .collect { portfolios ->
                    _userPortfolios.value = portfolios
                    updatePortfolioState(portfolios)
                }
        }
    }

    /**
     * Update the portfolio state based on real portfolios and sample selection.
     */
    private fun updatePortfolioState(portfolios: List<Portfolio>) {
        val hasRealPortfolios = portfolios.isNotEmpty()
        val sampleId = tokenManager.getSamplePortfolioId()

        _portfolioState.value = when {
            hasRealPortfolios -> PulsePortfolioState.RealPortfolio
            sampleId != null -> PulsePortfolioState.SampleOnly(sampleId)
            else -> PulsePortfolioState.NoPortfolio
        }
    }

    /**
     * Called when user selects a sample portfolio from the picker.
     */
    fun selectSamplePortfolio(samplePortfolioId: String) {
        tokenManager.saveSamplePortfolioId(samplePortfolioId)
        _portfolioState.value = PulsePortfolioState.SampleOnly(samplePortfolioId)
    }

    /**
     * Observe portfolio alerts from local database (kept in sync by repository).
     */
    private fun observePortfolioAlerts() {
        viewModelScope.launch {
            portfolioAlertsRepository.getAlerts()
                .catch { /* Non-critical, ignore */ }
                .collect { alerts ->
                    _portfolioAlerts.value = alerts.take(5) // Show latest 5 on Pulse
                }
        }
        viewModelScope.launch {
            portfolioAlertsRepository.getUnreadCount()
                .catch { /* Non-critical, ignore */ }
                .collect { count ->
                    _unreadAlertCount.value = count
                }
        }
    }

    /**
     * Mark a portfolio alert as read.
     */
    fun markAlertRead(alertId: String) {
        viewModelScope.launch {
            portfolioAlertsRepository.markRead(alertId)
        }
    }

    /**
     * Observe portfolio insights from local database.
     */
    private fun observePortfolioInsights() {
        viewModelScope.launch {
            portfolioInsightsRepository.getInsights()
                .catch { /* Non-critical, ignore */ }
                .collect { insights ->
                    _portfolioInsights.value = insights.take(3) // Show top 3 on Pulse
                }
        }
    }

    /**
     * Dismiss a portfolio insight.
     */
    fun dismissInsight(insightId: String) {
        viewModelScope.launch {
            portfolioInsightsRepository.dismissInsight(insightId)
        }
    }

    /**
     * Observe network state changes and auto-refresh when network returns.
     */
    private fun observeNetworkState() {
        var wasOffline = false
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline && wasOffline) {
                    loadData()
                }
                wasOffline = !isOnline
            }
        }
    }

    /**
     * Load stocks, filings, and pulse summary data from repositories.
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

                            if (screenLoadStartTime > 0) {
                                val loadTime = System.currentTimeMillis() - screenLoadStartTime
                                observabilityService.trackScreenLoadTime("Pulse", loadTime)
                                screenLoadStartTime = 0
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
                            val watchlistedStockIds = _stocks.value.map { it.id }.toSet()
                            _filings.value = allFilings.filter { it.stockId in watchlistedStockIds }
                        }
                }

                // Fetch pulse summary from backend
                launch {
                    loadPulseSummary()
                }

                // Fetch portfolio summary
                launch {
                    loadPortfolioSummary()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load pulse summary data from the backend.
     * Falls back gracefully to null if the API call fails.
     */
    private suspend fun loadPulseSummary() {
        try {
            _pulseSummary.value = pulseRepository.getPulseSummary()
        } catch (_: Exception) {
            // Pulse summary is non-critical; continue with client-side fallbacks
            _pulseSummary.value = null
        }
    }

    /**
     * Load portfolio summary data from the backend.
     * Falls back gracefully to null if the API call fails (user may not have portfolios).
     */
    private suspend fun loadPortfolioSummary() {
        try {
            val result = portfolioRepository.getPortfolioSummary()
            result.onSuccess { summary ->
                _portfolioSummary.value = summary
            }.onFailure {
                // Portfolio summary is non-critical; user may not have portfolios yet
                _portfolioSummary.value = null
            }
        } catch (_: Exception) {
            _portfolioSummary.value = null
        }
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
