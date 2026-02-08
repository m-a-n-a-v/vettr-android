package com.vettr.android.core.data

import com.vettr.android.core.data.repository.ExecutiveRepository
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.model.Filing
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe red flag detector for analyzing stock filing patterns and executive changes.
 * Detects and scores 5 types of red flags with weighted composite scoring.
 */
@Singleton
class RedFlagDetector @Inject constructor(
    private val filingRepository: FilingRepository,
    private val executiveRepository: ExecutiveRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val mutex = Mutex()

    /**
     * Detects all red flags for a given stock ticker.
     * Thread-safe operation using Mutex.
     */
    suspend fun detectFlags(ticker: String, stockId: String): List<DetectedFlag> = mutex.withLock {
        withContext(dispatcher) {
            val filings = filingRepository.getFilingsForStock(stockId).first()
            val executives = executiveRepository.getExecutivesForStock(stockId).first()

            val flags = mutableListOf<DetectedFlag>()

            // Detect each type of red flag
            detectConsolidationVelocity(ticker, filings)?.let { flags.add(it) }
            detectFinancingVelocity(ticker, filings)?.let { flags.add(it) }
            detectExecutiveChurn(ticker, executives)?.let { flags.add(it) }
            detectDisclosureGaps(ticker, filings)?.let { flags.add(it) }
            detectDebtTrend(ticker, filings)?.let { flags.add(it) }

            flags
        }
    }

    /**
     * Calculates composite red flag score from detected flags.
     * Score is 0-100 with severity thresholds: <30 (low), 30-60 (moderate), 60-85 (high), >85 (critical)
     */
    fun calculateCompositeScore(flags: List<DetectedFlag>): RedFlagScore {
        val totalScore = flags.sumOf { it.score }
        val severity = when {
            totalScore < 30 -> RedFlagSeverity.LOW
            totalScore < 60 -> RedFlagSeverity.MODERATE
            totalScore < 85 -> RedFlagSeverity.HIGH
            else -> RedFlagSeverity.CRITICAL
        }
        return RedFlagScore(totalScore, severity, flags)
    }

    /**
     * Detects consolidation velocity (30% weight).
     * Flags rapid share consolidations which may indicate dilution concerns.
     */
    private fun detectConsolidationVelocity(ticker: String, filings: List<Filing>): DetectedFlag? {
        val consolidationFilings = filings.filter {
            it.type.contains("consolidation", ignoreCase = true) ||
            it.summary.contains("consolidation", ignoreCase = true)
        }

        if (consolidationFilings.isEmpty()) return null

        // Calculate velocity: number of consolidations in last 12 months
        val now = System.currentTimeMillis()
        val oneYearAgo = now - (365L * 24 * 60 * 60 * 1000)
        val recentConsolidations = consolidationFilings.count { it.date >= oneYearAgo }

        val score = when {
            recentConsolidations >= 3 -> 30.0 // Critical: 3+ in a year
            recentConsolidations == 2 -> 22.5 // High: 2 in a year
            recentConsolidations == 1 -> 15.0 // Moderate: 1 in a year
            else -> 0.0
        }

        if (score == 0.0) return null

        return DetectedFlag(
            type = RedFlagType.CONSOLIDATION_VELOCITY,
            ticker = ticker,
            score = score,
            description = "Detected $recentConsolidations share consolidation(s) in the past year. " +
                    "Frequent consolidations may indicate ongoing dilution concerns.",
            detectedAt = now
        )
    }

    /**
     * Detects financing velocity (25% weight).
     * Flags frequent equity financings which may indicate cash burn issues.
     */
    private fun detectFinancingVelocity(ticker: String, filings: List<Filing>): DetectedFlag? {
        val financingFilings = filings.filter {
            it.type.contains("financing", ignoreCase = true) ||
            it.type.contains("offering", ignoreCase = true) ||
            it.summary.contains("private placement", ignoreCase = true) ||
            it.summary.contains("equity financing", ignoreCase = true)
        }

        if (financingFilings.isEmpty()) return null

        // Calculate velocity: number of financings in last 12 months
        val now = System.currentTimeMillis()
        val oneYearAgo = now - (365L * 24 * 60 * 60 * 1000)
        val recentFinancings = financingFilings.count { it.date >= oneYearAgo }

        val score = when {
            recentFinancings >= 4 -> 25.0 // Critical: 4+ in a year
            recentFinancings == 3 -> 18.75 // High: 3 in a year
            recentFinancings == 2 -> 12.5 // Moderate: 2 in a year
            recentFinancings == 1 -> 6.25 // Low: 1 in a year
            else -> 0.0
        }

        if (score == 0.0) return null

        return DetectedFlag(
            type = RedFlagType.FINANCING_VELOCITY,
            ticker = ticker,
            score = score,
            description = "Detected $recentFinancings equity financing(s) in the past year. " +
                    "Frequent financings may indicate cash burn or operational challenges.",
            detectedAt = now
        )
    }

    /**
     * Detects executive churn (20% weight).
     * Flags high turnover in C-suite which may indicate instability.
     */
    private fun detectExecutiveChurn(ticker: String, executives: List<com.vettr.android.core.model.Executive>): DetectedFlag? {
        if (executives.isEmpty()) return null

        // Calculate churn: percentage of executives with < 2 years tenure
        val recentHires = executives.count { it.yearsAtCompany < 2.0 }
        val churnRate = (recentHires.toDouble() / executives.size.toDouble()) * 100

        val score = when {
            churnRate >= 60 -> 20.0 // Critical: 60%+ turnover
            churnRate >= 40 -> 15.0 // High: 40-60% turnover
            churnRate >= 25 -> 10.0 // Moderate: 25-40% turnover
            else -> 0.0
        }

        if (score == 0.0) return null

        val now = System.currentTimeMillis()
        return DetectedFlag(
            type = RedFlagType.EXECUTIVE_CHURN,
            ticker = ticker,
            score = score,
            description = "High executive turnover detected: ${recentHires} of ${executives.size} " +
                    "executives have less than 2 years tenure (${churnRate.toInt()}%). " +
                    "May indicate leadership instability.",
            detectedAt = now
        )
    }

    /**
     * Detects disclosure gaps (15% weight).
     * Flags significant delays in required filings.
     */
    private fun detectDisclosureGaps(ticker: String, filings: List<Filing>): DetectedFlag? {
        if (filings.size < 2) return null

        val now = System.currentTimeMillis()
        val sortedFilings = filings.sortedByDescending { it.date }

        // Check for gaps between filings (expected quarterly = ~90 days)
        val gaps = mutableListOf<Long>()
        for (i in 0 until sortedFilings.size - 1) {
            val gap = sortedFilings[i].date - sortedFilings[i + 1].date
            val gapDays = gap / (24 * 60 * 60 * 1000)
            if (gapDays > 120) { // More than 4 months
                gaps.add(gapDays)
            }
        }

        if (gaps.isEmpty()) return null

        val maxGapDays = gaps.maxOrNull() ?: 0
        val score = when {
            maxGapDays >= 240 -> 15.0 // Critical: 8+ months gap
            maxGapDays >= 180 -> 11.25 // High: 6+ months gap
            maxGapDays >= 120 -> 7.5 // Moderate: 4+ months gap
            else -> 0.0
        }

        if (score == 0.0) return null

        return DetectedFlag(
            type = RedFlagType.DISCLOSURE_GAPS,
            ticker = ticker,
            score = score,
            description = "Significant filing gap detected: ${maxGapDays} days between disclosures. " +
                    "Delays may indicate disclosure issues or operational challenges.",
            detectedAt = now
        )
    }

    /**
     * Detects debt trend (10% weight).
     * Flags increasing debt mentions in filings.
     */
    private fun detectDebtTrend(ticker: String, filings: List<Filing>): DetectedFlag? {
        val now = System.currentTimeMillis()
        val sixMonthsAgo = now - (180L * 24 * 60 * 60 * 1000)

        val recentFilings = filings.filter { it.date >= sixMonthsAgo }
        if (recentFilings.isEmpty()) return null

        // Count debt-related mentions
        val debtMentions = recentFilings.count { filing ->
            filing.summary.contains("debt", ignoreCase = true) ||
            filing.summary.contains("loan", ignoreCase = true) ||
            filing.summary.contains("borrowing", ignoreCase = true) ||
            filing.summary.contains("credit facility", ignoreCase = true)
        }

        val debtRate = (debtMentions.toDouble() / recentFilings.size.toDouble()) * 100

        val score = when {
            debtRate >= 60 -> 10.0 // Critical: 60%+ of filings mention debt
            debtRate >= 40 -> 7.5 // High: 40-60% mention debt
            debtRate >= 25 -> 5.0 // Moderate: 25-40% mention debt
            else -> 0.0
        }

        if (score == 0.0) return null

        return DetectedFlag(
            type = RedFlagType.DEBT_TREND,
            ticker = ticker,
            score = score,
            description = "Increasing debt mentions: ${debtMentions} of ${recentFilings.size} " +
                    "recent filings (${debtRate.toInt()}%) reference debt or borrowing. " +
                    "May indicate financial leverage concerns.",
            detectedAt = now
        )
    }
}

/**
 * Types of red flags that can be detected
 */
enum class RedFlagType(val weight: Double) {
    CONSOLIDATION_VELOCITY(0.30),
    FINANCING_VELOCITY(0.25),
    EXECUTIVE_CHURN(0.20),
    DISCLOSURE_GAPS(0.15),
    DEBT_TREND(0.10)
}

/**
 * Severity levels for red flag scores
 */
enum class RedFlagSeverity {
    LOW,        // < 30
    MODERATE,   // 30-60
    HIGH,       // 60-85
    CRITICAL    // > 85
}

/**
 * A detected red flag with score and description
 */
data class DetectedFlag(
    val type: RedFlagType,
    val ticker: String,
    val score: Double,
    val description: String,
    val detectedAt: Long
)

/**
 * Composite red flag score with severity and breakdown
 */
data class RedFlagScore(
    val totalScore: Double,
    val severity: RedFlagSeverity,
    val flags: List<DetectedFlag>
)
