package com.vettr.android.core.data.repository

import com.vettr.android.core.data.remote.AiAgentQuestionResponse
import com.vettr.android.core.data.remote.AiAgentRequest
import com.vettr.android.core.data.remote.AiAgentResponse
import com.vettr.android.core.data.remote.AiAgentUsageResponse
import com.vettr.android.core.data.remote.VettrApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of AiAgentRepository using VettrApi.
 */
class AiAgentRepositoryImpl @Inject constructor(
    private val vettrApi: VettrApi
) : AiAgentRepository {

    override suspend fun askQuestion(
        ticker: String,
        questionId: String?,
        customQuestion: String?
    ): Result<AiAgentResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.askAiAgent(
                    AiAgentRequest(
                        ticker = ticker,
                        questionId = questionId,
                        customQuestion = customQuestion
                    )
                )
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("AI agent error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to ask AI agent")
                Result.failure(e)
            }
        }
    }

    override suspend fun getQuestions(): Result<List<AiAgentQuestionResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.getAiAgentQuestions()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get AI questions: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to get AI questions")
                Result.failure(e)
            }
        }
    }

    override suspend fun getUsage(): Result<AiAgentUsageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.getAiAgentUsage()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to get AI usage: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to get AI usage")
                Result.failure(e)
            }
        }
    }
}
