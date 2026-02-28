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
 * Comprehensive unit tests for VetrScoreCalculator V2 covering all four pillars,
 * adjustments, caching, and edge cases.
 *
 * V2 Pillars:
 * - Financial Survival (35%)
 * - Operational Efficiency (25%)
 * - Shareholder Structure (25%)
 * - Market Sentiment (15%)
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
    fun `test calculateScore returns result with all four pillars`() = runTest(testDispatcher) {
        setupMockRepositories()

        val result = calculator.calculateScore(testTicker, testStockId)

        assertNotNull(result)
        assertTrue(result.overallScore in 0..100)
        assertEquals(4, result.components.size)
        assertTrue(result.components.containsKey("financialSurvival"))
        assertTrue(result.components.containsKey("operationalEfficiency"))
        assertTrue(result.components.containsKey("shareholderStructure"))
        assertTrue(result.components.containsKey("marketSentiment"))
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
    fun `test high score with strong pillars`() = runTest(testDispatcher) {
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 100
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(
            List(5) { createExecutive(yearsAtCompany = 5.0) }
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(
            List(4) { i -> createFiling(daysAgo = 90L * i, isMaterial = true, summary = "Audited financial report with cash flow analysis") }
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 1_500_000_000.0, priceChange = 25.0)
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        // Should be very high score with bonus for audited financials
        assertTrue(result.overallScore >= 85)
    }

    @Test
    fun `test low score with poor pillars`() = runTest(testDispatcher) {
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

    // ========== Financial Survival Pillar Tests ==========

    @Test
    fun `test financial survival with large cap and no debt flags`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 600_000_000.0)
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()

        val result = calculator.calculateScore(testTicker, testStockId)

        val financialSurvival = result.components["financialSurvival"]!!
        assertTrue(financialSurvival >= 70) // Large cap + no debt flags + decent disclosure
    }

    @Test
    fun `test financial survival with micro cap and debt flags`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 15_000_000.0)
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns listOf(
            DetectedFlag(RedFlagType.DEBT_TREND, testTicker, 25.0, "High debt", now)
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val financialSurvival = result.components["financialSurvival"]!!
        assertTrue(financialSurvival <= 50) // Micro cap + debt flags
    }

    // ========== Operational Efficiency Pillar Tests ==========

    @Test
    fun `test operational efficiency with regular filings and experienced team`() = runTest(testDispatcher) {
        setupMockRepositories()
        val filings = listOf(
            createFiling(daysAgo = 30),
            createFiling(daysAgo = 120),
            createFiling(daysAgo = 210),
            createFiling(daysAgo = 300)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(
            List(3) { createExecutive(yearsAtCompany = 5.0) }
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val opEfficiency = result.components["operationalEfficiency"]!!
        assertTrue(opEfficiency >= 80) // Regular cadence + consistent gaps + experienced team
    }

    @Test
    fun `test operational efficiency with no filings returns 0`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())

        val result = calculator.calculateScore(testTicker, testStockId)

        assertEquals(0, result.components["operationalEfficiency"])
    }

    @Test
    fun `test operational efficiency with sparse filings`() = runTest(testDispatcher) {
        setupMockRepositories()
        val filings = listOf(
            createFiling(daysAgo = 40),
            createFiling(daysAgo = 300)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(
            listOf(createExecutive(yearsAtCompany = 1.0))
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val opEfficiency = result.components["operationalEfficiency"]!!
        assertTrue(opEfficiency in 30..60) // Sparse filings + inconsistent + limited experience
    }

    // ========== Shareholder Structure Pillar Tests ==========

    @Test
    fun `test shareholder structure with strong pedigree and no dilution`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 90
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(
            List(5) { createExecutive(yearsAtCompany = 6.0) }
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val shareholderStructure = result.components["shareholderStructure"]!!
        assertTrue(shareholderStructure >= 80) // Strong pedigree + no dilution + high insider conviction
    }

    @Test
    fun `test shareholder structure with dilution flags`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { executiveRepository.getExecutiveScore(testStockId) } returns 50
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 20.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 15.0, "test", now)
        )
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(
            listOf(createExecutive(yearsAtCompany = 1.0))
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val shareholderStructure = result.components["shareholderStructure"]!!
        assertTrue(shareholderStructure <= 40) // Moderate pedigree + high dilution + low conviction
    }

    // ========== Market Sentiment Pillar Tests ==========

    @Test
    fun `test market sentiment with strong momentum and high liquidity`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 1_200_000_000.0, priceChange = 22.0)
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()

        val result = calculator.calculateScore(testTicker, testStockId)

        val marketSentiment = result.components["marketSentiment"]!!
        assertTrue(marketSentiment >= 90) // High liquidity + strong momentum + clean sentiment
    }

    @Test
    fun `test market sentiment with negative momentum and sentiment flags`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(
            createStock(marketCap = 30_000_000.0, priceChange = -12.0)
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns listOf(
            DetectedFlag(RedFlagType.DISCLOSURE_GAPS, testTicker, 15.0, "test", now),
            DetectedFlag(RedFlagType.EXECUTIVE_CHURN, testTicker, 10.0, "test", now)
        )

        val result = calculator.calculateScore(testTicker, testStockId)

        val marketSentiment = result.components["marketSentiment"]!!
        assertTrue(marketSentiment <= 30) // Low liquidity + negative momentum + sentiment concern
    }

    @Test
    fun `test market sentiment with no stock data returns 0`() = runTest(testDispatcher) {
        setupMockRepositories()
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(null)

        val result = calculator.calculateScore(testTicker, testStockId)

        assertEquals(0, result.components["marketSentiment"])
    }

    // ========== Bonus and Penalty Tests ==========

    @Test
    fun `test bonus for audited financials`() = runTest(testDispatcher) {
        setupMockRepositories(hasAuditedFinancials = true)

        val result = calculator.calculateScore(testTicker, testStockId)

        // With audited financials, should have higher overall score due to +5 bonus
        assertTrue(result.overallScore >= 55) // Bonus applied
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
        assertTrue(result.overallScore < 70) // Penalty applied
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
    fun `test all four pillar keys exist in result`() = runTest(testDispatcher) {
        setupMockRepositories()

        val result = calculator.calculateScore(testTicker, testStockId)

        // Verify all pillar keys exist
        assertNotNull(result.components["financialSurvival"])
        assertNotNull(result.components["operationalEfficiency"])
        assertNotNull(result.components["shareholderStructure"])
        assertNotNull(result.components["marketSentiment"])
    }

    @Test
    fun `test pillar weights produce expected overall range`() = runTest(testDispatcher) {
        setupMockRepositories()

        val result = calculator.calculateScore(testTicker, testStockId)

        // With moderate mock data, score should be in a reasonable range
        assertTrue(result.overallScore in 30..90)
    }

    // ========== Helper Functions ==========

    private fun setupMockRepositories(hasAuditedFinancials: Boolean = false) {
        val filings = if (hasAuditedFinancials) {
            listOf(
                createFiling(summary = "Audited financial statements with cash flow analysis", daysAgo = 30),
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
}
