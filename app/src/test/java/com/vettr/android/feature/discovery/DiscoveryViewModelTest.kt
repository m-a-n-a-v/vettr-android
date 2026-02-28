package com.vettr.android.feature.discovery

import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Stock
import com.vettr.android.core.util.ObservabilityService
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DiscoveryViewModel.
 * Tests stock loading, search, and sector filtering functionality.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryViewModelTest {

    private lateinit var viewModel: DiscoveryViewModel
    private lateinit var stockRepository: StockRepository
    private lateinit var filingRepository: FilingRepository
    private lateinit var observabilityService: ObservabilityService
    private lateinit var api: VettrApi

    private val testDispatcher = StandardTestDispatcher()

    private val testStocks = listOf(
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        stockRepository = mockk()
        filingRepository = mockk()
        observabilityService = mockk(relaxed = true)
        api = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData populates stocks flow`() = runTest {
        // Given
        coEvery { stockRepository.getStocks() } returns flowOf(testStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        // When
        viewModel = DiscoveryViewModel(stockRepository, filingRepository, observabilityService, api)
        advanceUntilIdle()

        // Then
        assertEquals(testStocks, viewModel.stocks.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadData extracts unique sectors from stocks`() = runTest {
        // Given
        coEvery { stockRepository.getStocks() } returns flowOf(testStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        // When
        viewModel = DiscoveryViewModel(stockRepository, filingRepository, observabilityService, api)
        advanceUntilIdle()

        // Then - sectors should be extracted from stock data
        val sectors = viewModel.sectors.value
        assertTrue(sectors.contains("Automotive"))
        assertTrue(sectors.contains("Technology"))
    }

    @Test
    fun `updateSearchQuery filters stocks by query`() = runTest {
        // Given
        coEvery { stockRepository.getStocks() } returns flowOf(testStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        viewModel = DiscoveryViewModel(stockRepository, filingRepository, observabilityService, api)
        advanceUntilIdle()

        // When - search for AAPL
        viewModel.updateSearchQuery("AAPL")
        advanceUntilIdle()

        // Then
        assertEquals("AAPL", viewModel.searchQuery.value)
        assertEquals(1, viewModel.stocks.value.size)
        assertEquals("AAPL", viewModel.stocks.value.first().ticker)
    }

    @Test
    fun `updateSearchQuery with empty string shows all stocks`() = runTest {
        // Given
        coEvery { stockRepository.getStocks() } returns flowOf(testStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        viewModel = DiscoveryViewModel(stockRepository, filingRepository, observabilityService, api)
        advanceUntilIdle()

        // Search first
        viewModel.updateSearchQuery("AAPL")
        advanceUntilIdle()
        assertEquals(1, viewModel.stocks.value.size)

        // When - clear search
        viewModel.updateSearchQuery("")
        advanceUntilIdle()

        // Then - should show all stocks
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(testStocks.size, viewModel.stocks.value.size)
    }

    @Test
    fun `toggleSector filters stocks by selected sector`() = runTest {
        // Given
        coEvery { stockRepository.getStocks() } returns flowOf(testStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        viewModel = DiscoveryViewModel(stockRepository, filingRepository, observabilityService, api)
        advanceUntilIdle()

        // When - select Technology sector
        viewModel.toggleSector("Technology")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.selectedSectors.value.contains("Technology"))
        assertEquals(1, viewModel.stocks.value.size)
        assertEquals("AAPL", viewModel.stocks.value.first().ticker)
    }

    @Test
    fun `clearSectors shows all stocks`() = runTest {
        // Given
        coEvery { stockRepository.getStocks() } returns flowOf(testStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        viewModel = DiscoveryViewModel(stockRepository, filingRepository, observabilityService, api)
        advanceUntilIdle()

        // Filter by sector first
        viewModel.toggleSector("Technology")
        advanceUntilIdle()
        assertEquals(1, viewModel.stocks.value.size)

        // When - clear sectors
        viewModel.clearSectors()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.selectedSectors.value.isEmpty())
        assertEquals(testStocks.size, viewModel.stocks.value.size)
    }
}
