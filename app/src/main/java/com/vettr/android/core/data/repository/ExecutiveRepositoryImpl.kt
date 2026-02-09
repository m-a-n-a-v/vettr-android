package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.ExecutiveDao
import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.data.remote.toExecutive
import com.vettr.android.core.model.Executive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of ExecutiveRepository using network-first approach.
 * Fetches from admin API, caches to Room, and serves from Room via Flow.
 */
class ExecutiveRepositoryImpl @Inject constructor(
    private val executiveDao: ExecutiveDao,
    private val vettrApi: VettrApi
) : ExecutiveRepository {

    @Volatile
    private var hasFetchedAllExecutives = false

    private val fetchedStockIds = mutableSetOf<String>()

    override fun getExecutivesForStock(stockId: String): Flow<List<Executive>> {
        return executiveDao.getByStockId(stockId).onStart {
            if (stockId !in fetchedStockIds) {
                refreshExecutivesForStock(stockId)
            }
        }
    }

    override fun searchByName(query: String): Flow<List<Executive>> {
        return executiveDao.searchByName(query).onStart {
            if (!hasFetchedAllExecutives) {
                refreshAllExecutives()
            }
        }
    }

    override suspend fun getExecutiveScore(stockId: String): Int {
        // Ensure executives are fetched before calculating score
        if (stockId !in fetchedStockIds) {
            refreshExecutivesForStock(stockId)
        }

        val executives = executiveDao.getByStockId(stockId).first()

        if (executives.isEmpty()) {
            return 0
        }

        // Calculate score based on:
        // - Team size (more executives = more coverage)
        // - Average tenure (longer tenure = stability)
        // - Specialization diversity

        val sizeScore = minOf(executives.size * 10, 30) // Max 30 points for team size

        val avgTenure = executives.map { it.yearsAtCompany }.average()
        val tenureScore = when {
            avgTenure >= 5.0 -> 40
            avgTenure >= 3.0 -> 30
            avgTenure >= 1.0 -> 20
            else -> 10
        } // Max 40 points for tenure

        val uniqueSpecializations = executives.map { it.specialization }.distinct().size
        val diversityScore = minOf(uniqueSpecializations * 10, 30) // Max 30 points for diversity

        return (sizeScore + tenureScore + diversityScore).coerceIn(0, 100)
    }

    private suspend fun refreshExecutivesForStock(stockId: String) {
        try {
            val response = vettrApi.getExecutivesForStock(stockId = stockId)
            if (response.success) {
                val executives = response.data.items.map { it.toExecutive() }
                executiveDao.insertAll(executives)
                fetchedStockIds.add(stockId)
                Timber.d("Fetched %d executives for stock %s from API", executives.size, stockId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch executives for stock %s from API", stockId)
        }
    }

    private suspend fun refreshAllExecutives() {
        try {
            val response = vettrApi.getExecutives()
            if (response.success) {
                val executives = response.data.items.map { it.toExecutive() }
                executiveDao.insertAll(executives)
                hasFetchedAllExecutives = true
                Timber.d("Fetched %d executives from API", executives.size)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch executives from API")
        }
    }
}
