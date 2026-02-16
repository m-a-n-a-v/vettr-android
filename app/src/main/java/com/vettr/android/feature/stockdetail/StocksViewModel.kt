package com.vettr.android.feature.stockdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Stock
import com.vettr.android.core.util.PaginatedState
import com.vettr.android.core.util.PaginationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Stocks list screen.
 * Manages stock list data, search functionality, and loading state.
 */
@HiltViewModel
class StocksViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _allStocks = MutableStateFlow<List<Stock>>(emptyList())
    val allStocks: StateFlow<List<Stock>> = _allStocks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastUpdatedAt = MutableStateFlow<Long?>(null)
    val lastUpdatedAt: StateFlow<Long?> = _lastUpdatedAt.asStateFlow()

    private var lastRefreshTime: Long = 0
    private val refreshDebounceMs = 10_000L // 10 seconds

    /**
     * Favorite stocks flow from repository.
     */
    val favoriteStocks: StateFlow<List<Stock>> = stockRepository.getFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Count of favorite stocks.
     */
    val favoriteCount: StateFlow<Int> = favoriteStocks
        .combine(flow { emit(Unit) }) { favorites, _ ->
            favorites.size
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * Watchlist limit based on user's tier.
     */
    val watchlistLimit: StateFlow<Int> = authRepository.getCurrentUser()
        .combine(flow { emit(Unit) }) { user, _ ->
            val tierString = user?.tier?.uppercase() ?: "FREE"
            try {
                com.vettr.android.core.model.VettrTier.valueOf(tierString).watchlistLimit
            } catch (e: IllegalArgumentException) {
                com.vettr.android.core.model.VettrTier.FREE.watchlistLimit
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.vettr.android.core.model.VettrTier.FREE.watchlistLimit
        )

    // Pagination state
    private val _paginatedState = MutableStateFlow(PaginatedState<Stock>())
    val paginatedState: StateFlow<PaginatedState<Stock>> = _paginatedState.asStateFlow()

    // Job for canceling in-flight requests
    private var loadJob: Job? = null

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
                        _lastUpdatedAt.value = System.currentTimeMillis()
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh stocks by reloading from repository.
     * Implements debounce logic to prevent more than 1 refresh per 10 seconds.
     */
    fun refresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < refreshDebounceMs) {
            // Skip refresh if within debounce window
            return
        }
        lastRefreshTime = currentTime
        loadStocks()
    }

    /**
     * Update search query to filter stocks.
     * @param query Search query string
     */
    fun searchStocks(query: String) {
        _searchQuery.value = query
    }

    /**
     * Load the first page of stocks using pagination.
     * Cancels any in-flight requests before starting a new one.
     */
    fun loadStocksPaginated() {
        // Cancel any previous load operation
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _paginatedState.value = _paginatedState.value.copy(
                isLoading = true,
                error = null,
                currentPage = 0,
                items = emptyList()
            )

            try {
                val stocks = stockRepository.getStocksPaginated(
                    limit = PaginationHelper.PAGE_SIZE,
                    offset = 0
                )

                _paginatedState.value = _paginatedState.value.copy(
                    items = stocks,
                    isLoading = false,
                    currentPage = 0,
                    hasMorePages = stocks.size == PaginationHelper.PAGE_SIZE
                )
            } catch (e: Exception) {
                _paginatedState.value = _paginatedState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load stocks"
                )
            }
        }
    }

    /**
     * Load the next page of stocks.
     * Only loads if not already loading and if there are more pages.
     */
    fun loadMoreStocks() {
        val currentState = _paginatedState.value

        // Don't load if already loading or if no more pages
        if (currentState.isLoadingMore || !currentState.hasMorePages) {
            return
        }

        viewModelScope.launch {
            _paginatedState.value = currentState.copy(isLoadingMore = true)

            try {
                val nextPage = currentState.currentPage + 1
                val newStocks = stockRepository.getStocksPaginated(
                    limit = PaginationHelper.PAGE_SIZE,
                    offset = PaginationHelper.calculateOffset(nextPage)
                )

                _paginatedState.value = _paginatedState.value.copy(
                    items = currentState.items + newStocks,
                    isLoadingMore = false,
                    currentPage = nextPage,
                    hasMorePages = newStocks.size == PaginationHelper.PAGE_SIZE
                )
            } catch (e: Exception) {
                _paginatedState.value = _paginatedState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Failed to load more stocks"
                )
            }
        }
    }

    /**
     * Search stocks with pagination.
     * Cancels any in-flight requests before starting a new search.
     * @param query Search query string
     */
    fun searchStocksPaginated(query: String) {
        // Cancel any previous load operation
        loadJob?.cancel()

        _searchQuery.value = query

        // If query is empty, load all stocks
        if (query.isBlank()) {
            loadStocksPaginated()
            return
        }

        loadJob = viewModelScope.launch {
            _paginatedState.value = _paginatedState.value.copy(
                isLoading = true,
                error = null,
                currentPage = 0,
                items = emptyList()
            )

            try {
                val stocks = stockRepository.searchStocksPaginated(
                    query = query,
                    limit = PaginationHelper.PAGE_SIZE,
                    offset = 0
                )

                _paginatedState.value = _paginatedState.value.copy(
                    items = stocks,
                    isLoading = false,
                    currentPage = 0,
                    hasMorePages = stocks.size == PaginationHelper.PAGE_SIZE
                )
            } catch (e: Exception) {
                _paginatedState.value = _paginatedState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to search stocks"
                )
            }
        }
    }

    /**
     * Load more search results.
     */
    fun loadMoreSearchResults() {
        val currentState = _paginatedState.value
        val query = _searchQuery.value

        // Don't load if query is empty, already loading, or no more pages
        if (query.isBlank() || currentState.isLoadingMore || !currentState.hasMorePages) {
            return
        }

        viewModelScope.launch {
            _paginatedState.value = currentState.copy(isLoadingMore = true)

            try {
                val nextPage = currentState.currentPage + 1
                val newStocks = stockRepository.searchStocksPaginated(
                    query = query,
                    limit = PaginationHelper.PAGE_SIZE,
                    offset = PaginationHelper.calculateOffset(nextPage)
                )

                _paginatedState.value = _paginatedState.value.copy(
                    items = currentState.items + newStocks,
                    isLoadingMore = false,
                    currentPage = nextPage,
                    hasMorePages = newStocks.size == PaginationHelper.PAGE_SIZE
                )
            } catch (e: Exception) {
                _paginatedState.value = _paginatedState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Failed to load more search results"
                )
            }
        }
    }
}
