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
 * VETR Score V2 Calculator - Calculates holistic investment score (0-100) from four pillars.
 *
 * Pillar Weights:
 * - Financial Survival (35%): Cash runway, free cash flow, debt health
 * - Operational Efficiency (25%): Sector-specific operational ratios
 * - Shareholder Structure (25%): Pedigree, dilution, insider conviction, warrant overhang
 * - Market Sentiment (15%): Liquidity, momentum, news sentiment, short interest, analyst targets
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
        // V2 Pillar weights (must sum to 1.0)
        private const val WEIGHT_FINANCIAL_SURVIVAL = 0.35
        private const val WEIGHT_OPERATIONAL_EFFICIENCY = 0.25
        private const val WEIGHT_SHAREHOLDER_STRUCTURE = 0.25
        private const val WEIGHT_MARKET_SENTIMENT = 0.15

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
     * @return VetrScoreResult with overall score and pillar breakdown
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
            val pillars = calculatePillars(ticker, stockId)
            val baseScore = calculateBaseScore(pillars)
            val adjustments = calculateAdjustments(ticker, stockId)
            val overlay = calculateHourlyActionOverlay(stockId)
            val finalScore = (baseScore + adjustments + overlay).coerceIn(0.0, 100.0).toInt()

            VetrScoreResult(
                overallScore = finalScore,
                components = pillars,
                lastUpdated = now
            )
        }

        // Cache the result
        scoreCache[ticker] = CachedScore(result, now)

        result
    }

    /**
     * Calculate all V2 pillar scores.
     */
    private suspend fun calculatePillars(ticker: String, stockId: String): Map<String, Int> {
        return mapOf(
            "financialSurvival" to calculateFinancialSurvivalScore(ticker, stockId),
            "operationalEfficiency" to calculateOperationalEfficiencyScore(stockId),
            "shareholderStructure" to calculateShareholderStructureScore(ticker, stockId),
            "marketSentiment" to calculateMarketSentimentScore(ticker, stockId)
        )
    }

    /**
     * Calculate weighted base score from V2 pillars.
     */
    private fun calculateBaseScore(pillars: Map<String, Int>): Double {
        val financialSurvival = pillars["financialSurvival"] ?: 0
        val operationalEfficiency = pillars["operationalEfficiency"] ?: 0
        val shareholderStructure = pillars["shareholderStructure"] ?: 0
        val marketSentiment = pillars["marketSentiment"] ?: 0

        return (financialSurvival * WEIGHT_FINANCIAL_SURVIVAL) +
               (operationalEfficiency * WEIGHT_OPERATIONAL_EFFICIENCY) +
               (shareholderStructure * WEIGHT_SHAREHOLDER_STRUCTURE) +
               (marketSentiment * WEIGHT_MARKET_SENTIMENT)
    }

    /**
     * Calculate Financial Survival Score (0-100).
     * Evaluates cash runway, free cash flow, and debt health.
     * Uses red flag data for debt trends and filing data for financial disclosures.
     */
    private suspend fun calculateFinancialSurvivalScore(ticker: String, stockId: String): Int {
        val stock = stockRepository.getStock(stockId).first()
        val flags = redFlagRepository.detectFlagsForStock(ticker)
        val filings = filingRepository.getFilingsForStock(stockId).first()

        // Cash runway proxy: market cap relative to burn rate indicators
        val cashRunwayScore = if (stock != null) {
            when {
                stock.marketCap >= 500_000_000 -> 40 // Large cap = strong survival
                stock.marketCap >= 100_000_000 -> 30 // Mid cap = decent survival
                stock.marketCap >= 50_000_000 -> 20  // Small cap = moderate risk
                stock.marketCap >= 10_000_000 -> 10  // Micro cap = higher risk
                else -> 5                             // Nano cap = survival concern
            }
        } else 0

        // Debt health: inverse of debt-related red flags
        val debtFlags = flags.filter { it.type == RedFlagType.DEBT_TREND }
        val debtHealthScore = when {
            debtFlags.isEmpty() -> 35                         // No debt flags = healthy
            debtFlags.sumOf { it.score } < 10 -> 25          // Minor debt concern
            debtFlags.sumOf { it.score } < 20 -> 15          // Moderate debt concern
            else -> 5                                         // Significant debt issues
        }

        // Financial disclosure quality (proxy for FCF transparency)
        val financialFilings = filings.filter {
            it.type.contains("financial", ignoreCase = true) ||
            it.summary.contains("cash flow", ignoreCase = true) ||
            it.summary.contains("balance sheet", ignoreCase = true)
        }
        val disclosureScore = when {
            financialFilings.size >= 4 -> 25   // Excellent disclosure
            financialFilings.size >= 2 -> 20   // Good disclosure
            financialFilings.size >= 1 -> 10   // Some disclosure
            else -> 5                           // Poor disclosure
        }

        return (cashRunwayScore + debtHealthScore + disclosureScore).coerceIn(0, 100)
    }

    /**
     * Calculate Operational Efficiency Score (0-100).
     * Evaluates sector-specific operational ratios using filing data and governance indicators.
     */
    private suspend fun calculateOperationalEfficiencyScore(stockId: String): Int {
        val filings = filingRepository.getFilingsForStock(stockId).first()
        val executives = executiveRepository.getExecutivesForStock(stockId).first()

        if (filings.isEmpty()) return 0

        val now = System.currentTimeMillis()
        val oneYearAgo = now - (365L * 24 * 60 * 60 * 1000)
        val recentFilings = filings.filter { it.date >= oneYearAgo }

        // Filing frequency as operational cadence proxy
        val cadenceScore = when {
            recentFilings.size >= 4 -> 40  // Regular quarterly cadence
            recentFilings.size == 3 -> 30  // Mostly regular
            recentFilings.size == 2 -> 20  // Sparse
            recentFilings.size == 1 -> 10  // Minimal
            else -> 0
        }

        // Filing consistency (operational regularity)
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
                avgGap <= 100 -> 30  // Excellent: ~quarterly
                avgGap <= 150 -> 25  // Good: mostly regular
                avgGap <= 200 -> 15  // Moderate: some delays
                else -> 5            // Poor: significant delays
            }
        } else {
            15 // Default for insufficient data
        }

        // Executive specialization as operational competence indicator
        val specScore = if (executives.isNotEmpty()) {
            val avgTenure = executives.map { it.yearsAtCompany }.average()
            when {
                avgTenure >= 5.0 -> 30  // Deep operational expertise
                avgTenure >= 3.0 -> 25  // Good operational knowledge
                avgTenure >= 2.0 -> 20  // Moderate experience
                avgTenure >= 1.0 -> 15  // Limited experience
                else -> 10              // Very limited
            }
        } else 10

        return (cadenceScore + consistencyScore + specScore).coerceIn(0, 100)
    }

    /**
     * Calculate Shareholder Structure Score (0-100).
     * Evaluates executive pedigree, dilution risk, insider conviction, and warrant overhang.
     */
    private suspend fun calculateShareholderStructureScore(ticker: String, stockId: String): Int {
        val executiveScore = executiveRepository.getExecutiveScore(stockId)
        val flags = redFlagRepository.detectFlagsForStock(ticker)
        val executives = executiveRepository.getExecutivesForStock(stockId).first()

        // Pedigree component (executive quality)
        val pedigreeScore = (executiveScore * 0.35).coerceIn(0.0, 35.0).toInt()

        // Dilution / financing risk (inverse of consolidation + financing velocity flags)
        val dilutionFlags = flags.filter {
            it.type == RedFlagType.CONSOLIDATION_VELOCITY || it.type == RedFlagType.FINANCING_VELOCITY
        }
        val dilutionScore = when {
            dilutionFlags.isEmpty() -> 30                      // No dilution risk
            dilutionFlags.sumOf { it.score } < 15 -> 20       // Low dilution risk
            dilutionFlags.sumOf { it.score } < 30 -> 10       // Moderate dilution risk
            else -> 0                                          // High dilution risk
        }

        // Insider conviction proxy (executive tenure + team size)
        val insiderScore = if (executives.isNotEmpty()) {
            val teamFactor = when {
                executives.size >= 5 -> 20  // Strong team
                executives.size >= 3 -> 15  // Adequate team
                executives.size >= 2 -> 10  // Minimal team
                else -> 5                    // Very small team
            }
            val avgTenure = executives.map { it.yearsAtCompany }.average()
            val tenureFactor = when {
                avgTenure >= 4.0 -> 15  // High conviction
                avgTenure >= 2.0 -> 10  // Moderate conviction
                else -> 5               // Low conviction
            }
            (teamFactor + tenureFactor).coerceAtMost(35)
        } else 5

        return (pedigreeScore + dilutionScore + insiderScore).coerceIn(0, 100)
    }

    /**
     * Calculate Market Sentiment Score (0-100).
     * Evaluates liquidity and news sentiment (momentum removed — replaced by Hourly Action Overlay).
     * Re-weighted after removing momentum sub-metric.
     */
    private suspend fun calculateMarketSentimentScore(ticker: String, stockId: String): Int {
        val stock = stockRepository.getStock(stockId).first() ?: return 0
        val flags = redFlagRepository.detectFlagsForStock(ticker)

        // Liquidity proxy: market cap as indicator of tradability
        // Re-weighted: was 30 max out of 100, now ~46 max out of 100 (30/65 * 100)
        val liquidityScore = when {
            stock.marketCap >= 1_000_000_000 -> 46  // Highly liquid
            stock.marketCap >= 250_000_000 -> 38    // Good liquidity
            stock.marketCap >= 100_000_000 -> 31    // Moderate liquidity
            stock.marketCap >= 50_000_000 -> 23     // Low liquidity
            else -> 15                               // Very low liquidity
        }

        // Sentiment penalty from disclosure gaps / executive churn
        // Re-weighted: was 30 max, now ~46 max (30/65 * 100)
        val sentimentFlags = flags.filter {
            it.type == RedFlagType.DISCLOSURE_GAPS || it.type == RedFlagType.EXECUTIVE_CHURN
        }
        val sentimentScore = when {
            sentimentFlags.isEmpty() -> 54                     // Clean sentiment
            sentimentFlags.sumOf { it.score } < 10 -> 36      // Minor concern
            sentimentFlags.sumOf { it.score } < 20 -> 18      // Moderate concern
            else -> 0                                           // Significant concern
        }

        return (liquidityScore + sentimentScore).coerceIn(0, 100)
    }

    /**
     * Calculate Hourly Action Overlay: volatility-adjusted price action tilt (±7.5 max).
     * Applied after base score calculation as a post-processing step.
     *
     * Steps:
     * A: Return% = (currentPrice - previousClose) / previousClose * 100
     * B: Z-Score = Return% / ATR% (14-day ATR)
     * C: Dynamic Tilt = 15 * (sigmoid(Z) - 0.5) → range ±7.5
     */
    private suspend fun calculateHourlyActionOverlay(stockId: String): Double {
        val stock = stockRepository.getStock(stockId).first() ?: return 0.0
        val currentPrice = stock.price
        val previousClose = stock.price - stock.priceChange // Approximate previous close

        if (currentPrice <= 0.0 || previousClose <= 0.0) return 0.0

        // Step A: Hourly Return
        val returnPct = ((currentPrice - previousClose) / previousClose) * 100.0

        // Step B: Z-Score — use a default ATR% of 5% for local calculation
        // (Server-side uses actual 14-day ATR from OHLC data)
        val atrPct = 5.0
        val zScore = if (atrPct == 0.0) 0.0 else returnPct / atrPct

        // Step C: Sigmoid cap → Dynamic Tilt (±7.5 max)
        val sigmoid = 1.0 / (1.0 + kotlin.math.exp(-zScore))
        return 15.0 * (sigmoid - 0.5)
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
 * Result of VETR Score V2 calculation with pillar breakdown.
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
