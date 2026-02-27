package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response for a sample portfolio.
 */
data class SamplePortfolioResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("stocks") val stocks: List<SamplePortfolioStockDto>
)

/**
 * Stock within a sample portfolio.
 */
data class SamplePortfolioStockDto(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("exchange") val exchange: String,
    @SerializedName("sector") val sector: String,
    @SerializedName("vetrScore") val vetrScore: Int,
    @SerializedName("price") val price: Double
)
