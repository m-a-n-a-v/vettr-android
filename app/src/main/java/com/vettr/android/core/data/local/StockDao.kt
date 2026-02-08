package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vettr.android.core.model.Stock
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks")
    fun getAll(): Flow<List<Stock>>

    @Query("SELECT * FROM stocks WHERE id = :id")
    fun getById(id: String): Flow<Stock?>

    @Query("SELECT * FROM stocks WHERE ticker = :ticker")
    fun getByTicker(ticker: String): Flow<Stock?>

    @Query("SELECT * FROM stocks WHERE name LIKE '%' || :query || '%' OR ticker LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<Stock>>

    @Query("SELECT * FROM stocks WHERE is_favorite = 1")
    fun getFavorites(): Flow<List<Stock>>

    // Pagination queries
    @Query("SELECT * FROM stocks ORDER BY ticker ASC LIMIT :limit OFFSET :offset")
    suspend fun getStocksPaginated(limit: Int, offset: Int): List<Stock>

    @Query("SELECT * FROM stocks WHERE name LIKE '%' || :query || '%' OR ticker LIKE '%' || :query || '%' ORDER BY ticker ASC LIMIT :limit OFFSET :offset")
    suspend fun searchByNamePaginated(query: String, limit: Int, offset: Int): List<Stock>

    @Query("SELECT COUNT(*) FROM stocks")
    suspend fun getStocksCount(): Int

    @Query("SELECT COUNT(*) FROM stocks WHERE name LIKE '%' || :query || '%' OR ticker LIKE '%' || :query || '%'")
    suspend fun getSearchResultsCount(query: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: Stock)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<Stock>)

    @Update
    suspend fun update(stock: Stock)

    @Delete
    suspend fun delete(stock: Stock)

    @Query("UPDATE stocks SET is_favorite = NOT is_favorite WHERE id = :stockId")
    suspend fun toggleFavorite(stockId: String)
}
