package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.SamplePortfolioResponse
import com.vettr.android.core.data.remote.VettrApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of SamplePortfolioRepository using VettrApi with 30-minute cache.
 */
class SamplePortfolioRepositoryImpl @Inject constructor(
    private val vettrApi: VettrApi
) : SamplePortfolioRepository {

    private var cachedPortfolios: CacheEntry? = null

    override suspend fun getSamplePortfolios(): Result<List<SamplePortfolioResponse>> {
        return withContext(Dispatchers.IO) {
            val cached = cachedPortfolios
            if (cached != null && !cached.isExpired()) {
                return@withContext Result.success(cached.data)
            }

            try {
                val response = vettrApi.getSamplePortfolios()
                if (response.isSuccessful) {
                    val portfolios = response.body() ?: emptyList()
                    cachedPortfolios = CacheEntry(portfolios)
                    Result.success(portfolios)
                } else {
                    cached?.let { Result.success(it.data) }
                        ?: Result.failure(Exception("Failed to fetch sample portfolios: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch sample portfolios")
                cached?.let { Result.success(it.data) }
                    ?: Result.failure(e)
            }
        }
    }

    private data class CacheEntry(
        val data: List<SamplePortfolioResponse>,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS
        }
    }

    companion object {
        private const val CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    }
}
