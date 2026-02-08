package com.vettr.android.core.util.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vettr.android.MainActivity
import com.vettr.android.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationService handles all local push notifications for alert rules.
 * Creates notification channel, manages permissions, and sends notifications with deep links.
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "vettr_alerts"
        const val CHANNEL_NAME = "VETTR Alerts"
        private const val NOTIFICATION_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
        private const val DUPLICATE_PREVENTION_WINDOW_MS = 60 * 60 * 1000L // 1 hour
    }

    // Track last notification time per stock ticker to prevent duplicates
    private val lastNotificationTimes = mutableMapOf<String, Long>()

    init {
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for VETTR alerts.
     * Required for Android O (API 26) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NOTIFICATION_IMPORTANCE
            ).apply {
                description = "Notifications for stock price alerts, filings, and management changes"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if the app has notification permission.
     * For Android 13+ (API 33), POST_NOTIFICATIONS permission is required.
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-Android 13, notifications are enabled by default
            true
        }
    }

    /**
     * Sends a notification for an alert trigger.
     * Includes deep link to StockDetailScreen and prevents duplicate notifications within 1 hour.
     *
     * @param stockTicker The stock ticker symbol (e.g., "AAPL")
     * @param title The notification title
     * @param message The notification message
     * @param alertId Optional alert rule ID for deep linking
     */
    fun sendAlertNotification(
        stockTicker: String,
        title: String,
        message: String,
        alertId: String? = null
    ) {
        // Check notification permission
        if (!hasNotificationPermission()) {
            return
        }

        // Prevent duplicate notifications within 1 hour window
        val currentTime = System.currentTimeMillis()
        val lastTime = lastNotificationTimes[stockTicker]
        if (lastTime != null && (currentTime - lastTime) < DUPLICATE_PREVENTION_WINDOW_MS) {
            return // Skip duplicate notification
        }

        // Create deep link URI to StockDetailScreen using vettr:// scheme
        val deepLinkUri = android.net.Uri.parse("vettr://stock/$stockTicker")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            stockTicker.hashCode(), // Use ticker hashCode as unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Using default icon, replace with custom alert icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Send notification
        try {
            NotificationManagerCompat.from(context).notify(
                stockTicker.hashCode(), // Use ticker hashCode as unique notification ID
                notification
            )

            // Update last notification time
            lastNotificationTimes[stockTicker] = currentTime
        } catch (e: SecurityException) {
            // Handle case where permission was revoked
            e.printStackTrace()
        }
    }

    /**
     * Sends an instant alert notification (for immediate triggers).
     */
    fun sendInstantAlert(stockTicker: String, title: String, message: String) {
        sendAlertNotification(stockTicker, title, message)
    }

    /**
     * Cancels all pending notifications.
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
        lastNotificationTimes.clear()
    }

    /**
     * Cancels notification for a specific stock ticker.
     */
    fun cancelNotification(stockTicker: String) {
        NotificationManagerCompat.from(context).cancel(stockTicker.hashCode())
        lastNotificationTimes.remove(stockTicker)
    }
}
