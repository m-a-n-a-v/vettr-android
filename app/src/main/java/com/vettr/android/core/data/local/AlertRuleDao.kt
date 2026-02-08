package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vettr.android.core.model.AlertRule
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for AlertRule entities.
 * Manages user-defined alert rules with reactive queries.
 */
@Dao
interface AlertRuleDao {
    /**
     * Get all alert rules for a specific user.
     * @param userId User unique identifier
     * @return Flow emitting list of user's alert rules
     */
    @Query("SELECT * FROM alert_rules WHERE user_id = :userId")
    fun getByUserId(userId: String): Flow<List<AlertRule>>

    /**
     * Get all alert rules for a specific stock ticker.
     * @param ticker Stock ticker symbol
     * @return Flow emitting list of alert rules for the stock
     */
    @Query("SELECT * FROM alert_rules WHERE stock_ticker = :ticker")
    fun getByStockTicker(ticker: String): Flow<List<AlertRule>>

    /**
     * Get a specific alert rule by ID.
     * @param id Alert rule unique identifier
     * @return Flow emitting alert rule or null if not found
     */
    @Query("SELECT * FROM alert_rules WHERE id = :id")
    fun getById(id: String): Flow<AlertRule?>

    /**
     * Get all active alert rules.
     * @return Flow emitting list of active alert rules
     */
    @Query("SELECT * FROM alert_rules WHERE is_active = 1")
    fun getActive(): Flow<List<AlertRule>>

    /**
     * Get count of alert rules for a specific user.
     * @param userId User unique identifier
     * @return Flow emitting count of user's alert rules
     */
    @Query("SELECT COUNT(*) FROM alert_rules WHERE user_id = :userId")
    fun getCountByUserId(userId: String): Flow<Int>

    /**
     * Insert an alert rule, replacing if already exists.
     * @param rule Alert rule to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AlertRule)

    /**
     * Update an existing alert rule.
     * @param rule Alert rule to update
     */
    @Update
    suspend fun update(rule: AlertRule)

    /**
     * Delete an alert rule from the database.
     * @param rule Alert rule to delete
     */
    @Delete
    suspend fun delete(rule: AlertRule)
}
