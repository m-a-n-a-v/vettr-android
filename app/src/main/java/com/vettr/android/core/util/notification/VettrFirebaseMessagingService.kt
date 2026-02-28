package com.vettr.android.core.util.notification

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service for handling push notifications.
 *
 * Responsibilities:
 * - Persists new FCM tokens to SharedPreferences for later registration with backend
 * - Receives remote messages and delegates to NotificationService for display
 * - Extracts deep link data from message payload for notification tap navigation
 *
 * Token registration with the backend happens in AuthViewModel after successful login,
 * reading the pending token from SharedPreferences.
 */
@AndroidEntryPoint
class VettrFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationService: NotificationService

    companion object {
        private const val PREFS_NAME = "vettr_prefs"
        private const val KEY_PENDING_FCM_TOKEN = "pending_fcm_token"

        /**
         * Retrieve the pending FCM token from SharedPreferences.
         * Called by AuthViewModel after successful login to register with backend.
         *
         * @param context Application context
         * @return The pending FCM token, or null if none is stored
         */
        fun getPendingFcmToken(context: Context): String? {
            return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_PENDING_FCM_TOKEN, null)
        }

        /**
         * Clear the pending FCM token after successful registration with backend.
         *
         * @param context Application context
         */
        fun clearPendingFcmToken(context: Context) {
            context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(KEY_PENDING_FCM_TOKEN)
                .apply()
        }
    }

    /**
     * Called when a new FCM token is generated.
     * Stores the token in SharedPreferences so it can be registered with the backend
     * on next authenticated app open.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_PENDING_FCM_TOKEN, token)
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Called when a push notification is received while the app is in the foreground,
     * or when a data-only message is received.
     *
     * Extracts title, body, and deep link from the message payload and delegates
     * to NotificationService for local notification display.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: return
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val deepLink = message.data["deep_link"]

        // Extract ticker from deep_link for notification grouping and deep link
        // Expected format: /stocks/TICKER or vettr://stock/TICKER
        val ticker = deepLink?.let { link ->
            val regex = Regex("/stocks?/([A-Z][A-Z0-9.]+)")
            regex.find(link)?.groupValues?.getOrNull(1)
        }

        notificationService.sendAlertNotification(
            stockTicker = ticker ?: "VETTR",
            title = title,
            message = body
        )
    }
}
