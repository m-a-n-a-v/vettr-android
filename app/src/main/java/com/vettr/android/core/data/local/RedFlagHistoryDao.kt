package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.RedFlagHistory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for RedFlagHistory entities.
 * Manages historical red flag detection data with reactive queries.
 */
@Dao
interface RedFlagHistoryDao {
    /**
     * Get all red flag history records for a specific stock, ordered by most recent.
     * @param ticker Stock ticker symbol
     * @return Flow emitting list of red flag history records
     */
    @Query("SELECT * FROM red_flag_history WHERE stock_ticker = :ticker ORDER BY detected_at DESC")
    fun getByStockTicker(ticker: String): Flow<List<RedFlagHistory>>

    /**
     * Get recent red flag history across all stocks.
     * @param limit Maximum number of records to return
     * @return Flow emitting list of recent red flag records
     */
    @Query("SELECT * FROM red_flag_history ORDER BY detected_at DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<RedFlagHistory>>

    /**
     * Insert a red flag history record, replacing if already exists.
     * @param flag Red flag history record to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flag: RedFlagHistory)

    /**
     * Insert multiple red flag history records, replacing if already exist.
     * @param flags List of red flag history records to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flags: List<RedFlagHistory>)

    /**
     * Mark a specific red flag as acknowledged by the user.
     * @param id Red flag history record ID
     */
    @Query("UPDATE red_flag_history SET is_acknowledged = 1 WHERE id = :id")
    suspend fun acknowledge(id: String)

    /**
     * Mark all red flags for a stock as acknowledged.
     * @param ticker Stock ticker symbol
     */
    @Query("UPDATE red_flag_history SET is_acknowledged = 1 WHERE stock_ticker = :ticker")
    suspend fun acknowledgeAllForStock(ticker: String)
}
