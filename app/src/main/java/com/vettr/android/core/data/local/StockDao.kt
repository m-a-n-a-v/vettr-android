package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vettr.android.core.model.Stock
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Stock entities.
 * Provides reactive queries using Flow for real-time UI updates.
 */
@Dao
interface StockDao {
    /**
     * Get all stocks from the database.
     * @return Flow emitting list of all stocks
     */
    @Query("SELECT * FROM stocks")
    fun getAll(): Flow<List<Stock>>

    /**
     * Get stock by unique identifier.
     * @param id Stock ID
     * @return Flow emitting stock or null if not found
     */
    @Query("SELECT * FROM stocks WHERE id = :id")
    fun getById(id: String): Flow<Stock?>

    /**
     * Get stock by ticker symbol.
     * @param ticker Stock ticker (e.g., "AAPL")
     * @return Flow emitting stock or null if not found
     */
    @Query("SELECT * FROM stocks WHERE ticker = :ticker")
    fun getByTicker(ticker: String): Flow<Stock?>

    /**
     * Search stocks by name or ticker using wildcard matching.
     * @param query Search query string
     * @return Flow emitting list of matching stocks
     */
    @Query("SELECT * FROM stocks WHERE name LIKE '%' || :query || '%' OR ticker LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<Stock>>

    /**
     * Get all stocks marked as favorites.
     * @return Flow emitting list of favorite stocks
     */
    @Query("SELECT * FROM stocks WHERE is_favorite = 1")
    fun getFavorites(): Flow<List<Stock>>

    /**
     * Get stocks with pagination support, ordered by ticker.
     * @param limit Number of items per page
     * @param offset Starting position
     * @return List of stocks for the requested page
     */
    @Query("SELECT * FROM stocks ORDER BY ticker ASC LIMIT :limit OFFSET :offset")
    suspend fun getStocksPaginated(limit: Int, offset: Int): List<Stock>

    /**
     * Search stocks with pagination support, ordered by ticker.
     * @param query Search query string
     * @param limit Number of items per page
     * @param offset Starting position
     * @return List of matching stocks for the requested page
     */
    @Query("SELECT * FROM stocks WHERE name LIKE '%' || :query || '%' OR ticker LIKE '%' || :query || '%' ORDER BY ticker ASC LIMIT :limit OFFSET :offset")
    suspend fun searchByNamePaginated(query: String, limit: Int, offset: Int): List<Stock>

    /**
     * Get total count of stocks in the database.
     * @return Total number of stocks
     */
    @Query("SELECT COUNT(*) FROM stocks")
    suspend fun getStocksCount(): Int

    /**
     * Get count of stocks matching search query.
     * @param query Search query string
     * @return Number of matching stocks
     */
    @Query("SELECT COUNT(*) FROM stocks WHERE name LIKE '%' || :query || '%' OR ticker LIKE '%' || :query || '%'")
    suspend fun getSearchResultsCount(query: String): Int

    /**
     * Insert a single stock, replacing if already exists.
     * @param stock Stock to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: Stock)

    /**
     * Insert multiple stocks, replacing if already exist.
     * @param stocks List of stocks to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<Stock>)

    /**
     * Update an existing stock.
     * @param stock Stock to update
     */
    @Update
    suspend fun update(stock: Stock)

    /**
     * Delete a stock from the database.
     * @param stock Stock to delete
     */
    @Delete
    suspend fun delete(stock: Stock)

    /**
     * Toggle the favorite status of a stock.
     * @param stockId Stock ID to toggle
     */
    @Query("UPDATE stocks SET is_favorite = NOT is_favorite WHERE id = :stockId")
    suspend fun toggleFavorite(stockId: String)
}
