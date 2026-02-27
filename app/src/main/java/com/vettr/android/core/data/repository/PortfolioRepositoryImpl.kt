package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.PortfolioDao
import com.vettr.android.core.data.local.PortfolioHoldingDao
import com.vettr.android.core.data.remote.AddHoldingRequest
import com.vettr.android.core.data.remote.CreatePortfolioRequest
import com.vettr.android.core.data.remote.HoldingResponse
import com.vettr.android.core.data.remote.PortfolioResponse
import com.vettr.android.core.data.remote.PortfolioSummaryResponse
import com.vettr.android.core.data.remote.UpdateHoldingRequest
import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.model.Portfolio
import com.vettr.android.core.model.PortfolioHolding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of PortfolioRepository using network-first approach with Room caching.
 */
class PortfolioRepositoryImpl @Inject constructor(
    private val vettrApi: VettrApi,
    private val portfolioDao: PortfolioDao,
    private val holdingDao: PortfolioHoldingDao
) : PortfolioRepository {

    @Volatile
    private var hasFetchedPortfolios = false

    override fun getPortfolios(): Flow<List<Portfolio>> {
        return portfolioDao.getAll().onStart {
            if (!hasFetchedPortfolios) {
                refreshPortfolios()
            }
        }
    }

    override suspend fun createPortfolio(name: String, provider: String): Result<PortfolioResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.createPortfolio(CreatePortfolioRequest(name, provider))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    portfolioDao.insert(body.toEntity())
                    Result.success(body)
                } else {
                    Result.failure(Exception("Failed to create portfolio: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to create portfolio")
                Result.failure(e)
            }
        }
    }

    override suspend fun deletePortfolio(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.deletePortfolio(id)
                if (response.isSuccessful) {
                    portfolioDao.deleteById(id)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete portfolio: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete portfolio")
                Result.failure(e)
            }
        }
    }

    override suspend fun getPortfolioSummary(): Result<PortfolioSummaryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.getPortfolioSummary()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to get portfolio summary: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to get portfolio summary")
                Result.failure(e)
            }
        }
    }

    override fun getHoldings(portfolioId: String): Flow<List<PortfolioHolding>> {
        return holdingDao.getByPortfolioId(portfolioId).onStart {
            refreshHoldings(portfolioId)
        }
    }

    override suspend fun addHolding(
        portfolioId: String,
        ticker: String,
        quantity: Double,
        avgCost: Double
    ): Result<HoldingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.addHolding(
                    portfolioId,
                    AddHoldingRequest(ticker, quantity, avgCost)
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    holdingDao.insert(body.toEntity(portfolioId))
                    Result.success(body)
                } else {
                    Result.failure(Exception("Failed to add holding: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to add holding")
                Result.failure(e)
            }
        }
    }

    override suspend fun updateHolding(
        portfolioId: String,
        holdingId: String,
        quantity: Double,
        avgCost: Double
    ): Result<HoldingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.updateHolding(
                    portfolioId,
                    holdingId,
                    UpdateHoldingRequest(quantity, avgCost)
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    holdingDao.insert(body.toEntity(portfolioId))
                    Result.success(body)
                } else {
                    Result.failure(Exception("Failed to update holding: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update holding")
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteHolding(portfolioId: String, holdingId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.deleteHolding(portfolioId, holdingId)
                if (response.isSuccessful) {
                    holdingDao.deleteById(holdingId)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete holding: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete holding")
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshPortfolios() {
        withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.listPortfolios()
                if (response.isSuccessful) {
                    val portfolios = response.body()?.map { it.toEntity() } ?: emptyList()
                    portfolioDao.insertAll(portfolios)
                    hasFetchedPortfolios = true
                    Timber.d("Fetched ${portfolios.size} portfolios from API")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh portfolios from API")
            }
        }
    }

    override suspend fun refreshHoldings(portfolioId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.listHoldings(portfolioId)
                if (response.isSuccessful) {
                    val holdings = response.body()?.map { it.toEntity(portfolioId) } ?: emptyList()
                    holdingDao.deleteByPortfolioId(portfolioId)
                    holdingDao.insertAll(holdings)
                    Timber.d("Fetched ${holdings.size} holdings for portfolio $portfolioId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh holdings from API")
            }
        }
    }
}

private fun PortfolioResponse.toEntity(): Portfolio {
    return Portfolio(
        id = id,
        userId = userId,
        provider = provider,
        accountId = accountId,
        name = name,
        status = status,
        createdAt = try { java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).parse(createdAt)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() },
        updatedAt = try { java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).parse(updatedAt)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
    )
}

private fun HoldingResponse.toEntity(portfolioId: String): PortfolioHolding {
    return PortfolioHolding(
        id = id,
        portfolioId = portfolioId,
        ticker = ticker,
        quantity = quantity,
        avgCost = avgCost,
        currentPrice = currentPrice,
        currentValue = currentValue,
        gainLoss = gainLoss,
        gainLossPercent = gainLossPercent
    )
}
