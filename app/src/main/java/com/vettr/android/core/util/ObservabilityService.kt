package com.vettr.android.core.util

/**
 * Interface for observability and performance monitoring in the VETTR app.
 * Tracks app health metrics including startup time, screen load times, and API response times.
 *
 * SLAs:
 * - Crash-free rate: >99%
 * - App startup time: <2s
 * - API response time: <500ms
 */
interface ObservabilityService {
    /**
     * Track app startup time from process start to first frame.
     * @param durationMs The startup duration in milliseconds
     */
    fun trackAppStartup(durationMs: Long)

    /**
     * Track screen load time from navigation to first render.
     * @param screenName The name of the screen being loaded
     * @param durationMs The load duration in milliseconds
     */
    fun trackScreenLoadTime(screenName: String, durationMs: Long)

    /**
     * Track API call performance.
     * @param endpoint The API endpoint being called
     * @param durationMs The call duration in milliseconds
     * @param success Whether the call was successful
     * @param statusCode Optional HTTP status code
     */
    fun trackApiCall(
        endpoint: String,
        durationMs: Long,
        success: Boolean,
        statusCode: Int? = null
    )

    /**
     * Track memory usage metrics.
     * @param usedMemoryMb Used memory in megabytes
     * @param maxMemoryMb Maximum available memory in megabytes
     */
    fun trackMemoryUsage(usedMemoryMb: Long, maxMemoryMb: Long)

    /**
     * Track custom performance metric.
     * @param metricName The name of the metric
     * @param value The metric value
     * @param unit Optional unit of measurement
     */
    fun trackCustomMetric(metricName: String, value: Double, unit: String? = null)

    /**
     * Start a performance trace for a specific operation.
     * @param traceName The name of the trace
     * @return A trace ID to be used when stopping the trace
     */
    fun startTrace(traceName: String): String

    /**
     * Stop a performance trace.
     * @param traceId The trace ID returned from startTrace
     * @param attributes Optional attributes to attach to the trace
     */
    fun stopTrace(traceId: String, attributes: Map<String, String> = emptyMap())

    /**
     * Track SLA violations for alerting.
     * @param slaName The name of the SLA being violated
     * @param actualValue The actual measured value
     * @param threshold The threshold that was violated
     */
    fun trackSlaViolation(slaName: String, actualValue: Double, threshold: Double)
}
