package com.vettr.android.core.data.repository

import com.vettr.android.core.model.Filing
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for SEC filing data operations.
 * Provides abstraction over data sources for regulatory filings.
 */
interface FilingRepository {
    /**
     * Get all filings for a specific stock.
     * @param stockId Stock unique identifier
     * @return Flow emitting list of filings for the stock
     */
    fun getFilingsForStock(stockId: String): Flow<List<Filing>>

    /**
     * Get latest filings across all stocks.
     * @param limit Maximum number of filings to return
     * @return Flow emitting list of recent filings
     */
    fun getLatestFilings(limit: Int): Flow<List<Filing>>

    /**
     * Mark a filing as read by the user.
     * @param filingId Filing unique identifier
     */
    suspend fun markAsRead(filingId: String)
}
