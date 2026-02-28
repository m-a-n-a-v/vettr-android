package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for portfolio API endpoints.
 */
data class PortfolioResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("accountId") val accountId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("holdingsCount") val holdingsCount: Int = 0,
    @SerializedName("totalValue") val totalValue: Double = 0.0,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

/**
 * Request payload for creating a portfolio.
 */
data class CreatePortfolioRequest(
    @SerializedName("name") val name: String,
    @SerializedName("provider") val provider: String
)

/**
 * Response for a single portfolio holding.
 */
data class HoldingResponse(
    @SerializedName("id") val id: String,
    @SerializedName("portfolioId") val portfolioId: String,
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("avgCost") val avgCost: Double,
    @SerializedName("currentPrice") val currentPrice: Double = 0.0,
    @SerializedName("currentValue") val currentValue: Double = 0.0,
    @SerializedName("gainLoss") val gainLoss: Double = 0.0,
    @SerializedName("gainLossPercent") val gainLossPercent: Double = 0.0,
    @SerializedName("vetrScore") val vetrScore: Int? = null,
    @SerializedName("priceChangePercent") val priceChangePercent: Double? = null
)

/**
 * Request payload for adding or updating a holding.
 */
data class AddHoldingRequest(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("avgCost") val avgCost: Double
)

/**
 * Request payload for updating a holding.
 */
data class UpdateHoldingRequest(
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("avgCost") val avgCost: Double
)

/**
 * Portfolio summary response aggregating all portfolio data.
 */
data class PortfolioSummaryResponse(
    @SerializedName("totalValue") val totalValue: Double,
    @SerializedName("totalCost") val totalCost: Double,
    @SerializedName("totalGainLoss") val totalGainLoss: Double,
    @SerializedName("totalGainLossPercent") val totalGainLossPercent: Double,
    @SerializedName("holdingsCount") val holdingsCount: Int,
    @SerializedName("vetrCoverage") val vetrCoverage: Double = 0.0,
    @SerializedName("portfolioCount") val portfolioCount: Int = 0
)
