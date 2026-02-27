package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.PortfolioAlert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PortfolioAlert entities.
 */
@Dao
interface PortfolioAlertDao {

    @Query("SELECT * FROM portfolio_alerts ORDER BY triggered_at DESC")
    fun getAll(): Flow<List<PortfolioAlert>>

    @Query("SELECT * FROM portfolio_alerts WHERE portfolio_id = :portfolioId ORDER BY triggered_at DESC")
    fun getByPortfolioId(portfolioId: String): Flow<List<PortfolioAlert>>

    @Query("SELECT * FROM portfolio_alerts WHERE id = :id")
    fun getById(id: String): Flow<PortfolioAlert?>

    @Query("SELECT COUNT(*) FROM portfolio_alerts WHERE is_read = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("UPDATE portfolio_alerts SET is_read = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE portfolio_alerts SET is_read = 1")
    suspend fun markAllRead()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: PortfolioAlert)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<PortfolioAlert>)

    @Query("DELETE FROM portfolio_alerts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM portfolio_alerts")
    suspend fun deleteAll()
}
