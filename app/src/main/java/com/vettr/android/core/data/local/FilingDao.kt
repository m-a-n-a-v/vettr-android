package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.Filing
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Filing entities.
 * Manages SEC filing data with reactive queries.
 */
@Dao
interface FilingDao {

    /**
     * Get all filings for a specific stock, ordered by date descending.
     * @param stockId Stock unique identifier
     * @return Flow emitting list of filings for the stock
     */
    @Query("SELECT * FROM filings WHERE stock_id = :stockId ORDER BY date DESC")
    fun getByStockId(stockId: String): Flow<List<Filing>>

    /**
     * Get the latest filings across all stocks.
     * @param limit Maximum number of filings to return
     * @return Flow emitting list of recent filings
     */
    @Query("SELECT * FROM filings ORDER BY date DESC LIMIT :limit")
    fun getLatest(limit: Int): Flow<List<Filing>>

    /**
     * Get a specific filing by ID.
     * @param id Filing unique identifier
     * @return Flow emitting filing or null if not found
     */
    @Query("SELECT * FROM filings WHERE id = :id")
    fun getById(id: String): Flow<Filing?>

    /**
     * Insert a single filing, replacing if already exists.
     * @param filing Filing to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(filing: Filing)

    /**
     * Insert multiple filings, replacing if already exist.
     * @param filings List of filings to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(filings: List<Filing>)

    /**
     * Mark a filing as read by the user.
     * @param id Filing unique identifier
     */
    @Query("UPDATE filings SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}
