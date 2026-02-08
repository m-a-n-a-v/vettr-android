package com.vettr.android.core.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of ErrorReportingService that logs errors to Logcat.
 * Used in debug builds for error tracking verification without external dependencies.
 * Features:
 * - Breadcrumb tracking (last 10 user actions)
 * - Device info capture (model, OS, screen, memory)
 * - Formatted error logging
 */
@Singleton
class MockErrorReportingService @Inject constructor(
    @ApplicationContext private val context: Context
) : ErrorReportingService {

    companion object {
        private const val TAG = "ErrorReporting"
        private const val MAX_BREADCRUMBS = 10
    }

    private val breadcrumbs = mutableListOf<Breadcrumb>()
    private var currentUserId: String? = null
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    override fun logError(throwable: Throwable, context: Map<String, Any>) {
        Log.e(TAG, "╔════════════════════════════════════════════════════════════════")
        Log.e(TAG, "║ 🚨 ERROR REPORT")
        Log.e(TAG, "╠════════════════════════════════════════════════════════════════")
        Log.e(TAG, "║ Exception: ${throwable.javaClass.simpleName}")
        Log.e(TAG, "║ Message: ${throwable.message ?: "No message"}")
        Log.e(TAG, "║ User: ${currentUserId ?: "Not set"}")
        Log.e(TAG, "╠════════════════════════════════════════════════════════════════")

        // Log context
        if (context.isNotEmpty()) {
            Log.e(TAG, "║ Context:")
            context.forEach { (key, value) ->
                Log.e(TAG, "║   $key: $value")
            }
            Log.e(TAG, "╠════════════════════════════════════════════════════════════════")
        }

        // Log device info
        Log.e(TAG, "║ Device Info:")
        val deviceInfo = getDeviceInfo()
        deviceInfo.forEach { (key, value) ->
            Log.e(TAG, "║   $key: $value")
        }
        Log.e(TAG, "╠════════════════════════════════════════════════════════════════")

        // Log breadcrumbs
        if (breadcrumbs.isNotEmpty()) {
            Log.e(TAG, "║ Breadcrumbs (last ${breadcrumbs.size} actions):")
            breadcrumbs.forEach { breadcrumb ->
                Log.e(TAG, "║   [${breadcrumb.timestamp}] ${breadcrumb.message}")
            }
            Log.e(TAG, "╠════════════════════════════════════════════════════════════════")
        }

        // Log stack trace
        Log.e(TAG, "║ Stack Trace:")
        throwable.stackTrace.take(10).forEach { element ->
            Log.e(TAG, "║   at $element")
        }

        Log.e(TAG, "╚════════════════════════════════════════════════════════════════")
    }

    override fun logMessage(message: String, level: String) {
        val icon = when (level) {
            ErrorLevel.DEBUG -> "🐛"
            ErrorLevel.INFO -> "ℹ️"
            ErrorLevel.WARNING -> "⚠️"
            ErrorLevel.ERROR -> "❌"
            ErrorLevel.FATAL -> "💀"
            else -> "📝"
        }

        when (level) {
            ErrorLevel.DEBUG -> Log.d(TAG, "$icon $message")
            ErrorLevel.INFO -> Log.i(TAG, "$icon $message")
            ErrorLevel.WARNING -> Log.w(TAG, "$icon $message")
            ErrorLevel.ERROR, ErrorLevel.FATAL -> Log.e(TAG, "$icon $message")
            else -> Log.v(TAG, "$icon $message")
        }
    }

    override fun addBreadcrumb(message: String) {
        val timestamp = dateFormat.format(Date())
        val breadcrumb = Breadcrumb(message, timestamp)

        synchronized(breadcrumbs) {
            breadcrumbs.add(breadcrumb)
            // Keep only last 10 breadcrumbs
            if (breadcrumbs.size > MAX_BREADCRUMBS) {
                breadcrumbs.removeAt(0)
            }
        }

        Log.v(TAG, "🍞 Breadcrumb: [$timestamp] $message")
    }

    override fun setUser(id: String) {
        currentUserId = id
        Log.d(TAG, "👤 User ID Set for Error Reporting: $id")
    }

    /**
     * Capture device information for error context.
     */
    private fun getDeviceInfo(): Map<String, String> {
        val deviceInfo = mutableMapOf<String, String>()

        // Device model
        deviceInfo["Device"] = "${Build.MANUFACTURER} ${Build.MODEL}"
        deviceInfo["OS Version"] = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        // Screen size
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            val density = displayMetrics.density
            deviceInfo["Screen Size"] = "${width}x$height (${density}x)"
        } catch (e: Exception) {
            deviceInfo["Screen Size"] = "Unknown"
        }

        // Memory usage
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val availableMB = memoryInfo.availMem / (1024 * 1024)
            val totalMB = memoryInfo.totalMem / (1024 * 1024)
            val usedMB = totalMB - availableMB

            deviceInfo["Memory Usage"] = "${usedMB}MB / ${totalMB}MB (${(usedMB * 100 / totalMB)}% used)"
        } catch (e: Exception) {
            deviceInfo["Memory Usage"] = "Unknown"
        }

        return deviceInfo
    }

    /**
     * Data class representing a breadcrumb entry.
     */
    private data class Breadcrumb(
        val message: String,
        val timestamp: String
    )
}
