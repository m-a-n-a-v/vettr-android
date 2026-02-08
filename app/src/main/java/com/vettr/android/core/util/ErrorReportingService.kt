package com.vettr.android.core.util

/**
 * Interface for error and crash reporting in the VETTR app.
 * Captures errors, logs, breadcrumbs, and device context for debugging.
 */
interface ErrorReportingService {

    /**
     * Log an error or exception with optional context.
     * @param throwable The exception/error to log
     * @param context Optional map of contextual data about the error
     */
    fun logError(throwable: Throwable, context: Map<String, Any> = emptyMap())

    /**
     * Log a message with a specified severity level.
     * @param message The message to log
     * @param level The severity level (e.g., "debug", "info", "warning", "error")
     */
    fun logMessage(message: String, level: String)

    /**
     * Add a breadcrumb to track user actions before an error occurs.
     * Breadcrumbs help understand the sequence of events leading to an error.
     * @param message Description of the user action or event
     */
    fun addBreadcrumb(message: String)

    /**
     * Set the user identifier for error tracking.
     * Should be an anonymized/hashed ID, not PII.
     * @param id The anonymized user ID
     */
    fun setUser(id: String)
}

/**
 * Severity levels for log messages.
 */
object ErrorLevel {
    const val DEBUG = "debug"
    const val INFO = "info"
    const val WARNING = "warning"
    const val ERROR = "error"
    const val FATAL = "fatal"
}
