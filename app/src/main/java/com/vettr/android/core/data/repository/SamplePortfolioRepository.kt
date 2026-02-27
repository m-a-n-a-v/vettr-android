package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.SamplePortfolioResponse

/**
 * Repository interface for sample portfolios.
 */
interface SamplePortfolioRepository {

    /**
     * Get all available sample portfolios.
     */
    suspend fun getSamplePortfolios(): Result<List<SamplePortfolioResponse>>
}
