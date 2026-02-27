package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vettr.android.core.model.PortfolioHolding
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PortfolioHolding entities.
 */
@Dao
interface PortfolioHoldingDao {

    @Query("SELECT * FROM portfolio_holdings WHERE portfolio_id = :portfolioId ORDER BY ticker ASC")
    fun getByPortfolioId(portfolioId: String): Flow<List<PortfolioHolding>>

    @Query("SELECT * FROM portfolio_holdings WHERE id = :id")
    fun getById(id: String): Flow<PortfolioHolding?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(holding: PortfolioHolding)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holdings: List<PortfolioHolding>)

    @Update
    suspend fun update(holding: PortfolioHolding)

    @Delete
    suspend fun delete(holding: PortfolioHolding)

    @Query("DELETE FROM portfolio_holdings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM portfolio_holdings WHERE portfolio_id = :portfolioId")
    suspend fun deleteByPortfolioId(portfolioId: String)

    @Query("SELECT COUNT(*) FROM portfolio_holdings WHERE portfolio_id = :portfolioId")
    suspend fun getCountByPortfolioId(portfolioId: String): Int
}
