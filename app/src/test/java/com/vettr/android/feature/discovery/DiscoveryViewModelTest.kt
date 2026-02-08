package com.vettr.android.feature.discovery

import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Stock
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DiscoveryViewModel.
 * Tests filter toggle functionality and stock display updates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryViewModelTest {

    private lateinit var viewModel: DiscoveryViewModel
    private lateinit var stockRepository: StockRepository
    private lateinit var filingRepository: FilingRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        stockRepository = mockk()
        filingRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleFilter updates displayed stocks from watchlist to alerts`() = runTest {
        // Given
        val watchlistStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 250.50,
                priceChange = 2.5,
                vetrScore = 75,
                isFavorite = true
            )
        )

        val allStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 250.50,
                priceChange = 2.5,
                vetrScore = 75,
                isFavorite = true
            ),
            Stock(
                id = "stock-2",
                ticker = "AAPL",
                name = "Apple Inc",
                exchange = "NASDAQ",
                sector = "Technology",
                marketCap = 3000000000000.0,
                price = 175.25,
                priceChange = -1.2,
                vetrScore = 88,
                isFavorite = false
            )
        )

        // Mock repository to return favorites first, then all stocks
        coEvery { stockRepository.getFavorites() } returns flowOf(watchlistStocks)
        coEvery { stockRepository.getStocks() } returns flowOf(allStocks)

        viewModel = DiscoveryViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        // Verify initial state is WATCHLIST
        assertEquals(DiscoveryFilter.WATCHLIST, viewModel.selectedFilter.value)
        assertEquals(watchlistStocks, viewModel.stocks.value)

        // When - toggle filter
        viewModel.toggleFilter()
        advanceUntilIdle()

        // Then - filter should be ALERTS and stocks should be all stocks
        assertEquals(DiscoveryFilter.ALERTS, viewModel.selectedFilter.value)
        assertEquals(allStocks, viewModel.stocks.value)
    }

    @Test
    fun `toggleFilter updates from alerts back to watchlist`() = runTest {
        // Given
        val watchlistStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 250.50,
                priceChange = 2.5,
                vetrScore = 75,
                isFavorite = true
            )
        )

        val allStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 250.50,
                priceChange = 2.5,
                vetrScore = 75,
                isFavorite = true
            ),
            Stock(
                id = "stock-2",
                ticker = "AAPL",
                name = "Apple Inc",
                exchange = "NASDAQ",
                sector = "Technology",
                marketCap = 3000000000000.0,
                price = 175.25,
                priceChange = -1.2,
                vetrScore = 88,
                isFavorite = false
            )
        )

        coEvery { stockRepository.getFavorites() } returns flowOf(watchlistStocks)
        coEvery { stockRepository.getStocks() } returns flowOf(allStocks)

        viewModel = DiscoveryViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        // Toggle to ALERTS
        viewModel.toggleFilter()
        advanceUntilIdle()

        assertEquals(DiscoveryFilter.ALERTS, viewModel.selectedFilter.value)

        // When - toggle back to WATCHLIST
        viewModel.toggleFilter()
        advanceUntilIdle()

        // Then
        assertEquals(DiscoveryFilter.WATCHLIST, viewModel.selectedFilter.value)
        assertEquals(watchlistStocks, viewModel.stocks.value)
    }

    @Test
    fun `loadData populates stocks based on watchlist filter`() = runTest {
        // Given
        val favoriteStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 250.50,
                priceChange = 2.5,
                vetrScore = 75,
                isFavorite = true
            )
        )

        coEvery { stockRepository.getFavorites() } returns flowOf(favoriteStocks)

        // When
        viewModel = DiscoveryViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        // Then
        assertEquals(favoriteStocks, viewModel.stocks.value)
        assertEquals(DiscoveryFilter.WATCHLIST, viewModel.selectedFilter.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `searchStocks filters stocks by query`() = runTest {
        // Given
        val initialStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 250.50,
                priceChange = 2.5,
                vetrScore = 75,
                isFavorite = true
            )
        )

        val searchResults = listOf(
            Stock(
                id = "stock-2",
                ticker = "AAPL",
                name = "Apple Inc",
                exchange = "NASDAQ",
                sector = "Technology",
                marketCap = 3000000000000.0,
                price = 175.25,
                priceChange = -1.2,
                vetrScore = 88,
                isFavorite = false
            )
        )

        coEvery { stockRepository.getFavorites() } returns flowOf(initialStocks)
        coEvery { stockRepository.searchStocks("AAPL") } returns flowOf(searchResults)

        viewModel = DiscoveryViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        // When
        viewModel.searchStocks("AAPL")
        advanceUntilIdle()

        // Then
        assertEquals(searchResults, viewModel.stocks.value)
        assertEquals("AAPL", viewModel.searchQuery.value)
    }

    @Test
    fun `searchStocks with empty query reloads data`() = runTest {
        // Given
        val favoriteStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 250.50,
                priceChange = 2.5,
                vetrScore = 75,
                isFavorite = true
            )
        )

        coEvery { stockRepository.getFavorites() } returns flowOf(favoriteStocks)

        viewModel = DiscoveryViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        // When - search with empty query
        viewModel.searchStocks("")
        advanceUntilIdle()

        // Then - should reload favorites (current filter is WATCHLIST)
        assertEquals(favoriteStocks, viewModel.stocks.value)
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `refresh reloads data from repositories`() = runTest {
        // Given
        val initialStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 250.50,
                priceChange = 2.5,
                vetrScore = 75,
                isFavorite = true
            )
        )

        val updatedStocks = listOf(
            Stock(
                id = "stock-1",
                ticker = "TSLA",
                name = "Tesla Inc",
                exchange = "NASDAQ",
                sector = "Automotive",
                marketCap = 800000000000.0,
                price = 255.75,
                priceChange = 5.25,
                vetrScore = 78,
                isFavorite = true
            )
        )

        coEvery { stockRepository.getFavorites() } returnsMany listOf(
            flowOf(initialStocks),
            flowOf(updatedStocks)
        )

        viewModel = DiscoveryViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        assertEquals(initialStocks, viewModel.stocks.value)

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        assertEquals(updatedStocks, viewModel.stocks.value)
    }
}
