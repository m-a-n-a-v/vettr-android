package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.FilingDao
import com.vettr.android.core.model.Filing
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FilingRepositoryImpl @Inject constructor(
    private val filingDao: FilingDao
) : FilingRepository {

    override fun getFilingsForStock(stockId: String): Flow<List<Filing>> {
        return filingDao.getByStockId(stockId)
    }

    override fun getLatestFilings(limit: Int): Flow<List<Filing>> {
        return filingDao.getLatest(limit)
    }

    override suspend fun markAsRead(filingId: String) {
        filingDao.markAsRead(filingId)
    }
}
