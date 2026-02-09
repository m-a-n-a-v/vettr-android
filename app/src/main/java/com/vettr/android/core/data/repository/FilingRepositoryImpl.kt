package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.FilingDao
import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.data.remote.toFiling
import com.vettr.android.core.model.Filing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of FilingRepository using network-first approach.
 * Fetches from admin API, caches to Room, and serves from Room via Flow.
 */
class FilingRepositoryImpl @Inject constructor(
    private val filingDao: FilingDao,
    private val vettrApi: VettrApi
) : FilingRepository {

    @Volatile
    private var hasFetchedAllFilings = false

    private val fetchedStockIds = mutableSetOf<String>()

    override fun getFilingsForStock(stockId: String): Flow<List<Filing>> {
        return filingDao.getByStockId(stockId).onStart {
            if (stockId !in fetchedStockIds) {
                refreshFilingsForStock(stockId)
            }
        }
    }

    override fun getLatestFilings(limit: Int): Flow<List<Filing>> {
        return filingDao.getLatest(limit).onStart {
            if (!hasFetchedAllFilings) {
                refreshAllFilings()
            }
        }
    }

    override suspend fun markAsRead(filingId: String) {
        filingDao.markAsRead(filingId)
    }

    private suspend fun refreshFilingsForStock(stockId: String) {
        try {
            val response = vettrApi.getFilingsForStock(stockId = stockId)
            if (response.success) {
                val filings = response.data.items.map { it.toFiling() }
                filingDao.insertAll(filings)
                fetchedStockIds.add(stockId)
                Timber.d("Fetched %d filings for stock %s from API", filings.size, stockId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch filings for stock %s from API", stockId)
        }
    }

    private suspend fun refreshAllFilings() {
        try {
            val response = vettrApi.getFilings()
            if (response.success) {
                val filings = response.data.items.map { it.toFiling() }
                filingDao.insertAll(filings)
                hasFetchedAllFilings = true
                Timber.d("Fetched %d filings from API", filings.size)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch filings from API")
        }
    }
}
