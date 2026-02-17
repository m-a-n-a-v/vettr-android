package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for pulse/summary endpoint.
 */
data class PulseSummaryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PulseSummaryDto
)

/**
 * DTO for pulse summary data from the API.
 */
data class PulseSummaryDto(
    @SerializedName("watchlist_health") val watchlistHealth: WatchlistHealthDto,
    @SerializedName("sector_exposure") val sectorExposure: List<SectorExposureDto>,
    @SerializedName("red_flag_categories") val redFlagCategories: RedFlagCategoriesDto
)

data class WatchlistHealthDto(
    @SerializedName("elite") val elite: HealthBucketDto,
    @SerializedName("contender") val contender: HealthBucketDto,
    @SerializedName("watchlist") val watchlist: HealthBucketDto,
    @SerializedName("speculative") val speculative: HealthBucketDto,
    @SerializedName("toxic") val toxic: HealthBucketDto
)

data class HealthBucketDto(
    @SerializedName("count") val count: Int,
    @SerializedName("pct") val pct: Int
)

data class SectorExposureDto(
    @SerializedName("sector") val sector: String,
    @SerializedName("exchange") val exchange: String,
    @SerializedName("count") val count: Int,
    @SerializedName("pct") val pct: Int
)

data class RedFlagCategoriesDto(
    @SerializedName("critical_count") val criticalCount: Int,
    @SerializedName("warning_count") val warningCount: Int,
    @SerializedName("categories") val categories: List<RedFlagCategoryItemDto>,
    @SerializedName("latest_alert") val latestAlert: LatestAlertDto?
)

data class RedFlagCategoryItemDto(
    @SerializedName("category") val category: String,
    @SerializedName("label") val label: String,
    @SerializedName("stock_count") val stockCount: Int,
    @SerializedName("severity") val severity: String
)

data class LatestAlertDto(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("label") val label: String,
    @SerializedName("description") val description: String,
    @SerializedName("is_new") val isNew: Boolean
)
