package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.RedFlagHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface RedFlagHistoryDao {
    @Query("SELECT * FROM red_flag_history WHERE stock_ticker = :ticker ORDER BY detected_at DESC")
    fun getByStockTicker(ticker: String): Flow<List<RedFlagHistory>>

    @Query("SELECT * FROM red_flag_history ORDER BY detected_at DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<RedFlagHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flag: RedFlagHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flags: List<RedFlagHistory>)

    @Query("UPDATE red_flag_history SET is_acknowledged = 1 WHERE id = :id")
    suspend fun acknowledge(id: String)

    @Query("UPDATE red_flag_history SET is_acknowledged = 1 WHERE stock_ticker = :ticker")
    suspend fun acknowledgeAllForStock(ticker: String)
}
