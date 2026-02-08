package com.vettr.android.core.data

import com.vettr.android.core.data.repository.ExecutiveRepository
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Filing
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Unit tests for RedFlagDetector covering all severity levels and flag types.
 */
class RedFlagDetectorTest {

    private lateinit var filingRepository: FilingRepository
    private lateinit var executiveRepository: ExecutiveRepository
    private lateinit var redFlagDetector: RedFlagDetector
    private val testDispatcher = StandardTestDispatcher()

    private val testTicker = "TEST"
    private val testStockId = "stock-123"
    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        filingRepository = mockk()
        executiveRepository = mockk()
        redFlagDetector = RedFlagDetector(
            filingRepository = filingRepository,
            executiveRepository = executiveRepository,
            dispatcher = testDispatcher
        )
    }

    // ========== Consolidation Velocity Tests ==========

    @Test
    fun `test consolidation velocity - critical severity with 3 consolidations`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(type = "Share Consolidation", summary = "Share consolidation announced", daysAgo = 30),
            createFiling(type = "Consolidation", summary = "Consolidation details", daysAgo = 120),
            createFiling(type = "Consolidation Notice", summary = "Notice of share consolidation", daysAgo = 210)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(RedFlagType.CONSOLIDATION_VELOCITY, flags[0].type)
        assertEquals(30.0, flags[0].score, 0.01)
        assertTrue(flags[0].description.contains("3 share consolidation"))
    }

    @Test
    fun `test consolidation velocity - high severity with 2 consolidations`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(type = "Share Consolidation", summary = "Share consolidation details", daysAgo = 60),
            createFiling(type = "Consolidation Filing", summary = "Consolidation filing notice", daysAgo = 150)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(22.5, flags[0].score, 0.01)
    }

    @Test
    fun `test consolidation velocity - moderate severity with 1 consolidation`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(type = "Consolidation", daysAgo = 100)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(15.0, flags[0].score, 0.01)
    }

    @Test
    fun `test consolidation velocity - no flags when no consolidations`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(type = "Quarterly Report", daysAgo = 30),
            createFiling(type = "Annual Report", daysAgo = 100)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(0, flags.size)
    }

    // ========== Financing Velocity Tests ==========

    @Test
    fun `test financing velocity - critical severity with 4+ financings`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(type = "Private Placement", summary = "Private placement details", daysAgo = 30),
            createFiling(type = "Equity Financing", summary = "Equity financing announcement", daysAgo = 90),
            createFiling(type = "Offering", summary = "Public offering notice", daysAgo = 150),
            createFiling(type = "Financing", summary = "Financing completed", daysAgo = 210)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(RedFlagType.FINANCING_VELOCITY, flags[0].type)
        assertEquals(25.0, flags[0].score, 0.01)
        assertTrue(flags[0].description.contains("4 equity financing"))
    }

    @Test
    fun `test financing velocity - high severity with 3 financings`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(type = "Private Placement", summary = "Private placement announced", daysAgo = 45),
            createFiling(type = "Offering", summary = "Offering notice", daysAgo = 120),
            createFiling(type = "Equity Financing", summary = "Equity financing completed", daysAgo = 200)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(18.75, flags[0].score, 0.01)
    }

    @Test
    fun `test financing velocity - moderate severity with 2 financings`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(type = "Financing", summary = "Financing announcement", daysAgo = 60),
            createFiling(type = "Private Placement", summary = "Private placement closed", daysAgo = 140)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(12.5, flags[0].score, 0.01)
    }

    @Test
    fun `test financing velocity - low severity with 1 financing`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(type = "Offering", daysAgo = 120)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(6.25, flags[0].score, 0.01)
    }

    // ========== Executive Churn Tests ==========

    @Test
    fun `test executive churn - critical severity with 60%+ churn`() = runTest(testDispatcher) {
        val executives = listOf(
            createExecutive(yearsAtCompany = 1.5),
            createExecutive(yearsAtCompany = 1.0),
            createExecutive(yearsAtCompany = 0.5),
            createExecutive(yearsAtCompany = 5.0),
            createExecutive(yearsAtCompany = 8.0)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(executives)

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(RedFlagType.EXECUTIVE_CHURN, flags[0].type)
        assertEquals(20.0, flags[0].score, 0.01)
        assertTrue(flags[0].description.contains("3 of 5"))
    }

    @Test
    fun `test executive churn - high severity with 40-60% churn`() = runTest(testDispatcher) {
        val executives = listOf(
            createExecutive(yearsAtCompany = 1.0),
            createExecutive(yearsAtCompany = 1.5),
            createExecutive(yearsAtCompany = 3.0),
            createExecutive(yearsAtCompany = 6.0),
            createExecutive(yearsAtCompany = 10.0)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(executives)

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(15.0, flags[0].score, 0.01)
    }

    @Test
    fun `test executive churn - moderate severity with 25-40% churn`() = runTest(testDispatcher) {
        val executives = listOf(
            createExecutive(yearsAtCompany = 1.5),
            createExecutive(yearsAtCompany = 4.0),
            createExecutive(yearsAtCompany = 7.0)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(executives)

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(10.0, flags[0].score, 0.01)
    }

    @Test
    fun `test executive churn - no flags with low churn`() = runTest(testDispatcher) {
        val executives = listOf(
            createExecutive(yearsAtCompany = 5.0),
            createExecutive(yearsAtCompany = 8.0),
            createExecutive(yearsAtCompany = 12.0)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(emptyList())
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(executives)

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(0, flags.size)
    }

    // ========== Disclosure Gaps Tests ==========

    @Test
    fun `test disclosure gaps - critical severity with 8+ months gap`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(daysAgo = 10),
            createFiling(daysAgo = 260) // 250 day gap
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(RedFlagType.DISCLOSURE_GAPS, flags[0].type)
        assertEquals(15.0, flags[0].score, 0.01)
        assertTrue(flags[0].description.contains("250 days"))
    }

    @Test
    fun `test disclosure gaps - high severity with 6+ months gap`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(daysAgo = 20),
            createFiling(daysAgo = 210) // 190 day gap
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(11.25, flags[0].score, 0.01)
    }

    @Test
    fun `test disclosure gaps - moderate severity with 4+ months gap`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(daysAgo = 15),
            createFiling(daysAgo = 150) // 135 day gap
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(7.5, flags[0].score, 0.01)
    }

    @Test
    fun `test disclosure gaps - no flags with regular filings`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(daysAgo = 30),
            createFiling(daysAgo = 120),
            createFiling(daysAgo = 210)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(0, flags.size)
    }

    // ========== Debt Trend Tests ==========

    @Test
    fun `test debt trend - critical severity with 60%+ debt mentions`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(summary = "Company announced new debt facility", daysAgo = 30),
            createFiling(summary = "Loan agreement signed", daysAgo = 60),
            createFiling(summary = "Credit facility increased", daysAgo = 90),
            createFiling(summary = "Additional borrowing approved", daysAgo = 120),
            createFiling(summary = "Regular quarterly report", daysAgo = 150)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(RedFlagType.DEBT_TREND, flags[0].type)
        assertEquals(10.0, flags[0].score, 0.01)
        assertTrue(flags[0].description.contains("4 of 5"))
    }

    @Test
    fun `test debt trend - high severity with 40-60% debt mentions`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(summary = "New debt issuance", daysAgo = 40),
            createFiling(summary = "Quarterly report", daysAgo = 70),
            createFiling(summary = "Loan refinancing", daysAgo = 100),
            createFiling(summary = "Annual meeting", daysAgo = 130),
            createFiling(summary = "Executive appointment", daysAgo = 160)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(7.5, flags[0].score, 0.01)
    }

    @Test
    fun `test debt trend - moderate severity with 25-40% debt mentions`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(summary = "Credit facility update", daysAgo = 50),
            createFiling(summary = "Quarterly report", daysAgo = 80),
            createFiling(summary = "Annual report", daysAgo = 110),
            createFiling(summary = "Executive change", daysAgo = 140)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(1, flags.size)
        assertEquals(5.0, flags[0].score, 0.01)
    }

    @Test
    fun `test debt trend - no flags with minimal debt mentions`() = runTest(testDispatcher) {
        val filings = listOf(
            createFiling(summary = "Quarterly earnings report", daysAgo = 30),
            createFiling(summary = "Annual meeting results", daysAgo = 60),
            createFiling(summary = "Executive compensation", daysAgo = 90),
            createFiling(summary = "Shareholder update", daysAgo = 120),
            createFiling(summary = "Product launch announcement", daysAgo = 150)
        )
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        val flags = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(0, flags.size)
    }

    // ========== Composite Score Tests ==========

    @Test
    fun `test composite score - low severity under 30`() {
        val flags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 15.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 6.25, "test", now)
        )

        val score = redFlagDetector.calculateCompositeScore(flags)

        assertEquals(21.25, score.totalScore, 0.01)
        assertEquals(RedFlagSeverity.LOW, score.severity)
    }

    @Test
    fun `test composite score - moderate severity 30-60`() {
        val flags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 22.5, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 18.75, "test", now),
            DetectedFlag(RedFlagType.EXECUTIVE_CHURN, testTicker, 10.0, "test", now)
        )

        val score = redFlagDetector.calculateCompositeScore(flags)

        assertEquals(51.25, score.totalScore, 0.01)
        assertEquals(RedFlagSeverity.MODERATE, score.severity)
    }

    @Test
    fun `test composite score - high severity 60-85`() {
        val flags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 25.0, "test", now),
            DetectedFlag(RedFlagType.EXECUTIVE_CHURN, testTicker, 15.0, "test", now)
        )

        val score = redFlagDetector.calculateCompositeScore(flags)

        assertEquals(70.0, score.totalScore, 0.01)
        assertEquals(RedFlagSeverity.HIGH, score.severity)
    }

    @Test
    fun `test composite score - critical severity above 85`() {
        val flags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 25.0, "test", now),
            DetectedFlag(RedFlagType.EXECUTIVE_CHURN, testTicker, 20.0, "test", now),
            DetectedFlag(RedFlagType.DISCLOSURE_GAPS, testTicker, 15.0, "test", now),
            DetectedFlag(RedFlagType.DEBT_TREND, testTicker, 10.0, "test", now)
        )

        val score = redFlagDetector.calculateCompositeScore(flags)

        assertEquals(100.0, score.totalScore, 0.01)
        assertEquals(RedFlagSeverity.CRITICAL, score.severity)
    }

    @Test
    fun `test composite score - no flags results in zero score`() {
        val flags = emptyList<DetectedFlag>()

        val score = redFlagDetector.calculateCompositeScore(flags)

        assertEquals(0.0, score.totalScore, 0.01)
        assertEquals(RedFlagSeverity.LOW, score.severity)
    }

    // ========== Thread Safety Tests ==========

    @Test
    fun `test multiple concurrent detections are thread-safe`() = runTest(testDispatcher) {
        val filings = listOf(createFiling(type = "Consolidation", daysAgo = 30))
        coEvery { filingRepository.getFilingsForStock(testStockId) } returns flowOf(filings)
        coEvery { executiveRepository.getExecutivesForStock(testStockId) } returns flowOf(emptyList())

        // Multiple calls should not interfere with each other
        val flags1 = redFlagDetector.detectFlags(testTicker, testStockId)
        val flags2 = redFlagDetector.detectFlags(testTicker, testStockId)

        assertEquals(flags1.size, flags2.size)
        assertEquals(flags1[0].score, flags2[0].score, 0.01)
    }

    // ========== Helper Functions ==========

    private fun createFiling(
        type: String = "Regular Filing",
        summary: String = "Regular filing summary",
        daysAgo: Long = 0
    ): Filing {
        return Filing(
            id = "filing-${System.nanoTime()}",
            stockId = testStockId,
            type = type,
            title = "Test Filing",
            date = now - TimeUnit.DAYS.toMillis(daysAgo),
            summary = summary,
            isRead = false,
            isMaterial = false
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
