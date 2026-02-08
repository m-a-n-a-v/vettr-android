package com.vettr.android.core.data.repository

import com.vettr.android.core.model.Stock
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Stock data operations.
 * Provides abstraction over data sources (local database, remote API, cache).
 */
interface StockRepository {
    /**
     * Get all stocks.
     * @return Flow emitting list of all stocks
     */
    fun getStocks(): Flow<List<Stock>>

    /**
     * Get stock by ID.
     * @param id Stock unique identifier
     * @return Flow emitting stock or null if not found
     */
    fun getStock(id: String): Flow<Stock?>

    /**
     * Search stocks by name or ticker.
     * @param query Search query string
     * @return Flow emitting list of matching stocks
     */
    fun searchStocks(query: String): Flow<List<Stock>>

    /**
     * Get favorite stocks.
     * @return Flow emitting list of favorite stocks
     */
    fun getFavorites(): Flow<List<Stock>>

    /**
     * Toggle favorite status for a stock.
     * @param stockId Stock ID to toggle favorite
     */
    suspend fun toggleFavorite(stockId: String)

    /**
     * Get stocks with pagination.
     * @param limit Number of items to fetch
     * @param offset Starting position
     * @return List of stocks for the requested page
     */
    suspend fun getStocksPaginated(limit: Int, offset: Int): List<Stock>

    /**
     * Search stocks with pagination.
     * @param query Search query string
     * @param limit Number of items to fetch
     * @param offset Starting position
     * @return List of matching stocks for the requested page
     */
    suspend fun searchStocksPaginated(query: String, limit: Int, offset: Int): List<Stock>
}
