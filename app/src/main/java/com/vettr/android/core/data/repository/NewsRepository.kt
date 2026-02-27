package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.NewsArticleResponse

/**
 * Repository interface for fetching market news.
 */
interface NewsRepository {

    /**
     * Get news articles with optional source filter and limit.
     */
    suspend fun getNews(source: String? = null, limit: Int = 50): Result<List<NewsArticleResponse>>

    /**
     * Get only material news events.
     */
    suspend fun getMaterialNews(): Result<List<NewsArticleResponse>>
}
