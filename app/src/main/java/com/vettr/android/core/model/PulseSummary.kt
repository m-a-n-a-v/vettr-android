package com.vettr.android.core.model

/**
 * Pulse summary data representing the aggregated dashboard state.
 * Contains watchlist health, sector exposure, and red flag categories.
 */
data class PulseSummary(
    val watchlistHealth: WatchlistHealth,
    val sectorExposure: List<SectorExposureItem>,
    val redFlagCategories: RedFlagCategories
)

/**
 * Watchlist health breakdown showing distribution across 5-tier ratings.
 */
data class WatchlistHealth(
    val elite: HealthBucket,
    val contender: HealthBucket,
    val watchlist: HealthBucket,
    val speculative: HealthBucket,
    val toxic: HealthBucket
)

/**
 * A single health bucket with count and percentage.
 */
data class HealthBucket(
    val count: Int,
    val pct: Int
)

/**
 * Sector exposure item showing how many stocks belong to a sector/exchange combination.
 */
data class SectorExposureItem(
    val sector: String,
    val exchange: String,
    val count: Int,
    val pct: Int
)

/**
 * Red flag categories summary with critical/warning counts, category breakdown, and latest alert.
 */
data class RedFlagCategories(
    val criticalCount: Int,
    val warningCount: Int,
    val categories: List<RedFlagCategoryItem>,
    val latestAlert: LatestAlert?
)

/**
 * A single red flag category with severity.
 */
data class RedFlagCategoryItem(
    val category: String,
    val label: String,
    val stockCount: Int,
    val severity: String
)

/**
 * The most recent alert for red flag display.
 */
data class LatestAlert(
    val ticker: String,
    val label: String,
    val description: String,
    val isNew: Boolean
)
