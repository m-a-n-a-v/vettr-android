package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.VetrScoreHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface VetrScoreHistoryDao {
    @Query("SELECT * FROM vetr_score_history WHERE stock_ticker = :ticker ORDER BY calculated_at DESC LIMIT :limit")
    fun getByStockTicker(ticker: String, limit: Int): Flow<List<VetrScoreHistory>>

    @Query("SELECT * FROM vetr_score_history WHERE stock_ticker = :ticker ORDER BY calculated_at DESC LIMIT 1")
    fun getLatest(ticker: String): Flow<VetrScoreHistory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: VetrScoreHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scores: List<VetrScoreHistory>)
}
