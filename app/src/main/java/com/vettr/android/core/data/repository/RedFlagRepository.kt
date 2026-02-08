package com.vettr.android.core.data.repository

import com.vettr.android.core.data.DetectedFlag
import com.vettr.android.core.model.RedFlagHistory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for red flag detection and history.
 * Provides abstraction over red flag detection, storage, and trend analysis.
 */
interface RedFlagRepository {
    /**
     * Detect all red flags for a stock ticker.
     * Runs detection algorithms and returns current flags.
     * @param ticker Stock ticker symbol
     * @return List of detected flags with scores and descriptions
     */
    suspend fun detectFlagsForStock(ticker: String): List<DetectedFlag>

    /**
     * Get red flag history for a stock ticker.
     * Returns all historical flag detections ordered by most recent.
     * @param ticker Stock ticker symbol
     * @return Flow emitting list of red flag history records
     */
    fun getFlagHistory(ticker: String): Flow<List<RedFlagHistory>>

    /**
     * Acknowledge a specific red flag.
     * Marks the flag as acknowledged by the user.
     * @param id Red flag history ID to acknowledge
     */
    suspend fun acknowledgeFlag(id: String)

    /**
     * Acknowledge all red flags for a stock.
     * Marks all flags for the ticker as acknowledged.
     * @param ticker Stock ticker symbol
     */
    suspend fun acknowledgeAllForStock(ticker: String)

    /**
     * Get red flag trend for a stock.
     * Analyzes historical flags to determine if situation is improving or worsening.
     * @param ticker Stock ticker symbol
     * @return Red flag trend analysis
     */
    suspend fun getFlagTrend(ticker: String): RedFlagTrend
}

/**
 * Red flag trend analysis for a stock.
 * Shows whether red flags are increasing, decreasing, or stable over time.
 */
data class RedFlagTrend(
    val ticker: String,
    val currentScore: Double,
    val previousScore: Double,
    val trend: TrendDirection,
    val flagCount: Int,
    val newFlagsCount: Int,
    val resolvedFlagsCount: Int
)

/**
 * Direction of red flag trend
 */
enum class TrendDirection {
    IMPROVING,   // Flags decreasing or score dropping
    WORSENING,   // Flags increasing or score rising
    STABLE       // No significant change
}
