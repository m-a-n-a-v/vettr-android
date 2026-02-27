package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response for portfolio alert from API.
 */
data class PortfolioAlertResponse(
    @SerializedName("id") val id: String,
    @SerializedName("portfolioId") val portfolioId: String,
    @SerializedName("alertType") val alertType: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("triggeredAt") val triggeredAt: String
)

/**
 * Response for unread alert count.
 */
data class UnreadCountResponse(
    @SerializedName("count") val count: Int
)

/**
 * Response for portfolio insight from API.
 */
data class PortfolioInsightResponse(
    @SerializedName("id") val id: String,
    @SerializedName("portfolioId") val portfolioId: String,
    @SerializedName("insightType") val insightType: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("title") val title: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("isDismissed") val isDismissed: Boolean,
    @SerializedName("createdAt") val createdAt: String
)
