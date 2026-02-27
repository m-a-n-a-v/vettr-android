package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.NewsArticleResponse
import com.vettr.android.core.data.remote.VettrApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of NewsRepository using VettrApi with 5-minute in-memory cache.
 */
class NewsRepositoryImpl @Inject constructor(
    private val vettrApi: VettrApi
) : NewsRepository {

    // In-memory cache with timestamp
    private var cachedNews: Map<String, CacheEntry<List<NewsArticleResponse>>> = emptyMap()

    override suspend fun getNews(source: String?, limit: Int): Result<List<NewsArticleResponse>> {
        return withContext(Dispatchers.IO) {
            val cacheKey = "news_${source ?: "all"}_$limit"
            val cached = cachedNews[cacheKey]

            if (cached != null && !cached.isExpired(CACHE_DURATION_MS)) {
                return@withContext Result.success(cached.data)
            }

            try {
                val response = vettrApi.getNews(source, limit)
                if (response.isSuccessful) {
                    val articles = response.body() ?: emptyList()
                    cachedNews = cachedNews + (cacheKey to CacheEntry(articles))
                    Result.success(articles)
                } else {
                    // Return cached data if available, even if expired
                    cached?.let { Result.success(it.data) }
                        ?: Result.failure(Exception("Failed to fetch news: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch news")
                cached?.let { Result.success(it.data) }
                    ?: Result.failure(e)
            }
        }
    }

    override suspend fun getMaterialNews(): Result<List<NewsArticleResponse>> {
        val result = getNews()
        return result.map { articles -> articles.filter { it.isMaterial } }
    }

    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(duration: Long): Boolean {
            return System.currentTimeMillis() - timestamp > duration
        }
    }

    companion object {
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }
}
