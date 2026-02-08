package com.vettr.android.core.data.repository

import com.vettr.android.core.model.Executive
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Executive data operations.
 * Provides abstraction over data sources for executive/management team data.
 */
interface ExecutiveRepository {
    /**
     * Get all executives for a specific stock.
     * @param stockId Stock unique identifier
     * @return Flow emitting list of executives for the stock
     */
    fun getExecutivesForStock(stockId: String): Flow<List<Executive>>

    /**
     * Search executives by name.
     * @param query Search query string
     * @return Flow emitting list of matching executives
     */
    fun searchByName(query: String): Flow<List<Executive>>

    /**
     * Calculate executive score for a stock based on team quality.
     * Score considers tenure, experience, and specialization.
     * @param stockId Stock unique identifier
     * @return Executive quality score (0-100)
     */
    suspend fun getExecutiveScore(stockId: String): Int
}
