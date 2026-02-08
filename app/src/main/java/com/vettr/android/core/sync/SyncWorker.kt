package com.vettr.android.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vettr.android.core.data.local.SyncHistoryDao
import com.vettr.android.core.data.repository.AlertRuleRepository
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.SyncHistory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.UUID

/**
 * Background worker for syncing data using WorkManager.
 * Syncs filings, stock prices, and alerts with exponential backoff retry.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncHistoryDao: SyncHistoryDao,
    private val stockRepository: StockRepository,
    private val filingRepository: FilingRepository,
    private val alertRuleRepository: AlertRuleRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val syncId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        // Log sync start
        val syncHistory = SyncHistory(
            id = syncId,
            startedAt = startTime,
            status = "in_progress"
        )
        syncHistoryDao.insert(syncHistory)

        return try {
            var itemsSynced = 0

            // Sync stocks (prioritize watchlist stocks)
            try {
                // Mock sync: In real implementation, this would call API
                // For now, just simulate work
                delay(100)
                itemsSynced += 10 // Mock: synced 10 stocks
            } catch (e: Exception) {
                // Log error but continue with other syncs
                Timber.tag(TAG).e("Error syncing stocks", e)
            }

            // Sync filings
            try {
                // Mock sync: In real implementation, this would call API
                delay(100)
                itemsSynced += 5 // Mock: synced 5 filings
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error syncing filings", e)
            }

            // Sync alert rules
            try {
                // Mock sync: In real implementation, this would call API
                delay(100)
                itemsSynced += 3 // Mock: synced 3 alert rules
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error syncing alerts", e)
            }

            // Update sync history with success
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            syncHistoryDao.updateCompletion(
                id = syncId,
                completedAt = endTime,
                duration = duration,
                status = "success"
            )

            Timber.tag(TAG).d("Sync completed successfully. Items synced: $itemsSynced")
            Result.success()
        } catch (e: Exception) {
            // Update sync history with failure
            syncHistoryDao.updateErrors(
                id = syncId,
                errors = e.message ?: "Unknown error"
            )

            Timber.tag(TAG).e(e, "Sync failed")

            // Retry with exponential backoff
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        const val WORK_NAME = "vettr_sync_work"
    }
}
