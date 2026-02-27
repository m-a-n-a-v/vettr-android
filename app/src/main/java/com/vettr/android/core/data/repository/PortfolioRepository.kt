package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.PortfolioResponse
import com.vettr.android.core.data.remote.PortfolioSummaryResponse
import com.vettr.android.core.data.remote.HoldingResponse
import com.vettr.android.core.model.Portfolio
import com.vettr.android.core.model.PortfolioHolding
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for portfolio CRUD operations with offline support.
 */
interface PortfolioRepository {

    /**
     * Get all portfolios for the current user.
     */
    fun getPortfolios(): Flow<List<Portfolio>>

    /**
     * Create a new portfolio.
     */
    suspend fun createPortfolio(name: String, provider: String): Result<PortfolioResponse>

    /**
     * Delete a portfolio by ID.
     */
    suspend fun deletePortfolio(id: String): Result<Unit>

    /**
     * Get portfolio summary across all portfolios.
     */
    suspend fun getPortfolioSummary(): Result<PortfolioSummaryResponse>

    /**
     * Get holdings for a specific portfolio.
     */
    fun getHoldings(portfolioId: String): Flow<List<PortfolioHolding>>

    /**
     * Add a holding to a portfolio.
     */
    suspend fun addHolding(
        portfolioId: String,
        ticker: String,
        quantity: Double,
        avgCost: Double
    ): Result<HoldingResponse>

    /**
     * Update an existing holding.
     */
    suspend fun updateHolding(
        portfolioId: String,
        holdingId: String,
        quantity: Double,
        avgCost: Double
    ): Result<HoldingResponse>

    /**
     * Delete a holding from a portfolio.
     */
    suspend fun deleteHolding(portfolioId: String, holdingId: String): Result<Unit>

    /**
     * Refresh portfolios from API.
     */
    suspend fun refreshPortfolios()

    /**
     * Refresh holdings from API.
     */
    suspend fun refreshHoldings(portfolioId: String)
}
