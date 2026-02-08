package com.vettr.android.core.data

import com.vettr.android.core.data.repository.ExecutiveRepository
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.RedFlagRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VETR Score Calculator - Calculates holistic investment score (0-100) from multiple components.
 *
 * Component Weights:
 * - Pedigree (25%): Executive team quality and experience
 * - Filing Velocity (20%): Timeliness and frequency of regulatory filings
 * - Red Flag (25%): Inverse of red flag score (lower red flags = higher score)
 * - Growth (15%): Market cap and price momentum
 * - Governance (15%): Corporate governance quality indicators
 *
 * Adjustments:
 * - Bonus +5: Audited financials and industry expertise
 * - Penalty -10: Overdue filings or regulatory issues
 * - Final score clamped to 0-100 range
 *
 * Caching: Scores cached with 24-hour TTL for performance
 */
@Singleton
class VetrScoreCalculator @Inject constructor(
    private val executiveRepository: ExecutiveRepository,
    private val filingRepository: FilingRepository,
    private val redFlagRepository: RedFlagRepository,
    private val stockRepository: StockRepository,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val mutex = Mutex()
    private val scoreCache = mutableMapOf<String, CachedScore>()

    companion object {
        // Component weights (must sum to 1.0)
        private const val WEIGHT_PEDIGREE = 0.25
        private const val WEIGHT_FILING_VELOCITY = 0.20
        private const val WEIGHT_RED_FLAG = 0.25
        private const val WEIGHT_GROWTH = 0.15
        private const val WEIGHT_GOVERNANCE = 0.15

        // Bonus and penalty adjustments
        private const val BONUS_AUDITED_FINANCIALS = 5
        private const val PENALTY_OVERDUE_FILINGS = -10

        // Cache TTL: 24 hours in milliseconds
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L
    }

    /**
     * Calculate VETR Score for a stock ticker.
     * Thread-safe operation with caching.
     *
     * @param ticker Stock ticker symbol
     * @param stockId Stock unique identifier
     * @return VetrScoreResult with overall score and component breakdown
     */
    suspend fun calculateScore(ticker: String, stockId: String): VetrScoreResult = mutex.withLock {
        // Check cache first
        val cached = scoreCache[ticker]
        val now = System.currentTimeMillis()

        if (cached != null && (now - cached.calculatedAt) < CACHE_TTL_MS) {
            return cached.result
        }

        // Calculate fresh score
        val result = withContext(dispatcher) {
            val components = calculateComponents(ticker, stockId)
            val baseScore = calculateBaseScore(components)
            val adjustments = calculateAdjustments(ticker, stockId)
            val finalScore = (baseScore + adjustments).coerceIn(0.0, 100.0).toInt()

            VetrScoreResult(
                overallScore = finalScore,
                components = components,
                lastUpdated = now
            )
        }

        // Cache the result
        scoreCache[ticker] = CachedScore(result, now)

        result
    }

    /**
     * Calculate all component scores.
     */
    private suspend fun calculateComponents(ticker: String, stockId: String): Map<String, Int> {
        return mapOf(
            "pedigree" to calculatePedigreeScore(stockId),
            "filingVelocity" to calculateFilingVelocityScore(stockId),
            "redFlag" to calculateRedFlagScore(ticker),
            "growth" to calculateGrowthScore(stockId),
            "governance" to calculateGovernanceScore(ticker, stockId)
        )
    }

    /**
     * Calculate weighted base score from components.
     */
    private fun calculateBaseScore(components: Map<String, Int>): Double {
        val pedigree = components["pedigree"] ?: 0
        val filingVelocity = components["filingVelocity"] ?: 0
        val redFlag = components["redFlag"] ?: 0
        val growth = components["growth"] ?: 0
        val governance = components["governance"] ?: 0

        return (pedigree * WEIGHT_PEDIGREE) +
               (filingVelocity * WEIGHT_FILING_VELOCITY) +
               (redFlag * WEIGHT_RED_FLAG) +
               (growth * WEIGHT_GROWTH) +
               (governance * WEIGHT_GOVERNANCE)
    }

    /**
     * Calculate Pedigree Score (0-100) based on executive team quality.
     * Considers tenure, experience, and specialization.
     */
    private suspend fun calculatePedigreeScore(stockId: String): Int {
        val executiveScore = executiveRepository.getExecutiveScore(stockId)
        return executiveScore.coerceIn(0, 100)
    }

    /**
     * Calculate Filing Velocity Score (0-100) based on filing timeliness and frequency.
     * Higher scores for regular, timely filings; lower for delays or gaps.
     */
    private suspend fun calculateFilingVelocityScore(stockId: String): Int {
        val filings = filingRepository.getFilingsForStock(stockId).first()

        if (filings.isEmpty()) return 0

        val now = System.currentTimeMillis()
        val oneYearAgo = now - (365L * 24 * 60 * 60 * 1000)
        val recentFilings = filings.filter { it.date >= oneYearAgo }

        // Score based on filing frequency (expected ~4 quarterly filings per year)
        val frequencyScore = when {
            recentFilings.size >= 4 -> 60 // Good: 4+ filings
            recentFilings.size == 3 -> 45 // Moderate: 3 filings
            recentFilings.size == 2 -> 30 // Low: 2 filings
            recentFilings.size == 1 -> 15 // Poor: 1 filing
            else -> 0
        }

        // Score based on consistency (check for large gaps)
        val sortedFilings = filings.sortedByDescending { it.date }.take(4)
        val consistencyScore = if (sortedFilings.size >= 2) {
            val gaps = mutableListOf<Long>()
            for (i in 0 until sortedFilings.size - 1) {
                val gap = sortedFilings[i].date - sortedFilings[i + 1].date
                val gapDays = gap / (24 * 60 * 60 * 1000)
                gaps.add(gapDays)
            }

            val avgGap = gaps.average()
            when {
                avgGap <= 100 -> 40 // Excellent: ~quarterly
                avgGap <= 150 -> 30 // Good: mostly regular
                avgGap <= 200 -> 20 // Moderate: some delays
                else -> 10 // Poor: significant delays
            }
        } else {
            20 // Default for insufficient data
        }

        return (frequencyScore + consistencyScore).coerceIn(0, 100)
    }

    /**
     * Calculate Red Flag Score (0-100) - inverse of red flag severity.
     * Lower red flags = higher score. Score is 100 minus the red flag composite score.
     */
    private suspend fun calculateRedFlagScore(ticker: String): Int {
        val flags = redFlagRepository.detectFlagsForStock(ticker)
        val totalRedFlagScore = flags.sumOf { it.score }

        // Invert the red flag score: 0 red flags = 100 score, 100 red flags = 0 score
        return (100.0 - totalRedFlagScore).coerceIn(0.0, 100.0).toInt()
    }

    /**
     * Calculate Growth Score (0-100) based on market cap and price momentum.
     */
    private suspend fun calculateGrowthScore(stockId: String): Int {
        val stock = stockRepository.getStock(stockId).first() ?: return 0

        // Score based on market cap (larger cap = higher score up to a point)
        val marketCapScore = when {
            stock.marketCap >= 1_000_000_000 -> 40 // $1B+
            stock.marketCap >= 500_000_000 -> 35 // $500M+
            stock.marketCap >= 250_000_000 -> 30 // $250M+
            stock.marketCap >= 100_000_000 -> 25 // $100M+
            stock.marketCap >= 50_000_000 -> 20 // $50M+
            else -> 15 // < $50M
        }

        // Score based on price momentum (positive change = higher score)
        val momentumScore = when {
            stock.priceChange >= 20.0 -> 60 // Excellent: 20%+ gain
            stock.priceChange >= 10.0 -> 50 // Very good: 10-20% gain
            stock.priceChange >= 5.0 -> 40 // Good: 5-10% gain
            stock.priceChange >= 0.0 -> 30 // Positive: 0-5% gain
            stock.priceChange >= -5.0 -> 20 // Slight loss: 0-5% loss
            stock.priceChange >= -10.0 -> 10 // Moderate loss: 5-10% loss
            else -> 0 // Significant loss: 10%+ loss
        }

        return (marketCapScore + momentumScore).coerceIn(0, 100)
    }

    /**
     * Calculate Governance Score (0-100) based on corporate governance indicators.
     * Considers board quality, transparency, and regulatory compliance.
     */
    private suspend fun calculateGovernanceScore(ticker: String, stockId: String): Int {
        val executives = executiveRepository.getExecutivesForStock(stockId).first()
        val filings = filingRepository.getFilingsForStock(stockId).first()

        // Score based on executive team size (adequate governance structure)
        val teamSizeScore = when {
            executives.size >= 5 -> 35 // Strong team
            executives.size >= 3 -> 25 // Adequate team
            executives.size >= 2 -> 15 // Minimal team
            else -> 5 // Very small team
        }

        // Score based on material filings (transparency indicator)
        val materialFilingsCount = filings.count { it.isMaterial }
        val transparencyScore = when {
            materialFilingsCount >= 10 -> 35 // High transparency
            materialFilingsCount >= 5 -> 25 // Good transparency
            materialFilingsCount >= 2 -> 15 // Moderate transparency
            else -> 5 // Low transparency
        }

        // Score based on executive tenure (stability indicator)
        val avgTenure = if (executives.isNotEmpty()) {
            executives.map { it.yearsAtCompany }.average()
        } else 0.0

        val stabilityScore = when {
            avgTenure >= 5.0 -> 30 // Excellent stability
            avgTenure >= 3.0 -> 25 // Good stability
            avgTenure >= 2.0 -> 20 // Moderate stability
            avgTenure >= 1.0 -> 15 // Low stability
            else -> 10 // Very low stability
        }

        return (teamSizeScore + transparencyScore + stabilityScore).coerceIn(0, 100)
    }

    /**
     * Calculate bonus/penalty adjustments.
     */
    private suspend fun calculateAdjustments(ticker: String, stockId: String): Int {
        var adjustment = 0

        // Bonus for audited financials and expertise
        if (hasAuditedFinancials(stockId)) {
            adjustment += BONUS_AUDITED_FINANCIALS
        }

        // Penalty for overdue filings or regulatory issues
        if (hasOverdueFilings(stockId) || hasRegulatoryIssues(ticker)) {
            adjustment += PENALTY_OVERDUE_FILINGS
        }

        return adjustment
    }

    /**
     * Check if stock has audited financials.
     * Looks for audit-related filings in the past year.
     */
    private suspend fun hasAuditedFinancials(stockId: String): Boolean {
        val filings = filingRepository.getFilingsForStock(stockId).first()
        val now = System.currentTimeMillis()
        val oneYearAgo = now - (365L * 24 * 60 * 60 * 1000)

        return filings.any { filing ->
            filing.date >= oneYearAgo && (
                filing.type.contains("audit", ignoreCase = true) ||
                filing.summary.contains("audited", ignoreCase = true) ||
                filing.summary.contains("audit report", ignoreCase = true)
            )
        }
    }

    /**
     * Check if stock has overdue filings.
     * Considers a filing overdue if no filings in the past 6 months.
     */
    private suspend fun hasOverdueFilings(stockId: String): Boolean {
        val filings = filingRepository.getFilingsForStock(stockId).first()
        if (filings.isEmpty()) return true

        val now = System.currentTimeMillis()
        val sixMonthsAgo = now - (180L * 24 * 60 * 60 * 1000)
        val recentFilings = filings.filter { it.date >= sixMonthsAgo }

        return recentFilings.isEmpty()
    }

    /**
     * Check if stock has regulatory issues.
     * Looks for regulatory or compliance-related red flags.
     */
    private suspend fun hasRegulatoryIssues(ticker: String): Boolean {
        val flags = redFlagRepository.detectFlagsForStock(ticker)

        // Check for disclosure gaps (indicator of regulatory issues)
        return flags.any { it.type == RedFlagType.DISCLOSURE_GAPS && it.score >= 10.0 }
    }

    /**
     * Clear the cache for a specific ticker or all tickers.
     */
    suspend fun clearCache(ticker: String? = null) = mutex.withLock {
        if (ticker != null) {
            scoreCache.remove(ticker)
        } else {
            scoreCache.clear()
        }
    }

    /**
     * Get cached score if available and not expired.
     */
    suspend fun getCachedScore(ticker: String): VetrScoreResult? = mutex.withLock {
        val cached = scoreCache[ticker] ?: return@withLock null
        val now = System.currentTimeMillis()

        if ((now - cached.calculatedAt) < CACHE_TTL_MS) {
            cached.result
        } else {
            scoreCache.remove(ticker)
            null
        }
    }
}

/**
 * Result of VETR Score calculation with component breakdown.
 */
data class VetrScoreResult(
    val overallScore: Int,
    val components: Map<String, Int>,
    val lastUpdated: Long
)

/**
 * Cached score with timestamp for TTL management.
 */
private data class CachedScore(
    val result: VetrScoreResult,
    val calculatedAt: Long
)
