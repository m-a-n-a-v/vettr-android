package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.StockDao
import com.vettr.android.core.data.remote.StockDto
import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.model.Stock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of StockRepository using network-first approach.
 * Fetches from admin API, caches to Room, and serves from Room via Flow.
 */
class StockRepositoryImpl @Inject constructor(
    private val stockDao: StockDao,
    private val vettrApi: VettrApi
) : StockRepository {

    @Volatile
    private var hasFetchedFromApi = false

    override fun getStocks(): Flow<List<Stock>> {
        return stockDao.getAll().onStart {
            if (!hasFetchedFromApi) {
                refreshFromApi()
            }
        }
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
        if (!hasFetchedFromApi) {
            refreshFromApi()
        }
        return stockDao.getStocksPaginated(limit, offset)
    }

    override suspend fun searchStocksPaginated(query: String, limit: Int, offset: Int): List<Stock> {
        return stockDao.searchByNamePaginated(query, limit, offset)
    }

    private suspend fun refreshFromApi() {
        try {
            val response = vettrApi.getStocks()
            if (response.success) {
                val stocks = response.data.items.map { it.toStock() }
                stockDao.insertAll(stocks)
                hasFetchedFromApi = true
                Timber.d("Fetched ${stocks.size} stocks from API")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch stocks from API")
        }
    }
}

private fun StockDto.toStock(): Stock {
    return Stock(
        id = id,
        ticker = ticker,
        name = name,
        exchange = exchange,
        sector = sector,
        marketCap = marketCap,
        price = price,
        priceChange = priceChange,
        vetrScore = vetrScore,
        isFavorite = false
    )
}
