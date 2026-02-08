package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vettr.android.core.model.AlertRule
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertRuleDao {
    @Query("SELECT * FROM alert_rules WHERE user_id = :userId")
    fun getByUserId(userId: String): Flow<List<AlertRule>>

    @Query("SELECT * FROM alert_rules WHERE stock_ticker = :ticker")
    fun getByStockTicker(ticker: String): Flow<List<AlertRule>>

    @Query("SELECT * FROM alert_rules WHERE id = :id")
    fun getById(id: String): Flow<AlertRule?>

    @Query("SELECT * FROM alert_rules WHERE is_active = 1")
    fun getActive(): Flow<List<AlertRule>>

    @Query("SELECT COUNT(*) FROM alert_rules WHERE user_id = :userId")
    fun getCountByUserId(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AlertRule)

    @Update
    suspend fun update(rule: AlertRule)

    @Delete
    suspend fun delete(rule: AlertRule)
}
