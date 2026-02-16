package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for Discovery Collections API endpoint.
 * Matches the backend /discovery/collections response format.
 */
data class DiscoveryCollectionsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: DiscoveryCollectionsData
)

/**
 * Data wrapper containing the list of discovery collections.
 */
data class DiscoveryCollectionsData(
    @SerializedName("collections") val collections: List<DiscoveryCollectionDto>
)

/**
 * Data Transfer Object for a Discovery Collection.
 * Represents a curated collection of stocks matching specific criteria.
 */
data class DiscoveryCollectionDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("tagline") val tagline: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("criteria_summary") val criteriaSummary: String,
    @SerializedName("stocks") val stocks: List<CollectionStockDto>
)

/**
 * Data Transfer Object for a Stock within a Collection.
 * Similar to StockDto but uses snake_case field names to match the
 * discovery collections endpoint response format.
 */
data class CollectionStockDto(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("exchange") val exchange: String,
    @SerializedName("sector") val sector: String,
    @SerializedName("market_cap") val marketCap: Double?,
    @SerializedName("price") val price: Double?,
    @SerializedName("price_change") val priceChange: Double?,
    @SerializedName("vetr_score") val vetrScore: Int?
)
