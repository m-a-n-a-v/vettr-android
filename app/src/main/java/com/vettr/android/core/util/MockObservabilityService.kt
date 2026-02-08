package com.vettr.android.core.util

import timber.log.Timber
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
        Timber.tag(TAG).i("App startup: ${durationMs}ms")

        if (durationMs > APP_STARTUP_THRESHOLD_MS) {
            trackSlaViolation(
                slaName = "app_startup_time",
                actualValue = durationMs.toDouble(),
                threshold = APP_STARTUP_THRESHOLD_MS.toDouble()
            )
        }
    }

    override fun trackScreenLoadTime(screenName: String, durationMs: Long) {
        Timber.tag(TAG).i("Screen load - $screenName: ${durationMs}ms")

        if (durationMs > SCREEN_LOAD_THRESHOLD_MS) {
            Timber.tag(TAG).w("⚠️ Slow screen load detected: $screenName took ${durationMs}ms (threshold: ${SCREEN_LOAD_THRESHOLD_MS}ms)")
        }
    }

    override fun trackApiCall(
        endpoint: String,
        durationMs: Long,
        success: Boolean,
        statusCode: Int?
    ) {
        val status = if (success) "✓" else "✗"
        Timber.tag(TAG).i("API call $status - $endpoint: ${durationMs}ms (status: $statusCode)")

        if (durationMs > API_RESPONSE_THRESHOLD_MS) {
            Timber.tag(TAG).w("⚠️ Slow API response: $endpoint took ${durationMs}ms (threshold: ${API_RESPONSE_THRESHOLD_MS}ms)")
        }
    }

    override fun trackMemoryUsage(usedMemoryMb: Long, maxMemoryMb: Long) {
        val usagePercent = usedMemoryMb.toDouble() / maxMemoryMb.toDouble()
        Timber.tag(TAG).d("Memory usage: ${usedMemoryMb}MB / ${maxMemoryMb}MB (${String.format("%.1f", usagePercent * 100)}%)")

        if (usagePercent > MEMORY_USAGE_THRESHOLD_PERCENT) {
            Timber.tag(TAG).w("⚠️ High memory usage detected: ${String.format("%.1f", usagePercent * 100)}%")
        }
    }

    override fun trackCustomMetric(metricName: String, value: Double, unit: String?) {
        val unitStr = unit?.let { " $it" } ?: ""
        Timber.tag(TAG).d("Custom metric - $metricName: $value$unitStr")
    }

    override fun startTrace(traceName: String): String {
        val traceId = UUID.randomUUID().toString()
        traces[traceId] = System.currentTimeMillis()
        Timber.tag(TAG).d("Starting trace: $traceName (id: $traceId)")
        return traceId
    }

    override fun stopTrace(traceId: String, attributes: Map<String, String>) {
        val startTime = traces.remove(traceId)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            val attrStr = if (attributes.isNotEmpty()) {
                " [${attributes.entries.joinToString(", ") { "${it.key}=${it.value}" }}]"
            } else ""
            Timber.tag(TAG).d("Trace completed: ${duration}ms (id: $traceId)$attrStr")
        } else {
            Timber.tag(TAG).w("Trace not found: $traceId")
        }
    }

    override fun trackSlaViolation(slaName: String, actualValue: Double, threshold: Double) {
        Timber.tag(TAG).e("🚨 SLA VIOLATION - $slaName: actual=$actualValue, threshold=$threshold")
    }
}
