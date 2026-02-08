package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.Filing
import kotlinx.coroutines.flow.Flow

@Dao
interface FilingDao {

    @Query("SELECT * FROM filings WHERE stock_id = :stockId ORDER BY date DESC")
    fun getByStockId(stockId: String): Flow<List<Filing>>

    @Query("SELECT * FROM filings ORDER BY date DESC LIMIT :limit")
    fun getLatest(limit: Int): Flow<List<Filing>>

    @Query("SELECT * FROM filings WHERE id = :id")
    fun getById(id: String): Flow<Filing?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(filing: Filing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(filings: List<Filing>)

    @Query("UPDATE filings SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}
