package com.vettr.android.core.data.repository

import com.vettr.android.core.data.DetectedFlag
import com.vettr.android.core.data.RedFlagDetector
import com.vettr.android.core.data.local.RedFlagHistoryDao
import com.vettr.android.core.data.local.StockDao
import com.vettr.android.core.model.RedFlagHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of RedFlagRepository using RedFlagDetector and local database.
 * Handles red flag detection, persistence, and trend analysis.
 */
class RedFlagRepositoryImpl @Inject constructor(
    private val redFlagHistoryDao: RedFlagHistoryDao,
    private val redFlagDetector: RedFlagDetector,
    private val stockDao: StockDao
) : RedFlagRepository {

    override suspend fun detectFlagsForStock(ticker: String): List<DetectedFlag> {
        // Get stock by ticker
        val stock = stockDao.getByTicker(ticker).first()
            ?: throw IllegalArgumentException("Stock not found for ticker: $ticker")

        // Detect flags using RedFlagDetector
        val detectedFlags = redFlagDetector.detectFlags(ticker, stock.id)

        // Persist flags to history
        val historyRecords = detectedFlags.map { flag ->
            RedFlagHistory(
                id = UUID.randomUUID().toString(),
                stockTicker = ticker,
                flagType = flag.type.name,
                severity = redFlagDetector.calculateCompositeScore(listOf(flag)).severity.name,
                score = flag.score,
                description = flag.description,
                detectedAt = flag.detectedAt,
                isAcknowledged = false
            )
        }

        if (historyRecords.isNotEmpty()) {
            redFlagHistoryDao.insertAll(historyRecords)
        }

        return detectedFlags
    }

    override fun getFlagHistory(ticker: String): Flow<List<RedFlagHistory>> {
        return redFlagHistoryDao.getByStockTicker(ticker)
    }

    override suspend fun acknowledgeFlag(id: String) {
        redFlagHistoryDao.acknowledge(id)
    }

    override suspend fun acknowledgeAllForStock(ticker: String) {
        redFlagHistoryDao.acknowledgeAllForStock(ticker)
    }

    override suspend fun getFlagTrend(ticker: String): RedFlagTrend {
        val history = redFlagHistoryDao.getByStockTicker(ticker).first()

        if (history.isEmpty()) {
            return RedFlagTrend(
                ticker = ticker,
                currentScore = 0.0,
                previousScore = 0.0,
                trend = TrendDirection.STABLE,
                flagCount = 0,
                newFlagsCount = 0,
                resolvedFlagsCount = 0
            )
        }

        // Sort by detection time (most recent first)
        val sortedHistory = history.sortedByDescending { it.detectedAt }

        // Get recent flags (last 30 days)
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val recentFlags = sortedHistory.filter { it.detectedAt >= thirtyDaysAgo }

        // Get previous period flags (30-60 days ago)
        val sixtyDaysAgo = now - (60L * 24 * 60 * 60 * 1000)
        val previousFlags = sortedHistory.filter {
            it.detectedAt >= sixtyDaysAgo && it.detectedAt < thirtyDaysAgo
        }

        val currentScore = recentFlags.sumOf { it.score }
        val previousScore = previousFlags.sumOf { it.score }

        val trend = when {
            currentScore < previousScore * 0.9 -> TrendDirection.IMPROVING  // 10%+ improvement
            currentScore > previousScore * 1.1 -> TrendDirection.WORSENING  // 10%+ worsening
            else -> TrendDirection.STABLE
        }

        return RedFlagTrend(
            ticker = ticker,
            currentScore = currentScore,
            previousScore = previousScore,
            trend = trend,
            flagCount = recentFlags.size,
            newFlagsCount = recentFlags.size,
            resolvedFlagsCount = if (previousFlags.size > recentFlags.size) {
                previousFlags.size - recentFlags.size
            } else {
                0
            }
        )
    }
}
