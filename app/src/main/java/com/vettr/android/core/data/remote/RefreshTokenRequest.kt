package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Request payload for token refresh endpoint.
 * Contains the refresh token to exchange for a new access token.
 */
data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)
