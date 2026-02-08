package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Request payload for user authentication.
 * Contains credentials for login API call.
 */
data class LoginRequest(
    @SerializedName("id_token")
    val idToken: String,

    @SerializedName("provider")
    val provider: String = "google"
)
