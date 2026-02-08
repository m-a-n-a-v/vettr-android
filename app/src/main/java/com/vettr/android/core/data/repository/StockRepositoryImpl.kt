package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.StockDao
import com.vettr.android.core.model.Stock
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of StockRepository using local database as data source.
 * Provides local-first data access with Flow-based reactive updates.
 */
class StockRepositoryImpl @Inject constructor(
    private val stockDao: StockDao
) : StockRepository {

    override fun getStocks(): Flow<List<Stock>> {
        return stockDao.getAll()
    }

    override fun getStock(id: String): Flow<Stock?> {
        return stockDao.getById(id)
    }

    override fun searchStocks(query: String): Flow<List<Stock>> {
        return stockDao.searchByName(query)
    }

    override fun getFavorites(): Flow<List<Stock>> {
        return stockDao.getFavorites()
    }

    override suspend fun toggleFavorite(stockId: String) {
        stockDao.toggleFavorite(stockId)
    }

    override suspend fun getStocksPaginated(limit: Int, offset: Int): List<Stock> {
        return stockDao.getStocksPaginated(limit, offset)
    }

    override suspend fun searchStocksPaginated(query: String, limit: Int, offset: Int): List<Stock> {
        return stockDao.searchByNamePaginated(query, limit, offset)
    }
}
