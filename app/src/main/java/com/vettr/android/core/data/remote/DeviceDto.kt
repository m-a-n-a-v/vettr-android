package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Request payload for registering a device for push notifications.
 * Sent after successful authentication to associate FCM token with the user.
 */
data class RegisterDeviceRequest(
    @SerializedName("platform")
    val platform: String = "android",

    @SerializedName("token")
    val token: String
)

/**
 * Request payload for unregistering a device from push notifications.
 * Sent on sign-out to stop receiving notifications for this device.
 */
data class UnregisterDeviceRequest(
    @SerializedName("token")
    val token: String
)
