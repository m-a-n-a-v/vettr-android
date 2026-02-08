package com.vettr.android.core.data.repository

import com.vettr.android.core.data.VetrScoreResult
import com.vettr.android.core.model.VetrScoreHistory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for VETR Score calculation and history.
 * Provides abstraction over score calculation, storage, and trend analysis.
 */
interface VetrScoreRepository {
    /**
     * Calculate VETR Score for a stock ticker.
     * Computes the score using all available data sources and caches the result.
     * @param ticker Stock ticker symbol
     * @return VetrScoreResult with overall score and component breakdown
     */
    suspend fun calculateScore(ticker: String): VetrScoreResult

    /**
     * Get VETR Score history for a stock ticker.
     * Returns historical score data for trend analysis and charting.
     * @param ticker Stock ticker symbol
     * @param months Number of months of history to retrieve
     * @return Flow emitting list of historical score records
     */
    fun getScoreHistory(ticker: String, months: Int): Flow<List<VetrScoreHistory>>

    /**
     * Get VETR Score trend for a stock.
     * Analyzes historical scores to determine if the stock is improving or declining.
     * @param ticker Stock ticker symbol
     * @return Score trend analysis
     */
    suspend fun getScoreTrend(ticker: String): ScoreTrend

    /**
     * Compare VETR Scores with peer stocks.
     * Provides comparative analysis against similar stocks in the same sector.
     * @param ticker Stock ticker symbol
     * @return Peer comparison data
     */
    suspend fun compareScores(ticker: String): PeerComparison
}

/**
 * VETR Score trend analysis for a stock.
 * Shows whether the score is improving, stable, or declining over time.
 */
data class ScoreTrend(
    val direction: ScoreTrendDirection,
    val momentum: Double,
    val scoreChange: Int
)

/**
 * Direction of VETR Score trend
 */
enum class ScoreTrendDirection {
    IMPROVING,   // Score increasing over time
    STABLE,      // Score relatively unchanged
    DECLINING    // Score decreasing over time
}

/**
 * Peer comparison data for a stock's VETR Score.
 * Compares the stock against similar companies in the same sector.
 */
data class PeerComparison(
    val ticker: String,
    val score: Int,
    val sectorAverage: Int,
    val percentile: Int,
    val peerScores: List<PeerScore>
)

/**
 * Individual peer stock score for comparison.
 */
data class PeerScore(
    val ticker: String,
    val name: String,
    val score: Int
)
