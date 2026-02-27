package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.PortfolioInsightDao
import com.vettr.android.core.data.remote.PortfolioInsightResponse
import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.model.PortfolioInsight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of PortfolioInsightsRepository using VettrApi + PortfolioInsightDao.
 */
class PortfolioInsightsRepositoryImpl @Inject constructor(
    private val vettrApi: VettrApi,
    private val insightDao: PortfolioInsightDao
) : PortfolioInsightsRepository {

    @Volatile
    private var hasFetched = false

    override fun getInsights(): Flow<List<PortfolioInsight>> {
        return insightDao.getActive().onStart {
            if (!hasFetched) {
                refreshInsights()
            }
        }
    }

    override suspend fun dismissInsight(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.dismissInsight(id)
                if (response.isSuccessful) {
                    insightDao.dismiss(id)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to dismiss insight: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to dismiss insight")
                insightDao.dismiss(id)
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshInsights() {
        withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.getPortfolioInsights()
                if (response.isSuccessful) {
                    val insights = response.body()?.map { it.toEntity() } ?: emptyList()
                    insightDao.deleteAll()
                    insightDao.insertAll(insights)
                    hasFetched = true
                    Timber.d("Fetched ${insights.size} portfolio insights from API")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh portfolio insights")
            }
        }
    }
}

private fun PortfolioInsightResponse.toEntity(): PortfolioInsight {
    return PortfolioInsight(
        id = id,
        portfolioId = portfolioId,
        insightType = insightType,
        severity = severity,
        title = title,
        summary = summary,
        isDismissed = isDismissed,
        createdAt = try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .parse(createdAt)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) { System.currentTimeMillis() }
    )
}
