package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.model.HealthBucket
import com.vettr.android.core.model.LatestAlert
import com.vettr.android.core.model.PulseSummary
import com.vettr.android.core.model.RedFlagCategories
import com.vettr.android.core.model.RedFlagCategoryItem
import com.vettr.android.core.model.SectorExposureItem
import com.vettr.android.core.model.WatchlistHealth
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of PulseRepository using Retrofit API.
 * Maps DTOs to domain models.
 */
class PulseRepositoryImpl @Inject constructor(
    private val vettrApi: VettrApi
) : PulseRepository {

    override suspend fun getPulseSummary(): PulseSummary? {
        return try {
            val response = vettrApi.getPulseSummary()
            if (response.success) {
                val dto = response.data
                PulseSummary(
                    watchlistHealth = WatchlistHealth(
                        elite = HealthBucket(
                            count = dto.watchlistHealth.elite.count,
                            pct = dto.watchlistHealth.elite.pct
                        ),
                        contender = HealthBucket(
                            count = dto.watchlistHealth.contender.count,
                            pct = dto.watchlistHealth.contender.pct
                        ),
                        watchlist = HealthBucket(
                            count = dto.watchlistHealth.watchlist.count,
                            pct = dto.watchlistHealth.watchlist.pct
                        ),
                        speculative = HealthBucket(
                            count = dto.watchlistHealth.speculative.count,
                            pct = dto.watchlistHealth.speculative.pct
                        ),
                        toxic = HealthBucket(
                            count = dto.watchlistHealth.toxic.count,
                            pct = dto.watchlistHealth.toxic.pct
                        )
                    ),
                    sectorExposure = dto.sectorExposure.map { se ->
                        SectorExposureItem(
                            sector = se.sector,
                            exchange = se.exchange,
                            count = se.count,
                            pct = se.pct
                        )
                    },
                    redFlagCategories = RedFlagCategories(
                        criticalCount = dto.redFlagCategories.criticalCount,
                        warningCount = dto.redFlagCategories.warningCount,
                        categories = dto.redFlagCategories.categories.map { cat ->
                            RedFlagCategoryItem(
                                category = cat.category,
                                label = cat.label,
                                stockCount = cat.stockCount,
                                severity = cat.severity
                            )
                        },
                        latestAlert = dto.redFlagCategories.latestAlert?.let { alert ->
                            LatestAlert(
                                ticker = alert.ticker,
                                label = alert.label,
                                description = alert.description,
                                isNew = alert.isNew
                            )
                        }
                    )
                )
            } else {
                Timber.w("Pulse summary API returned success=false")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch pulse summary from API")
            null
        }
    }
}
