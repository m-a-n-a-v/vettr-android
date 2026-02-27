package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Request payload for AI agent question.
 */
data class AiAgentRequest(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("questionId") val questionId: String? = null,
    @SerializedName("customQuestion") val customQuestion: String? = null
)

/**
 * Response from AI agent with answer, data points, and follow-up suggestions.
 */
data class AiAgentResponse(
    @SerializedName("answer") val answer: String,
    @SerializedName("dataPoints") val dataPoints: List<AiDataPoint> = emptyList(),
    @SerializedName("followUpQuestions") val followUpQuestions: List<String> = emptyList()
)

/**
 * A key-value data point returned by the AI agent.
 */
data class AiDataPoint(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)

/**
 * Suggested question from the AI agent.
 */
data class AiAgentQuestionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("question") val question: String,
    @SerializedName("category") val category: String
)

/**
 * AI agent usage tracking response.
 */
data class AiAgentUsageResponse(
    @SerializedName("questionsUsed") val questionsUsed: Int,
    @SerializedName("questionsLimit") val questionsLimit: Int,
    @SerializedName("resetAt") val resetAt: String
)
