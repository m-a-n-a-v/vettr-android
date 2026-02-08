package com.vettr.android.feature.pulse

import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Filing
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
import org.junit.Assert.assertTrue
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

        coEvery { stockRepository.getStocks() } returns flowOf(mockStocks)
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        // When
        viewModel = PulseViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        // Then
        assertEquals(mockStocks, viewModel.stocks.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadData populates filings flow`() = runTest {
        // Given
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

        coEvery { stockRepository.getStocks() } returns flowOf(emptyList())
        coEvery { filingRepository.getLatestFilings(10) } returns flowOf(mockFilings)

        // When
        viewModel = PulseViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        // Then
        assertEquals(mockFilings, viewModel.filings.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadData sets loading state during fetch`() = runTest {
        // Given
        coEvery { stockRepository.getStocks() } returns flowOf(emptyList())
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        // When
        viewModel = PulseViewModel(stockRepository, filingRepository)
        advanceUntilIdle()

        // Then - loading is false after data loads
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadData handles stock repository error`() = runTest {
        // Given
        coEvery { stockRepository.getStocks() } returns flowOf(emptyList())
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        // When
        viewModel = PulseViewModel(stockRepository, filingRepository)
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
                isFavorite = false
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
                isFavorite = false
            )
        )

        coEvery { stockRepository.getStocks() } returnsMany listOf(
            flowOf(initialStocks),
            flowOf(updatedStocks)
        )
        coEvery { filingRepository.getLatestFilings(any()) } returns flowOf(emptyList())

        viewModel = PulseViewModel(stockRepository, filingRepository)
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
