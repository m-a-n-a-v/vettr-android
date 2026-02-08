package com.vettr.android.core.data.repository

import com.vettr.android.core.model.Filing
import kotlinx.coroutines.flow.Flow

interface FilingRepository {
    fun getFilingsForStock(stockId: String): Flow<List<Filing>>
    fun getLatestFilings(limit: Int): Flow<List<Filing>>
    suspend fun markAsRead(filingId: String)
}
