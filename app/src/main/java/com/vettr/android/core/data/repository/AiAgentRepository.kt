package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.AiAgentQuestionResponse
import com.vettr.android.core.data.remote.AiAgentResponse
import com.vettr.android.core.data.remote.AiAgentUsageResponse

/**
 * Repository interface for AI agent operations.
 */
interface AiAgentRepository {

    /**
     * Ask the AI agent a question about a specific stock.
     */
    suspend fun askQuestion(
        ticker: String,
        questionId: String? = null,
        customQuestion: String? = null
    ): Result<AiAgentResponse>

    /**
     * Get available AI questions/suggestions.
     */
    suspend fun getQuestions(): Result<List<AiAgentQuestionResponse>>

    /**
     * Get current AI usage stats for the user.
     */
    suspend fun getUsage(): Result<AiAgentUsageResponse>
}
