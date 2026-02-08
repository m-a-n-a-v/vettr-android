package com.vettr.android.core.util

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.vettr.android.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Memory monitoring service for debugging and profiling.
 * Tracks heap usage and provides memory statistics.
 */
@Singleton
class MemoryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val runtime = Runtime.getRuntime()

    companion object {
        private const val TAG = "MemoryMonitor"
        private const val MB = 1024 * 1024

        // Memory thresholds (in MB)
        private const val TARGET_TYPICAL_MB = 150
        private const val TARGET_PEAK_MB = 300
    }

    /**
     * Get current memory statistics.
     */
    data class MemoryStats(
        val usedMemoryMB: Double,
        val maxMemoryMB: Double,
        val freeMemoryMB: Double,
        val totalMemoryMB: Double,
        val usedPercentage: Double,
        val isHealthy: Boolean
    )

    /**
     * Get current memory statistics.
     * @return MemoryStats object containing current memory usage
     */
    fun getMemoryStats(): MemoryStats {
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory

        val usedMemoryMB = usedMemory / MB.toDouble()
        val maxMemoryMB = maxMemory / MB.toDouble()
        val freeMemoryMB = freeMemory / MB.toDouble()
        val totalMemoryMB = totalMemory / MB.toDouble()
        val usedPercentage = (usedMemory / maxMemory.toDouble()) * 100

        // Consider memory healthy if under peak threshold
        val isHealthy = usedMemoryMB < TARGET_PEAK_MB

        return MemoryStats(
            usedMemoryMB = usedMemoryMB,
            maxMemoryMB = maxMemoryMB,
            freeMemoryMB = freeMemoryMB,
            totalMemoryMB = totalMemoryMB,
            usedPercentage = usedPercentage,
            isHealthy = isHealthy
        )
    }

    /**
     * Log current memory statistics.
     * Only logs in debug builds.
     */
    fun logMemoryStats() {
        if (!BuildConfig.DEBUG) return

        val stats = getMemoryStats()
        Log.d(TAG, buildString {
            append("Memory Stats: ")
            append("Used: %.2f MB / %.2f MB (%.1f%%), ".format(
                stats.usedMemoryMB,
                stats.maxMemoryMB,
                stats.usedPercentage
            ))
            append("Free: %.2f MB, ".format(stats.freeMemoryMB))
            append("Total: %.2f MB, ".format(stats.totalMemoryMB))
            append("Health: ${if (stats.isHealthy) "OK" else "WARNING"}")

            if (!stats.isHealthy) {
                append(" - Exceeds target of $TARGET_PEAK_MB MB")
            }
        })
    }

    /**
     * Get available device memory in MB.
     */
    fun getAvailableDeviceMemoryMB(): Double {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / MB.toDouble()
    }

    /**
     * Get total device memory in MB.
     */
    fun getTotalDeviceMemoryMB(): Double {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem / MB.toDouble()
    }

    /**
     * Check if device is in low memory condition.
     */
    fun isLowMemory(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }

    /**
     * Request garbage collection (for debugging only).
     * Note: This is a hint to the runtime, not a guarantee.
     */
    fun requestGC() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Requesting garbage collection")
            System.gc()
        }
    }
}
