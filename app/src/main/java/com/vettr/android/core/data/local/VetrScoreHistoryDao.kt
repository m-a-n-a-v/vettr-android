package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.VetrScoreHistory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for VetrScoreHistory entities.
 * Manages historical VETR Score data for trend analysis and charting.
 */
@Dao
interface VetrScoreHistoryDao {
    /**
     * Get score history for a specific stock, ordered by most recent.
     * @param ticker Stock ticker symbol
     * @param limit Maximum number of records to return
     * @return Flow emitting list of score history records
     */
    @Query("SELECT * FROM vetr_score_history WHERE stock_ticker = :ticker ORDER BY calculated_at DESC LIMIT :limit")
    fun getByStockTicker(ticker: String, limit: Int): Flow<List<VetrScoreHistory>>

    /**
     * Get the latest score record for a stock.
     * @param ticker Stock ticker symbol
     * @return Flow emitting latest score or null if none exist
     */
    @Query("SELECT * FROM vetr_score_history WHERE stock_ticker = :ticker ORDER BY calculated_at DESC LIMIT 1")
    fun getLatest(ticker: String): Flow<VetrScoreHistory?>

    /**
     * Insert a score history record, replacing if already exists.
     * @param score Score history record to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: VetrScoreHistory)

    /**
     * Insert multiple score history records, replacing if already exist.
     * @param scores List of score history records to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scores: List<VetrScoreHistory>)
}
