package com.vettr.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.lifecycleScope
import androidx.work.Configuration
import com.vettr.android.core.data.local.SeedDataService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VettrApp : Application(), Configuration.Provider {

    @Inject
    lateinit var seedDataService: SeedDataService

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Seed data on first launch
        applicationScope.launch {
            if (!seedDataService.isSeedComplete()) {
                seedDataService.seedAllData()
                seedDataService.markSeedComplete()
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
