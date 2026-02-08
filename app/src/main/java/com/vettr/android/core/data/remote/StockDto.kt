package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Stock entity from API.
 * Maps JSON response fields to Kotlin properties using Gson serialization.
 */
data class StockDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("exchange")
    val exchange: String,

    @SerializedName("sector")
    val sector: String,

    @SerializedName("market_cap")
    val marketCap: Double,

    @SerializedName("price")
    val price: Double,

    @SerializedName("price_change")
    val priceChange: Double,

    @SerializedName("vetr_score")
    val vetrScore: Int,

    @SerializedName("is_favorite")
    val isFavorite: Boolean = false
)
