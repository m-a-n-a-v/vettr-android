package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.Executive
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Executive entities.
 * Manages executive/management team data with reactive queries.
 */
@Dao
interface ExecutiveDao {
    /**
     * Get all executives for a specific stock.
     * @param stockId Stock unique identifier
     * @return Flow emitting list of executives for the stock
     */
    @Query("SELECT * FROM executives WHERE stock_id = :stockId")
    fun getByStockId(stockId: String): Flow<List<Executive>>

    /**
     * Search executives by name using wildcard matching.
     * @param query Search query string
     * @return Flow emitting list of matching executives
     */
    @Query("SELECT * FROM executives WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<Executive>>

    /**
     * Get a specific executive by ID.
     * @param id Executive unique identifier
     * @return Flow emitting executive or null if not found
     */
    @Query("SELECT * FROM executives WHERE id = :id")
    fun getById(id: String): Flow<Executive?>

    /**
     * Insert a single executive, replacing if already exists.
     * @param executive Executive to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(executive: Executive)

    /**
     * Insert multiple executives, replacing if already exist.
     * @param executives List of executives to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(executives: List<Executive>)

    /**
     * Delete an executive from the database.
     * @param executive Executive to delete
     */
    @Delete
    suspend fun delete(executive: Executive)
}
