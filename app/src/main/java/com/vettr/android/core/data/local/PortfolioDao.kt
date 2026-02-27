package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vettr.android.core.model.Portfolio
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Portfolio entities.
 * Provides reactive queries using Flow for real-time UI updates.
 */
@Dao
interface PortfolioDao {

    @Query("SELECT * FROM portfolios ORDER BY created_at DESC")
    fun getAll(): Flow<List<Portfolio>>

    @Query("SELECT * FROM portfolios WHERE id = :id")
    fun getById(id: String): Flow<Portfolio?>

    @Query("SELECT * FROM portfolios WHERE user_id = :userId ORDER BY created_at DESC")
    fun getByUserId(userId: String): Flow<List<Portfolio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portfolio: Portfolio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(portfolios: List<Portfolio>)

    @Update
    suspend fun update(portfolio: Portfolio)

    @Delete
    suspend fun delete(portfolio: Portfolio)

    @Query("DELETE FROM portfolios WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM portfolios")
    suspend fun getCount(): Int
}
