package com.vettr.android.core.util

import android.util.Log
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of ObservabilityService for development and testing.
 * Logs metrics to Logcat for debugging. In production, replace with Firebase Performance Monitoring.
 *
 * Alert Thresholds:
 * - App startup >2000ms → P1 alert
 * - Screen load >1000ms → P2 alert
 * - API response >500ms → P2 alert
 * - Crash-free rate <99% → P0 alert
 * - Memory usage >80% → P2 alert
 */
@Singleton
class MockObservabilityService @Inject constructor() : ObservabilityService {

    private val traces = mutableMapOf<String, Long>()

    companion object {
        private const val TAG = "Observability"
        private const val APP_STARTUP_THRESHOLD_MS = 2000L
        private const val SCREEN_LOAD_THRESHOLD_MS = 1000L
        private const val API_RESPONSE_THRESHOLD_MS = 500L
        private const val MEMORY_USAGE_THRESHOLD_PERCENT = 0.8
    }

    override fun trackAppStartup(durationMs: Long) {
        Log.i(TAG, "App startup: ${durationMs}ms")

        if (durationMs > APP_STARTUP_THRESHOLD_MS) {
            trackSlaViolation(
                slaName = "app_startup_time",
                actualValue = durationMs.toDouble(),
                threshold = APP_STARTUP_THRESHOLD_MS.toDouble()
            )
        }
    }

    override fun trackScreenLoadTime(screenName: String, durationMs: Long) {
        Log.i(TAG, "Screen load - $screenName: ${durationMs}ms")

        if (durationMs > SCREEN_LOAD_THRESHOLD_MS) {
            Log.w(TAG, "⚠️ Slow screen load detected: $screenName took ${durationMs}ms (threshold: ${SCREEN_LOAD_THRESHOLD_MS}ms)")
        }
    }

    override fun trackApiCall(
        endpoint: String,
        durationMs: Long,
        success: Boolean,
        statusCode: Int?
    ) {
        val status = if (success) "✓" else "✗"
        Log.i(TAG, "API call $status - $endpoint: ${durationMs}ms (status: $statusCode)")

        if (durationMs > API_RESPONSE_THRESHOLD_MS) {
            Log.w(TAG, "⚠️ Slow API response: $endpoint took ${durationMs}ms (threshold: ${API_RESPONSE_THRESHOLD_MS}ms)")
        }
    }

    override fun trackMemoryUsage(usedMemoryMb: Long, maxMemoryMb: Long) {
        val usagePercent = usedMemoryMb.toDouble() / maxMemoryMb.toDouble()
        Log.d(TAG, "Memory usage: ${usedMemoryMb}MB / ${maxMemoryMb}MB (${String.format("%.1f", usagePercent * 100)}%)")

        if (usagePercent > MEMORY_USAGE_THRESHOLD_PERCENT) {
            Log.w(TAG, "⚠️ High memory usage detected: ${String.format("%.1f", usagePercent * 100)}%")
        }
    }

    override fun trackCustomMetric(metricName: String, value: Double, unit: String?) {
        val unitStr = unit?.let { " $it" } ?: ""
        Log.d(TAG, "Custom metric - $metricName: $value$unitStr")
    }

    override fun startTrace(traceName: String): String {
        val traceId = UUID.randomUUID().toString()
        traces[traceId] = System.currentTimeMillis()
        Log.d(TAG, "Starting trace: $traceName (id: $traceId)")
        return traceId
    }

    override fun stopTrace(traceId: String, attributes: Map<String, String>) {
        val startTime = traces.remove(traceId)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            val attrStr = if (attributes.isNotEmpty()) {
                " [${attributes.entries.joinToString(", ") { "${it.key}=${it.value}" }}]"
            } else ""
            Log.d(TAG, "Trace completed: ${duration}ms (id: $traceId)$attrStr")
        } else {
            Log.w(TAG, "Trace not found: $traceId")
        }
    }

    override fun trackSlaViolation(slaName: String, actualValue: Double, threshold: Double) {
        Log.e(TAG, "🚨 SLA VIOLATION - $slaName: actual=$actualValue, threshold=$threshold")
    }
}
