package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response for a news article.
 */
data class NewsArticleResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("source") val source: String,
    @SerializedName("url") val url: String,
    @SerializedName("publishedAt") val publishedAt: String,
    @SerializedName("tickers") val tickers: List<String> = emptyList(),
    @SerializedName("isMaterial") val isMaterial: Boolean = false,
    @SerializedName("summary") val summary: String? = null
)
