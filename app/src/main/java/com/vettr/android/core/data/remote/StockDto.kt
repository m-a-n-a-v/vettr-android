package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Stock entity from admin API.
 * Maps JSON response fields to Kotlin properties using Gson serialization.
 * Uses camelCase field names matching the admin endpoint response format.
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

    @SerializedName("marketCap")
    val marketCap: Double,

    @SerializedName("price")
    val price: Double,

    @SerializedName("priceChange")
    val priceChange: Double,

    @SerializedName("vetrScore")
    val vetrScore: Int
)
