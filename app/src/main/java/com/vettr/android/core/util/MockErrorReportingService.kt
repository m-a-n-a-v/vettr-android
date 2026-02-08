package com.vettr.android.core.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import timber.log.Timber
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
        Timber.tag(TAG).e("╔════════════════════════════════════════════════════════════════")
        Timber.tag(TAG).e("║ 🚨 ERROR REPORT")
        Timber.tag(TAG).e("╠════════════════════════════════════════════════════════════════")
        Timber.tag(TAG).e("║ Exception: ${throwable.javaClass.simpleName}")
        Timber.tag(TAG).e("║ Message: ${throwable.message ?: "No message"}")
        Timber.tag(TAG).e("║ User: ${currentUserId ?: "Not set"}")
        Timber.tag(TAG).e("╠════════════════════════════════════════════════════════════════")

        // Log context
        if (context.isNotEmpty()) {
            Timber.tag(TAG).e("║ Context:")
            context.forEach { (key, value) ->
                Timber.tag(TAG).e("║   $key: $value")
            }
            Timber.tag(TAG).e("╠════════════════════════════════════════════════════════════════")
        }

        // Log device info
        Timber.tag(TAG).e("║ Device Info:")
        val deviceInfo = getDeviceInfo()
        deviceInfo.forEach { (key, value) ->
            Timber.tag(TAG).e("║   $key: $value")
        }
        Timber.tag(TAG).e("╠════════════════════════════════════════════════════════════════")

        // Log breadcrumbs
        if (breadcrumbs.isNotEmpty()) {
            Timber.tag(TAG).e("║ Breadcrumbs (last ${breadcrumbs.size} actions):")
            breadcrumbs.forEach { breadcrumb ->
                Timber.tag(TAG).e("║   [${breadcrumb.timestamp}] ${breadcrumb.message}")
            }
            Timber.tag(TAG).e("╠════════════════════════════════════════════════════════════════")
        }

        // Log stack trace
        Timber.tag(TAG).e("║ Stack Trace:")
        throwable.stackTrace.take(10).forEach { element ->
            Timber.tag(TAG).e("║   at $element")
        }

        Timber.tag(TAG).e("╚════════════════════════════════════════════════════════════════")
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
            ErrorLevel.DEBUG -> Timber.tag(TAG).d("$icon $message")
            ErrorLevel.INFO -> Timber.tag(TAG).i("$icon $message")
            ErrorLevel.WARNING -> Timber.tag(TAG).w("$icon $message")
            ErrorLevel.ERROR, ErrorLevel.FATAL -> Timber.tag(TAG).e("$icon $message")
            else -> Timber.tag(TAG).v("$icon $message")
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

        Timber.tag(TAG).v("🍞 Breadcrumb: [$timestamp] $message")
    }

    override fun setUser(id: String) {
        currentUserId = id
        Timber.tag(TAG).d("👤 User ID Set for Error Reporting: $id")
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
