package com.vettr.android

import android.app.Application
import android.content.ComponentCallbacks2
import androidx.hilt.work.HiltWorkerFactory
import timber.log.Timber
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.vettr.android.core.data.local.SeedDataService
import com.vettr.android.core.util.AppStartupTracker
import com.vettr.android.core.util.MemoryMonitor
import com.vettr.android.core.util.ObservabilityService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VettrApp : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject
    lateinit var seedDataService: SeedDataService

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var memoryMonitor: MemoryMonitor

    @Inject
    lateinit var observabilityService: ObservabilityService

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for debug builds only
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Track app startup time
        val startupDuration = AppStartupTracker.getElapsedTime()
        observabilityService.trackAppStartup(startupDuration)

        // Log initial memory state (debug builds only)
        memoryMonitor.logMemoryStats()

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

    override fun newImageLoader(context: android.content.Context): ImageLoader {
        return imageLoader
    }

    /**
     * Handle system memory trim requests.
     * Clears Coil image cache when system requests memory to be freed.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (BuildConfig.DEBUG) {
            Timber.d("onTrimMemory called with level: $level")
            memoryMonitor.logMemoryStats()
        }

        when (level) {
            // Running low on memory - moderate actions
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // Clear memory cache only
                imageLoader.memoryCache?.clear()
                if (BuildConfig.DEBUG) {
                    Timber.d("Cleared Coil memory cache (running low)")
                }
            }

            // Critical memory - aggressive actions
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // Clear both memory and disk caches
                imageLoader.memoryCache?.clear()
                imageLoader.diskCache?.clear()
                if (BuildConfig.DEBUG) {
                    Timber.d("Cleared Coil memory and disk cache (critical)")
                }
            }

            // UI hidden - moderate cleanup
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // Clear memory cache when UI is hidden
                imageLoader.memoryCache?.clear()
                if (BuildConfig.DEBUG) {
                    Timber.d("Cleared Coil memory cache (UI hidden)")
                }
            }

            // Background - trim based on severity
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                imageLoader.memoryCache?.clear()
                if (BuildConfig.DEBUG) {
                    Timber.d("Cleared Coil memory cache (background)")
                }
            }
        }

        // Log memory state after cleanup (debug builds only)
        if (BuildConfig.DEBUG) {
            memoryMonitor.logMemoryStats()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()

        if (BuildConfig.DEBUG) {
            Timber.w("onLowMemory called - clearing all caches")
            memoryMonitor.logMemoryStats()
        }

        // Clear all caches when device is critically low on memory
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()

        if (BuildConfig.DEBUG) {
            memoryMonitor.logMemoryStats()
        }
    }
}
