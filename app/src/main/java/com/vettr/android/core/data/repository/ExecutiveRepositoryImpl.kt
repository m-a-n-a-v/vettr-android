package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.ExecutiveDao
import com.vettr.android.core.model.Executive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of ExecutiveRepository using local database as data source.
 * Provides local-first data access with Flow-based reactive updates.
 */
class ExecutiveRepositoryImpl @Inject constructor(
    private val executiveDao: ExecutiveDao
) : ExecutiveRepository {

    override fun getExecutivesForStock(stockId: String): Flow<List<Executive>> {
        return executiveDao.getByStockId(stockId)
    }

    override fun searchByName(query: String): Flow<List<Executive>> {
        return executiveDao.searchByName(query)
    }

    override suspend fun getExecutiveScore(stockId: String): Int {
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
}
