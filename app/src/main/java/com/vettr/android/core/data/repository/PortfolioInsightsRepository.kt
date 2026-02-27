package com.vettr.android.core.data.repository

import com.vettr.android.core.model.PortfolioInsight
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI-generated portfolio insights.
 */
interface PortfolioInsightsRepository {

    /**
     * Get active (non-dismissed) insights.
     */
    fun getInsights(): Flow<List<PortfolioInsight>>

    /**
     * Dismiss an insight by ID.
     */
    suspend fun dismissInsight(id: String): Result<Unit>

    /**
     * Refresh insights from API.
     */
    suspend fun refreshInsights()
}
