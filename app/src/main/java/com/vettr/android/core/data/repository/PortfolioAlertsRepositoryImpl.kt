package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.PortfolioAlertDao
import com.vettr.android.core.data.remote.PortfolioAlertResponse
import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.model.PortfolioAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of PortfolioAlertsRepository using VettrApi + PortfolioAlertDao.
 */
class PortfolioAlertsRepositoryImpl @Inject constructor(
    private val vettrApi: VettrApi,
    private val alertDao: PortfolioAlertDao
) : PortfolioAlertsRepository {

    @Volatile
    private var hasFetched = false

    override fun getAlerts(): Flow<List<PortfolioAlert>> {
        return alertDao.getAll().onStart {
            if (!hasFetched) {
                refreshAlerts()
            }
        }
    }

    override fun getUnreadCount(): Flow<Int> {
        return alertDao.getUnreadCount()
    }

    override suspend fun markRead(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.markPortfolioAlertRead(id)
                if (response.isSuccessful) {
                    alertDao.markRead(id)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to mark alert read: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark alert read")
                // Still update locally for offline support
                alertDao.markRead(id)
                Result.failure(e)
            }
        }
    }

    override suspend fun markAllRead(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.markAllPortfolioAlertsRead()
                if (response.isSuccessful) {
                    alertDao.markAllRead()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to mark all alerts read: ${response.code()}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark all alerts read")
                alertDao.markAllRead()
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshAlerts() {
        withContext(Dispatchers.IO) {
            try {
                val response = vettrApi.getPortfolioAlerts()
                if (response.isSuccessful) {
                    val alerts = response.body()?.map { it.toEntity() } ?: emptyList()
                    alertDao.deleteAll()
                    alertDao.insertAll(alerts)
                    hasFetched = true
                    Timber.d("Fetched ${alerts.size} portfolio alerts from API")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh portfolio alerts")
            }
        }
    }
}

private fun PortfolioAlertResponse.toEntity(): PortfolioAlert {
    return PortfolioAlert(
        id = id,
        portfolioId = portfolioId,
        alertType = alertType,
        severity = severity,
        title = title,
        message = message,
        isRead = isRead,
        triggeredAt = try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .parse(triggeredAt)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) { System.currentTimeMillis() }
    )
}
