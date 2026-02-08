package com.vettr.android.feature.stockdetail

import androidx.lifecycle.SavedStateHandle
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for StockDetailViewModel.
 * Tests stock detail loading and favorite toggle functionality.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StockDetailViewModelTest {

    private lateinit var viewModel: StockDetailViewModel
    private lateinit var stockRepository: StockRepository
    private lateinit var filingRepository: FilingRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val testDispatcher = StandardTestDispatcher()
    private val testStockId = "stock-1"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        stockRepository = mockk()
        filingRepository = mockk()
        savedStateHandle = SavedStateHandle(mapOf("stockId" to testStockId))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleFavorite updates stock isFavorite status`() = runTest {
        // Given
        val mockStock = Stock(
            id = testStockId,
            ticker = "TSLA",
            name = "Tesla Inc",
            exchange = "NASDAQ",
            sector = "Automotive",
            marketCap = 800000000000.0,
            price = 250.50,
            priceChange = 2.5,
            vetrScore = 75,
            isFavorite = false
        )

        val updatedStock = mockStock.copy(isFavorite = true)

        // Mock initial stock load
        coEvery { stockRepository.getStock(testStockId) } returnsMany listOf(
            flowOf(mockStock),
            flowOf(updatedStock)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { stockRepository.toggleFavorite(testStockId) } returns Unit

        viewModel = StockDetailViewModel(stockRepository, filingRepository, savedStateHandle)
        advanceUntilIdle()

        // Verify initial state
        assertEquals(mockStock, viewModel.stock.value)
        assertFalse(viewModel.stock.value?.isFavorite ?: true)

        // When
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Then
        coVerify { stockRepository.toggleFavorite(testStockId) }
    }

    @Test
    fun `loadStock populates stock and filings`() = runTest {
        // Given
        val mockStock = Stock(
            id = testStockId,
            ticker = "TSLA",
            name = "Tesla Inc",
            exchange = "NASDAQ",
            sector = "Automotive",
            marketCap = 800000000000.0,
            price = 250.50,
            priceChange = 2.5,
            vetrScore = 75,
            isFavorite = false
        )

        val mockFilings = listOf(
            Filing(
                id = "filing-1",
                stockId = testStockId,
                type = "10-K",
                title = "Annual Report",
                date = System.currentTimeMillis(),
                summary = "Annual financial report for Tesla",
                isRead = false
            ),
            Filing(
                id = "filing-2",
                stockId = testStockId,
                type = "8-K",
                title = "Current Report",
                date = System.currentTimeMillis(),
                summary = "Current report for Tesla",
                isRead = true
            )
        )

        coEvery { stockRepository.getStock(testStockId) } returns flowOf(mockStock)
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(mockFilings)

        // When
        viewModel = StockDetailViewModel(stockRepository, filingRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertEquals(mockStock, viewModel.stock.value)
        assertEquals(mockFilings, viewModel.filings.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `selectTab updates selected tab state`() = runTest {
        // Given
        val mockStock = Stock(
            id = testStockId,
            ticker = "TSLA",
            name = "Tesla Inc",
            exchange = "NASDAQ",
            sector = "Automotive",
            marketCap = 800000000000.0,
            price = 250.50,
            priceChange = 2.5,
            vetrScore = 75,
            isFavorite = false
        )

        coEvery { stockRepository.getStock(testStockId) } returns flowOf(mockStock)
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())

        viewModel = StockDetailViewModel(stockRepository, filingRepository, savedStateHandle)
        advanceUntilIdle()

        // Verify initial tab is OVERVIEW
        assertEquals(StockDetailTab.OVERVIEW, viewModel.selectedTab.value)

        // When
        viewModel.selectTab(StockDetailTab.ANALYSIS)
        advanceUntilIdle()

        // Then
        assertEquals(StockDetailTab.ANALYSIS, viewModel.selectedTab.value)

        // When
        viewModel.selectTab(StockDetailTab.NEWS)
        advanceUntilIdle()

        // Then
        assertEquals(StockDetailTab.NEWS, viewModel.selectedTab.value)
    }

    @Test
    fun `selectTimeRange updates selected time range state`() = runTest {
        // Given
        val mockStock = Stock(
            id = testStockId,
            ticker = "TSLA",
            name = "Tesla Inc",
            exchange = "NASDAQ",
            sector = "Automotive",
            marketCap = 800000000000.0,
            price = 250.50,
            priceChange = 2.5,
            vetrScore = 75,
            isFavorite = false
        )

        coEvery { stockRepository.getStock(testStockId) } returns flowOf(mockStock)
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())

        viewModel = StockDetailViewModel(stockRepository, filingRepository, savedStateHandle)
        advanceUntilIdle()

        // Verify initial time range is ONE_DAY
        assertEquals(TimeRange.ONE_DAY, viewModel.selectedTimeRange.value)

        // When
        viewModel.selectTimeRange(TimeRange.ONE_WEEK)
        advanceUntilIdle()

        // Then
        assertEquals(TimeRange.ONE_WEEK, viewModel.selectedTimeRange.value)

        // When
        viewModel.selectTimeRange(TimeRange.ONE_YEAR)
        advanceUntilIdle()

        // Then
        assertEquals(TimeRange.ONE_YEAR, viewModel.selectedTimeRange.value)
    }

    @Test
    fun `toggleFavorite handles errors gracefully`() = runTest {
        // Given
        val mockStock = Stock(
            id = testStockId,
            ticker = "TSLA",
            name = "Tesla Inc",
            exchange = "NASDAQ",
            sector = "Automotive",
            marketCap = 800000000000.0,
            price = 250.50,
            priceChange = 2.5,
            vetrScore = 75,
            isFavorite = false
        )

        coEvery { stockRepository.getStock(testStockId) } returns flowOf(mockStock)
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { stockRepository.toggleFavorite(testStockId) } throws Exception("Network error")

        viewModel = StockDetailViewModel(stockRepository, filingRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Then - error message should be set
        assertNotNull(viewModel.errorMessage.value)
        assertEquals("Failed to toggle favorite: Network error", viewModel.errorMessage.value)
    }

    @Test
    fun `loadStock handles invalid stock ID`() = runTest {
        // Given - empty stock ID
        savedStateHandle = SavedStateHandle(mapOf("stockId" to ""))

        // When
        viewModel = StockDetailViewModel(stockRepository, filingRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertEquals("Invalid stock ID", viewModel.errorMessage.value)
    }
}
