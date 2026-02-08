package com.vettr.android.core.data

import android.content.Context
import com.vettr.android.core.data.local.ExecutiveDao
import com.vettr.android.core.data.local.FilingDao
import com.vettr.android.core.data.local.SeedDataService
import com.vettr.android.core.data.local.StockDao
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SeedDataService to verify seed data integrity
 */
class SeedDataServiceTest {

    private lateinit var stockDao: StockDao
    private lateinit var filingDao: FilingDao
    private lateinit var executiveDao: ExecutiveDao
    private lateinit var context: Context
    private lateinit var seedDataService: SeedDataService

    @Before
    fun setup() {
        stockDao = mockk(relaxed = true)
        filingDao = mockk(relaxed = true)
        executiveDao = mockk(relaxed = true)
        context = mockk(relaxed = true)
        seedDataService = SeedDataService(context, stockDao, filingDao, executiveDao)
    }

    @Test
    fun `25 stocks are generated with valid fields`() = runBlocking {
        // Given
        val stocksSlot = slot<List<Stock>>()
        coEvery { stockDao.insertAll(capture(stocksSlot)) } returns Unit

        // When
        seedDataService.seedAllData()

        // Then
        coVerify { stockDao.insertAll(any()) }
        val stocks = stocksSlot.captured

        // Verify exactly 25 stocks
        assertEquals("Should generate exactly 25 stocks", 25, stocks.size)

        // Verify each stock has valid fields
        stocks.forEach { stock ->
            assertNotNull("Stock ID should not be null", stock.id)
            assertFalse("Stock ID should not be empty", stock.id.isEmpty())

            assertNotNull("Ticker should not be null", stock.ticker)
            assertFalse("Ticker should not be empty", stock.ticker.isEmpty())

            assertNotNull("Name should not be null", stock.name)
            assertFalse("Name should not be empty", stock.name.isEmpty())

            assertNotNull("Exchange should not be null", stock.exchange)
            assertTrue(
                "Exchange should be TSX, TSXV, or CSE",
                stock.exchange in listOf("TSX", "TSXV", "CSE")
            )

            assertNotNull("Sector should not be null", stock.sector)
            assertFalse("Sector should not be empty", stock.sector.isEmpty())

            assertTrue("Market cap should be positive", stock.marketCap > 0)
            assertTrue("Price should be positive", stock.price > 0)
        }
    }

    @Test
    fun `VETR Scores are in valid range 0-100`() = runBlocking {
        // Given
        val stocksSlot = slot<List<Stock>>()
        coEvery { stockDao.insertAll(capture(stocksSlot)) } returns Unit

        // When
        seedDataService.seedAllData()

        // Then
        val stocks = stocksSlot.captured

        // Verify VETR Score is in valid range for each stock
        stocks.forEach { stock ->
            assertTrue(
                "VETR Score ${stock.vetrScore} for ${stock.ticker} should be >= 0",
                stock.vetrScore >= 0
            )
            assertTrue(
                "VETR Score ${stock.vetrScore} for ${stock.ticker} should be <= 100",
                stock.vetrScore <= 100
            )
        }

        // Verify we have a distribution of scores
        val scores = stocks.map { it.vetrScore }
        val minScore = scores.minOrNull()
        val maxScore = scores.maxOrNull()

        assertNotNull("Should have minimum score", minScore)
        assertNotNull("Should have maximum score", maxScore)
        assertTrue(
            "Should have score distribution (max > min)",
            maxScore!! > minScore!!
        )
    }

    @Test
    fun `Filings have required fields - title, date, summary`() = runBlocking {
        // Given
        val filingsSlot = slot<List<Filing>>()
        coEvery { filingDao.insertAll(capture(filingsSlot)) } returns Unit

        // When
        seedDataService.seedAllData()

        // Then
        coVerify { filingDao.insertAll(any()) }
        val filings = filingsSlot.captured

        // Verify filings were generated (should be 3-5 per stock = 75-125 total)
        assertTrue("Should generate filings", filings.isNotEmpty())
        assertTrue("Should generate at least 75 filings (3 per stock)", filings.size >= 75)
        assertTrue("Should generate at most 125 filings (5 per stock)", filings.size <= 125)

        // Verify each filing has required fields
        filings.forEach { filing ->
            // Verify title
            assertNotNull("Filing title should not be null", filing.title)
            assertFalse("Filing title should not be empty", filing.title.isEmpty())

            // Verify date
            assertNotNull("Filing date should not be null", filing.date)
            assertTrue("Filing date should be positive (Unix timestamp)", filing.date > 0)
            assertTrue(
                "Filing date should be in the past",
                filing.date <= System.currentTimeMillis()
            )

            // Verify summary
            assertNotNull("Filing summary should not be null", filing.summary)
            assertFalse("Filing summary should not be empty", filing.summary.isEmpty())

            // Verify type
            assertNotNull("Filing type should not be null", filing.type)
            assertFalse("Filing type should not be empty", filing.type.isEmpty())

            // Verify stockId
            assertNotNull("Filing stockId should not be null", filing.stockId)
            assertFalse("Filing stockId should not be empty", filing.stockId.isEmpty())
        }
    }

    @Test
    fun `All stocks have unique tickers`() = runBlocking {
        // Given
        val stocksSlot = slot<List<Stock>>()
        coEvery { stockDao.insertAll(capture(stocksSlot)) } returns Unit

        // When
        seedDataService.seedAllData()

        // Then
        val stocks = stocksSlot.captured
        val tickers = stocks.map { it.ticker }
        val uniqueTickers = tickers.toSet()

        assertEquals(
            "All tickers should be unique",
            tickers.size,
            uniqueTickers.size
        )
    }

    @Test
    fun `All stocks have unique IDs`() = runBlocking {
        // Given
        val stocksSlot = slot<List<Stock>>()
        coEvery { stockDao.insertAll(capture(stocksSlot)) } returns Unit

        // When
        seedDataService.seedAllData()

        // Then
        val stocks = stocksSlot.captured
        val ids = stocks.map { it.id }
        val uniqueIds = ids.toSet()

        assertEquals(
            "All stock IDs should be unique",
            ids.size,
            uniqueIds.size
        )
    }

    @Test
    fun `Executives are generated for each stock`() = runBlocking {
        // Given
        val executivesSlot = slot<List<Executive>>()
        coEvery { executiveDao.insertAll(capture(executivesSlot)) } returns Unit

        // When
        seedDataService.seedAllData()

        // Then
        coVerify { executiveDao.insertAll(any()) }
        val executives = executivesSlot.captured

        // Verify executives were generated (should be 2-4 per stock = 50-100 total)
        assertTrue("Should generate executives", executives.isNotEmpty())
        assertTrue("Should generate at least 50 executives (2 per stock)", executives.size >= 50)
        assertTrue("Should generate at most 100 executives (4 per stock)", executives.size <= 100)

        // Verify each executive has valid fields
        executives.forEach { executive ->
            assertNotNull("Executive name should not be null", executive.name)
            assertFalse("Executive name should not be empty", executive.name.isEmpty())

            assertNotNull("Executive title should not be null", executive.title)
            assertFalse("Executive title should not be empty", executive.title.isEmpty())

            assertTrue("Years at company should be positive", executive.yearsAtCompany > 0)

            assertNotNull("Education should not be null", executive.education)
            assertNotNull("Specialization should not be null", executive.specialization)
        }
    }
}
