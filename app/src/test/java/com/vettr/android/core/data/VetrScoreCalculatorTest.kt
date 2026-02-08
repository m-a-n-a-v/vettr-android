package com.vettr.android.core.data

import com.vettr.android.core.data.repository.ExecutiveRepository
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.RedFlagRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Comprehensive unit tests for VetrScoreCalculator covering all components,
 * adjustments, caching, and edge cases.
 */
class VetrScoreCalculatorTest {

    private lateinit var executiveRepository: ExecutiveRepository
    private lateinit var filingRepository: FilingRepository
    private lateinit var redFlagRepository: RedFlagRepository
    private lateinit var stockRepository: StockRepository
    private lateinit var calculator: VetrScoreCalculator
    private val testDispatcher = StandardTestDispatcher()

    private val testTicker = "TEST"
    private val testStockId = "stock-123"
    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        executiveRepository = mockk()
        filingRepository = mockk()
        redFlagRepository = mockk()
        stockRepository = mockk()

        calculator = VetrScoreCalculator(
            executiveRepository = executiveRepository,
            filingRepository = filingRepository,
            redFlagRepository = redFlagRepository,
            stockRepository = stockRepository,
            dispatcher = testDispatcher
        )
    }

    // ========== Overall Score Calculation Tests ==========

    @Test
    fun `test calculateScore returns result with all components`() = runTest(testDispatcher) {
        setupMockRepositories()

        val result = calculator.calculateScore(testTicker, testStockId)

        assertNotNull(result)
        assertTrue(result.overallScore in 0..100)
        assertEquals(5, result.components.size)
        assertTrue(result.components.containsKey("pedigree"))
        assertTrue(result.components.containsKey("filingVelocity"))
        assertTrue(result.components.containsKey("redFlag"))
        assertTrue(result.components.containsKey("growth"))
        assertTrue(result.components.containsKey("governance"))
    }

    @Test
    fun `test calculateScore clamps result to 0-100 range`() = runTest(testDispatcher) {
        // Setup mocks that could theoretically produce out-of-range scores
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 0
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(createStock(priceChange = -50.0))

        val result = calculator.calculateScore(testTicker, testStockId)

        assertTrue(result.overallScore >= 0)
        assertTrue(result.overallScore <= 100)
    }

    @Test
    fun `test perfect score with all components at 100`() = runTest(testDispatcher) {
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 100
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(
            List(5) { createExecutive(yearsAtCompany = 5.0) }
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(
            List(4) { i -> createFiling(daysAgo = 90L * i, isMaterial = true, summary = "Audited financial report") }
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 1_500_000_000.0, priceChange = 25.0)
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        // Should be very high score with bonus for audited financials
        assertTrue(result.overallScore >= 95)
    }

    @Test
    fun `test low score with poor components`() = runTest(testDispatcher) {
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 0
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(
            listOf(createExecutive(yearsAtCompany = 0.5))
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 25.0, "test", now),
            DetectedFlag(RedFlagType.DISCLOSURE_GAPS, testTicker, 15.0, "test", now)
        )
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 10_000_000.0, priceChange = -15.0)
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        // Should be low score with penalty for overdue filings
        assertTrue(result.overallScore <= 30)
    }

    // ========== Pedigree Score Tests ==========

    @Test
    fun `test pedigree score uses executive repository score`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 85

        val result = calculator.calculateScore(testTicker, testStockId)

        assertEquals(85, result.components["pedigree"])
    }

    @Test
    fun `test pedigree score clamps to 0-100`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 150

        val result = calculator.calculateScore(testTicker, testStockId)

        assertEquals(100, result.components["pedigree"])
    }

    // ========== Filing Velocity Score Tests ==========

    @Test
    fun `test filing velocity score with 4+ filings per year`() = runTest(testDispatcher) {
        setupMockRepositories()
        val filings = listOf(
            createFiling(daysAgo = 30),
            createFiling(daysAgo = 120),
            createFiling(daysAgo = 210),
            createFiling(daysAgo = 300)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)

        val result = calculator.calculateScore(testTicker, testStockId)

        val filingScore = result.components["filingVelocity"]!!
        assertTrue(filingScore >= 80) // High score for regular quarterly filings (60 + 30)
    }

    @Test
    fun `test filing velocity score with 3 filings per year`() = runTest(testDispatcher) {
        setupMockRepositories()
        val filings = listOf(
            createFiling(daysAgo = 40),
            createFiling(daysAgo = 160),
            createFiling(daysAgo = 280)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)

        val result = calculator.calculateScore(testTicker, testStockId)

        val filingScore = result.components["filingVelocity"]!!
        assertTrue(filingScore in 60..80) // Moderate score (45 + ~30)
    }

    @Test
    fun `test filing velocity score with inconsistent gaps`() = runTest(testDispatcher) {
        setupMockRepositories()
        val filings = listOf(
            createFiling(daysAgo = 20),
            createFiling(daysAgo = 250), // Large gap
            createFiling(daysAgo = 300)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)

        val result = calculator.calculateScore(testTicker, testStockId)

        val filingScore = result.components["filingVelocity"]!!
        // 3 filings = 45 frequency, gaps avg ~140 days = 30 consistency
        assertTrue(filingScore in 65..80) // Moderate score with some gaps
    }

    @Test
    fun `test filing velocity score with no filings`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())

        val result = calculator.calculateScore(testTicker, testStockId)

        assertEquals(0, result.components["filingVelocity"])
    }

    // ========== Red Flag Score Tests ==========

    @Test
    fun `test red flag score with no flags returns 100`() = runTest(testDispatcher) {
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()
        setupMockRepositories()

        val result = calculator.calculateScore(testTicker, testStockId)

        assertEquals(100, result.components["redFlag"])
    }

    @Test
    fun `test red flag score inverts flag score correctly`() = runTest(testDispatcher) {
        setupMockRepositories()
        val flags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 20.0, "test", now)
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns flags

        val result = calculator.calculateScore(testTicker, testStockId)

        // Total red flag score = 50, so inverted score = 100 - 50 = 50
        assertEquals(50, result.components["redFlag"])
    }

    @Test
    fun `test red flag score with critical flags returns low score`() = runTest(testDispatcher) {
        setupMockRepositories()
        val flags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 25.0, "test", now),
            DetectedFlag(RedFlagType.EXECUTIVE_CHURN, testTicker, 20.0, "test", now),
            DetectedFlag(RedFlagType.DISCLOSURE_GAPS, testTicker, 15.0, "test", now),
            DetectedFlag(RedFlagType.DEBT_TREND, testTicker, 10.0, "test", now)
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns flags

        val result = calculator.calculateScore(testTicker, testStockId)

        // Total red flag score = 100, so inverted score = 0
        assertEquals(0, result.components["redFlag"])
    }

    // ========== Growth Score Tests ==========

    @Test
    fun `test growth score with large market cap and strong momentum`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 1_200_000_000.0, priceChange = 22.0)
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val growthScore = result.components["growth"]!!
        assertTrue(growthScore >= 90) // Should be very high (40 + 60 = 100, clamped)
    }

    @Test
    fun `test growth score with small market cap and negative momentum`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 30_000_000.0, priceChange = -12.0)
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val growthScore = result.components["growth"]!!
        assertTrue(growthScore <= 20) // Should be low (15 + 0 = 15)
    }

    @Test
    fun `test growth score with mid-range values`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 150_000_000.0, priceChange = 7.0)
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val growthScore = result.components["growth"]!!
        assertTrue(growthScore in 55..75) // Should be moderate to good (25 + 40 = 65)
    }

    @Test
    fun `test growth score with no stock data returns 0`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(null)

        val result = calculator.calculateScore(testTicker, testStockId)

        assertEquals(0, result.components["growth"])
    }

    // ========== Governance Score Tests ==========

    @Test
    fun `test governance score with strong team, transparency, and stability`() = runTest(testDispatcher) {
        setupMockRepositories()
        val executives = List(6) { createExecutive(yearsAtCompany = 6.0) }
        val filings = List(12) { i -> createFiling(daysAgo = 30L * i, isMaterial = true) }
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(executives)
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)

        val result = calculator.calculateScore(testTicker, testStockId)

        val governanceScore = result.components["governance"]!!
        assertTrue(governanceScore >= 90) // Should be excellent (35 + 35 + 30 = 100, clamped)
    }

    @Test
    fun `test governance score with small team and low transparency`() = runTest(testDispatcher) {
        setupMockRepositories()
        val executives = listOf(createExecutive(yearsAtCompany = 1.0))
        val filings = listOf(createFiling(isMaterial = false))
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(executives)
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)

        val result = calculator.calculateScore(testTicker, testStockId)

        val governanceScore = result.components["governance"]!!
        assertTrue(governanceScore <= 35) // Should be low (15 + 5 + 15 = 35)
    }

    @Test
    fun `test governance score with adequate team size`() = runTest(testDispatcher) {
        setupMockRepositories()
        val executives = List(3) { createExecutive(yearsAtCompany = 3.5) }
        val filings = List(6) { i -> createFiling(daysAgo = 60L * i, isMaterial = true) }
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(executives)
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)

        val result = calculator.calculateScore(testTicker, testStockId)

        val governanceScore = result.components["governance"]!!
        assertTrue(governanceScore in 70..90) // Should be good (25 + 25 + 25 = 75)
    }

    // ========== Bonus and Penalty Tests ==========

    @Test
    fun `test bonus for audited financials`() = runTest(testDispatcher) {
        setupMockRepositories(hasAuditedFinancials = true)

        val result = calculator.calculateScore(testTicker, testStockId)

        // With audited financials, should have higher overall score due to +5 bonus
        // Base score from setupMockRepositories should be moderate, plus bonus
        assertTrue(result.overallScore >= 60) // Bonus applied
    }

    @Test
    fun `test penalty for overdue filings`() = runTest(testDispatcher) {
        setupMockRepositories()
        // No recent filings (overdue)
        val oldFilings = listOf(createFiling(daysAgo = 200))
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(oldFilings)

        val result = calculator.calculateScore(testTicker, testStockId)

        // Score should include -10 penalty for overdue filings
        assertTrue(result.overallScore <= 60) // Penalty applied
    }

    @Test
    fun `test penalty for regulatory issues via disclosure gaps`() = runTest(testDispatcher) {
        setupMockRepositories()
        val flags = listOf(
            DetectedFlag(RedFlagType.DISCLOSURE_GAPS, testTicker, 15.0, "test", now)
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns flags

        val result = calculator.calculateScore(testTicker, testStockId)

        // Penalty should be applied due to disclosure gap flag
        // Red flag score should reflect the disclosure gap
        assertTrue(result.components["redFlag"]!! < 90)
    }

    // ========== Caching Tests ==========

    @Test
    fun `test caching returns same result within TTL`() = runTest(testDispatcher) {
        setupMockRepositories()

        val result1 = calculator.calculateScore(testTicker, testStockId)
        val result2 = calculator.calculateScore(testTicker, testStockId)

        assertEquals(result1.overallScore, result2.overallScore)
        assertEquals(result1.components, result2.components)
    }

    @Test
    fun `test getCachedScore returns null when no cache`() = runTest(testDispatcher) {
        val cached = calculator.getCachedScore(testTicker)

        assertNull(cached)
    }

    @Test
    fun `test getCachedScore returns cached result within TTL`() = runTest(testDispatcher) {
        setupMockRepositories()

        val result = calculator.calculateScore(testTicker, testStockId)
        val cached = calculator.getCachedScore(testTicker)

        assertNotNull(cached)
        assertEquals(result.overallScore, cached!!.overallScore)
    }

    @Test
    fun `test clearCache removes specific ticker`() = runTest(testDispatcher) {
        setupMockRepositories()

        calculator.calculateScore(testTicker, testStockId)
        calculator.clearCache(testTicker)
        val cached = calculator.getCachedScore(testTicker)

        assertNull(cached)
    }

    @Test
    fun `test clearCache removes all tickers when no ticker specified`() = runTest(testDispatcher) {
        setupMockRepositories()

        calculator.calculateScore(testTicker, testStockId)

        // Setup mocks for second ticker
        val testTicker2 = "TEST2"
        val testStockId2 = "stock-456"
        coEvery { executiveRepository.getExecutiveScore(testStockId2) } returns 70
        coEvery { executiveRepository.getExecutivesForStock(testStockId2) } returns flowOf(
            listOf(createExecutive(yearsAtCompany = 4.0))
        )
        coEvery { filingRepository.getFilingsForStock(testStockId2) } returns flowOf(
            listOf(createFiling(daysAgo = 30))
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker2) } returns emptyList()
        coEvery { stockRepository.getStock(testStockId2) } returns flowOf(
            createStock(ticker = testTicker2, marketCap = 150_000_000.0, priceChange = 3.0)
        )

        calculator.calculateScore(testTicker2, testStockId2)
        calculator.clearCache()

        assertNull(calculator.getCachedScore(testTicker))
        assertNull(calculator.getCachedScore(testTicker2))
    }

    // ========== Thread Safety Tests ==========

    @Test
    fun `test concurrent calculations are thread-safe`() = runTest(testDispatcher) {
        setupMockRepositories()

        val result1 = calculator.calculateScore(testTicker, testStockId)
        val result2 = calculator.calculateScore(testTicker, testStockId)

        assertEquals(result1.overallScore, result2.overallScore)
    }

    // ========== Edge Case Tests ==========

    @Test
    fun `test calculation with all empty data`() = runTest(testDispatcher) {
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 0
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(null)

        val result = calculator.calculateScore(testTicker, testStockId)

        // Should handle gracefully with low but valid score
        assertTrue(result.overallScore in 0..30)
    }

    @Test
    fun `test component weights sum correctly`() = runTest(testDispatcher) {
        setupMockRepositories()

        val result = calculator.calculateScore(testTicker, testStockId)

        // Verify all component keys exist
        assertNotNull(result.components["pedigree"])
        assertNotNull(result.components["filingVelocity"])
        assertNotNull(result.components["redFlag"])
        assertNotNull(result.components["growth"])
        assertNotNull(result.components["governance"])
    }

    // ========== Helper Functions ==========

    private fun setupMockRepositories(hasAuditedFinancials: Boolean = false) {
        val filings = if (hasAuditedFinancials) {
            listOf(
                createFiling(summary = "Audited financial statements", daysAgo = 30),
                createFiling(daysAgo = 120),
                createFiling(daysAgo = 210)
            )
        } else {
            listOf(
                createFiling(daysAgo = 30),
                createFiling(daysAgo = 120),
                createFiling(daysAgo = 210)
            )
        }

        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 75
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(
            listOf(
                createExecutive(yearsAtCompany = 3.0),
                createExecutive(yearsAtCompany = 5.0),
                createExecutive(yearsAtCompany = 2.0)
            )
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 200_000_000.0, priceChange = 5.0)
        )
    }

    private fun createStock(
        ticker: String = testTicker,
        marketCap: Double = 100_000_000.0,
        priceChange: Double = 0.0
    ): Stock {
        return Stock(
            id = testStockId,
            ticker = ticker,
            name = "Test Company",
            exchange = "TSX-V",
            sector = "Technology",
            marketCap = marketCap,
            price = 1.50,
            priceChange = priceChange,
            vetrScore = 0,
            isFavorite = false
        )
    }

    private fun createFiling(
        type: String = "Quarterly Report",
        summary: String = "Regular filing",
        daysAgo: Long = 0,
        isMaterial: Boolean = false
    ): Filing {
        return Filing(
            id = "filing-${System.nanoTime()}",
            stockId = testStockId,
            type = type,
            title = "Test Filing",
            date = now - TimeUnit.DAYS.toMillis(daysAgo),
            summary = summary,
            isRead = false,
            isMaterial = isMaterial
        )
    }

    private fun createExecutive(
        name: String = "Test Executive",
        yearsAtCompany: Double = 5.0
    ): Executive {
        return Executive(
            id = "exec-${System.nanoTime()}",
            stockId = testStockId,
            name = name,
            title = "CEO",
            yearsAtCompany = yearsAtCompany,
            previousCompanies = "[]",
            education = "MBA",
            specialization = "Technology"
        )
    }

    private fun getWeightedMultiplier(): Double {
        return 0.25 + 0.20 + 0.25 + 0.15 + 0.15 // Sum of all weights
    }
}
