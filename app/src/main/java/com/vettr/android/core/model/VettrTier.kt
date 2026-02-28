package com.vettr.android.core.model

import androidx.compose.runtime.Immutable

/**
 * Represents VETTR subscription tiers with associated feature limits.
 */
@Immutable
enum class VettrTier(
    val watchlistLimit: Int,
    val pulseDelayHours: Int,
    val syncIntervalHours: Int
) {
    FREE(
        watchlistLimit = 5,
        pulseDelayHours = 12,
        syncIntervalHours = 24
    ),
    PRO(
        watchlistLimit = 25,
        pulseDelayHours = 4,
        syncIntervalHours = 12
    ),
    PREMIUM(
        watchlistLimit = Int.MAX_VALUE,
        pulseDelayHours = 0,
        syncIntervalHours = 4
    )
}
