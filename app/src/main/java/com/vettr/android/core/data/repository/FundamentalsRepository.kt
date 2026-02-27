package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.FundamentalsResponse

/**
 * Repository interface for stock fundamentals data.
 */
interface FundamentalsRepository {

    /**
     * Get fundamentals for a specific ticker.
     */
    suspend fun getFundamentals(ticker: String): Result<FundamentalsResponse>
}
