package com.vettr.android.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vettr.android.core.data.local.UserDao
import com.vettr.android.core.model.VettrTier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages background sync scheduling based on user tier.
 * - Premium: 4 hours
 * - Pro: 12 hours
 * - Free: 24 hours
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Start periodic sync based on the user's subscription tier.
     */
    suspend fun startPeriodicSync() {
        // Get current user to determine tier
        val user = userDao.getAll().firstOrNull()?.firstOrNull()
        val tier = user?.tier?.let { tierString ->
            VettrTier.valueOf(tierString)
        } ?: VettrTier.FREE

        // Get sync interval based on tier
        val intervalHours = tier.syncIntervalHours.toLong()

        // Create constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        // Create periodic work request with exponential backoff
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = intervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 30,
                timeUnit = TimeUnit.SECONDS
            )
            .build()

        // Schedule the work (replace existing if already scheduled)
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncWorkRequest
        )

        Timber.tag(TAG).d("Periodic sync started for tier: $tier (interval: $intervalHours hours)")
    }

    /**
     * Stop periodic sync.
     */
    fun stopPeriodicSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        Timber.tag(TAG).d("Periodic sync stopped")
    }

    /**
     * Trigger an immediate one-time sync.
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueue(syncWorkRequest)
        Timber.tag(TAG).d("Immediate sync triggered")
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
