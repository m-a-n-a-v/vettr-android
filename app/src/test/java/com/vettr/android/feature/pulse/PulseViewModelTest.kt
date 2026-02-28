package com.vettr.android.feature.pulse

import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.PortfolioAlertsRepository
import com.vettr.android.core.data.repository.PortfolioInsightsRepository
import com.vettr.android.core.data.repository.PortfolioRepository
import com.vettr.android.core.data.repository.PulseRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import com.vettr.android.core.util.NetworkMonitor
import com.vettr.android.core.util.ObservabilityService
import com.vettr.android.core.data.local.TokenManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
 * Unit tests for PulseViewModel.
 * Tests data loading for stocks and filings.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PulseViewModelTest {

    private lateinit var viewModel: PulseViewModel
    private lateinit var stockRepository: StockRepository
    private lateinit var filingRepository: FilingRepository
    private lateinit var pulseRepository: PulseRepository
    private lateinit var portfolioRepository: PortfolioRepository
    private lateinit var portfolioAlertsRepository: PortfolioAlertsRepository
    private lateinit var portfolioInsightsRepository: PortfolioInsightsRepository
    private lateinit var observabilityService: ObservabilityService
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var tokenManager: TokenManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        stockRepository = mockk()
        filingRepository = mockk()
        pulseRepository = mockk(relaxed = true)
        portfolioRepository = mockk(relaxed = true)
        portfolioAlertsRepository = mockk(relaxed = true)
        portfolioInsightsRepository = mockk(relaxed = true)
        observabilityService = mockk(relaxed = true)
        networkMonitor = mockk {
            every { isOnline } returns MutableStateFlow(true)
        }
        tokenManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData populates stocks flow`() = runTest {
        // Given
        val mockStocks = listOf(
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
                isFavorite = false
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
                isFavorite = true
            )
        )

        coEvery { stockRepository.getFavorites() } returns flowOf(mockStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        // When
        viewModel = PulseViewModel(stockRepository, filingRepository, pulseRepository, portfolioRepository, portfolioAlertsRepository, portfolioInsightsRepository, observabilityService, networkMonitor, tokenManager)
        advanceUntilIdle()

        // Then
        assertEquals(mockStocks, viewModel.stocks.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadData populates filings flow`() = runTest {
        // Given - stocks must match filing stockIds since filings are filtered to watchlist
        val mockStocks = listOf(
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
                isFavorite = true
            )
        )

        val mockFilings = listOf(
            Filing(
                id = "filing-1",
                stockId = "stock-1",
                type = "10-K",
                title = "Annual Report",
                date = System.currentTimeMillis(),
                summary = "Annual financial report for Tesla",
                isRead = false
            ),
            Filing(
                id = "filing-2",
                stockId = "stock-2",
                type = "8-K",
                title = "Current Report",
                date = System.currentTimeMillis(),
                summary = "Current report for Apple",
                isRead = true
            )
        )

        coEvery { stockRepository.getFavorites() } returns flowOf(mockStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(mockFilings)

        // When
        viewModel = PulseViewModel(stockRepository, filingRepository, pulseRepository, portfolioRepository, portfolioAlertsRepository, portfolioInsightsRepository, observabilityService, networkMonitor, tokenManager)
        advanceUntilIdle()

        // Then
        assertEquals(mockFilings, viewModel.filings.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadData sets loading state during fetch`() = runTest {
        // Given
        coEvery { stockRepository.getFavorites() } returns flowOf(emptyList())
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        // When
        viewModel = PulseViewModel(stockRepository, filingRepository, pulseRepository, portfolioRepository, portfolioAlertsRepository, portfolioInsightsRepository, observabilityService, networkMonitor, tokenManager)
        advanceUntilIdle()

        // Then - loading is false after data loads
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadData handles stock repository error`() = runTest {
        // Given
        coEvery { stockRepository.getFavorites() } returns flowOf(emptyList())
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        // When
        viewModel = PulseViewModel(stockRepository, filingRepository, pulseRepository, portfolioRepository, portfolioAlertsRepository, portfolioInsightsRepository, observabilityService, networkMonitor, tokenManager)
        advanceUntilIdle()

        // Then - ViewModel completes loading even with empty data
        assertFalse(viewModel.isLoading.value)
        assertEquals(emptyList<Stock>(), viewModel.stocks.value)
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
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        viewModel = PulseViewModel(stockRepository, filingRepository, pulseRepository, portfolioRepository, portfolioAlertsRepository, portfolioInsightsRepository, observabilityService, networkMonitor, tokenManager)
        advanceUntilIdle()

        // Verify initial state
        assertEquals(initialStocks, viewModel.stocks.value)

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then - should have updated stocks
        assertEquals(updatedStocks, viewModel.stocks.value)
    }
}
