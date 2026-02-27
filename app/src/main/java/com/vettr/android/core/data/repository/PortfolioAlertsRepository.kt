package com.vettr.android.core.data.repository

import com.vettr.android.core.model.PortfolioAlert
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for portfolio alerts with caching.
 */
interface PortfolioAlertsRepository {

    /**
     * Get all portfolio alerts.
     */
    fun getAlerts(): Flow<List<PortfolioAlert>>

    /**
     * Get unread alert count.
     */
    fun getUnreadCount(): Flow<Int>

    /**
     * Mark a single alert as read.
     */
    suspend fun markRead(id: String): Result<Unit>

    /**
     * Mark all alerts as read.
     */
    suspend fun markAllRead(): Result<Unit>

    /**
     * Refresh alerts from API.
     */
    suspend fun refreshAlerts()
}
