package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Request payload for forgot password.
 */
data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

/**
 * Request payload for resetting password with token.
 */
data class ResetPasswordRequest(
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String
)

/**
 * Request payload for changing password (authenticated user).
 */
data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)
