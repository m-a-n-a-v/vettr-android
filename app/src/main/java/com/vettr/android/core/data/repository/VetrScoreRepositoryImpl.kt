package com.vettr.android.core.data.repository

import com.vettr.android.core.data.VetrScoreCalculator
import com.vettr.android.core.data.VetrScoreResult
import com.vettr.android.core.data.local.StockDao
import com.vettr.android.core.data.local.VetrScoreHistoryDao
import com.vettr.android.core.model.VetrScoreHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of VetrScoreRepository using VetrScoreCalculator and local database.
 * Handles score calculation, persistence, trend analysis, and peer comparison.
 */
class VetrScoreRepositoryImpl @Inject constructor(
    private val vetrScoreHistoryDao: VetrScoreHistoryDao,
    private val vetrScoreCalculator: VetrScoreCalculator,
    private val stockDao: StockDao
) : VetrScoreRepository {

    override suspend fun calculateScore(ticker: String): VetrScoreResult {
        // Get stock by ticker
        val stock = stockDao.getByTicker(ticker).first()
            ?: throw IllegalArgumentException("Stock not found for ticker: $ticker")

        // Calculate score using VetrScoreCalculator
        val result = vetrScoreCalculator.calculateScore(ticker, stock.id)

        // Persist score to history
        val historyRecord = VetrScoreHistory(
            id = UUID.randomUUID().toString(),
            stockTicker = ticker,
            overallScore = result.overallScore,
            pedigreeScore = result.components["pedigree"] ?: 0,
            filingVelocityScore = result.components["filingVelocity"] ?: 0,
            redFlagScore = result.components["redFlag"] ?: 0,
            growthScore = result.components["growth"] ?: 0,
            governanceScore = result.components["governance"] ?: 0,
            calculatedAt = result.lastUpdated
        )

        vetrScoreHistoryDao.insert(historyRecord)

        return result
    }

    override fun getScoreHistory(ticker: String, months: Int): Flow<List<VetrScoreHistory>> {
        // Calculate the time threshold for the requested months
        val now = System.currentTimeMillis()
        val monthsAgo = now - (months * 30L * 24 * 60 * 60 * 1000)

        // Get all history for ticker and filter by time range
        return vetrScoreHistoryDao.getByStockTicker(ticker, limit = 1000).map { history ->
            history.filter { it.calculatedAt >= monthsAgo }
        }
    }

    override suspend fun getScoreTrend(ticker: String): ScoreTrend {
        val history = vetrScoreHistoryDao.getByStockTicker(ticker, limit = 100).first()

        if (history.isEmpty()) {
            return ScoreTrend(
                direction = ScoreTrendDirection.STABLE,
                momentum = 0.0,
                scoreChange = 0
            )
        }

        // Get most recent score
        val mostRecent = history.first()
        val currentScore = mostRecent.overallScore

        // Calculate average score over the past month
        val now = System.currentTimeMillis()
        val oneMonthAgo = now - (30L * 24 * 60 * 60 * 1000)
        val recentScores = history.filter { it.calculatedAt >= oneMonthAgo }

        if (recentScores.size < 2) {
            // Not enough data for trend analysis
            return ScoreTrend(
                direction = ScoreTrendDirection.STABLE,
                momentum = 0.0,
                scoreChange = 0
            )
        }

        // Get oldest score in the recent period
        val oldestRecent = recentScores.last()
        val previousScore = oldestRecent.overallScore
        val scoreChange = currentScore - previousScore

        // Calculate momentum as change per week
        val timeDiff = mostRecent.calculatedAt - oldestRecent.calculatedAt
        val weeks = timeDiff / (7.0 * 24 * 60 * 60 * 1000)
        val momentum = if (weeks > 0) scoreChange / weeks else 0.0

        // Determine trend direction
        val direction = when {
            scoreChange > 5 -> ScoreTrendDirection.IMPROVING // 5+ point improvement
            scoreChange < -5 -> ScoreTrendDirection.DECLINING // 5+ point decline
            else -> ScoreTrendDirection.STABLE // Within 5 points
        }

        return ScoreTrend(
            direction = direction,
            momentum = momentum,
            scoreChange = scoreChange
        )
    }

    override suspend fun compareScores(ticker: String): PeerComparison {
        // Get the current stock
        val stock = stockDao.getByTicker(ticker).first()
            ?: throw IllegalArgumentException("Stock not found for ticker: $ticker")

        // Get all stocks in the same sector
        val allStocks = stockDao.getAll().first()
        val peersInSector = allStocks.filter { it.sector == stock.sector && it.ticker != ticker }

        // Calculate sector average
        val sectorScores = peersInSector.map { it.vetrScore }
        val sectorAverage = if (sectorScores.isNotEmpty()) {
            sectorScores.average().toInt()
        } else {
            stock.vetrScore
        }

        // Calculate percentile (what percentage of peers have lower scores)
        val lowerScores = peersInSector.count { it.vetrScore < stock.vetrScore }
        val percentile = if (peersInSector.isNotEmpty()) {
            ((lowerScores.toDouble() / peersInSector.size) * 100).toInt()
        } else {
            50 // Default to median if no peers
        }

        // Get top 5 peers by score for comparison
        val topPeers = peersInSector
            .sortedByDescending { it.vetrScore }
            .take(5)
            .map { peer ->
                PeerScore(
                    ticker = peer.ticker,
                    name = peer.name,
                    score = peer.vetrScore
                )
            }

        return PeerComparison(
            ticker = ticker,
            score = stock.vetrScore,
            sectorAverage = sectorAverage,
            percentile = percentile,
            peerScores = topPeers
        )
    }
}
