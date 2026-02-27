package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.FundamentalsResponse
import com.vettr.android.core.data.remote.VettrApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of FundamentalsRepository using VettrApi with 1-hour in-memory cache.
 */
class FundamentalsRepositoryImpl @Inject constructor(
    private val vettrApi: VettrApi
) : FundamentalsRepository {

    private val cache = mutableMapOf<String, CacheEntry>()

    override suspend fun getFundamentals(ticker: String): Result<FundamentalsResponse> {
        return withContext(Dispatchers.IO) {
            val cached = cache[ticker]
            if (cached != null && !cached.isExpired()) {
                return@withContext Result.success(cached.data)
            }

            try {
                val response = vettrApi.getFundamentals(ticker)
                if (response.isSuccessful) {
                    val data = response.body()!!
                    cache[ticker] = CacheEntry(data)
                    Result.success(data)
                } else {
                    cached?.let { Result.success(it.data) }
                        ?: Result.failure(Exception("Failed to fetch fundamentals: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch fundamentals for $ticker")
                cached?.let { Result.success(it.data) }
                    ?: Result.failure(e)
            }
        }
    }

    private data class CacheEntry(
        val data: FundamentalsResponse,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS
        }
    }

    companion object {
        private const val CACHE_DURATION_MS = 60 * 60 * 1000L // 1 hour
    }
}
