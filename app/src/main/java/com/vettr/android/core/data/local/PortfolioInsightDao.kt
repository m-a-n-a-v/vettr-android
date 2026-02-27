package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.PortfolioInsight
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PortfolioInsight entities.
 */
@Dao
interface PortfolioInsightDao {

    @Query("SELECT * FROM portfolio_insights WHERE is_dismissed = 0 ORDER BY created_at DESC")
    fun getActive(): Flow<List<PortfolioInsight>>

    @Query("SELECT * FROM portfolio_insights ORDER BY created_at DESC")
    fun getAll(): Flow<List<PortfolioInsight>>

    @Query("SELECT * FROM portfolio_insights WHERE portfolio_id = :portfolioId AND is_dismissed = 0 ORDER BY created_at DESC")
    fun getByPortfolioId(portfolioId: String): Flow<List<PortfolioInsight>>

    @Query("SELECT * FROM portfolio_insights WHERE id = :id")
    fun getById(id: String): Flow<PortfolioInsight?>

    @Query("UPDATE portfolio_insights SET is_dismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: PortfolioInsight)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(insights: List<PortfolioInsight>)

    @Query("DELETE FROM portfolio_insights WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM portfolio_insights")
    suspend fun deleteAll()
}
