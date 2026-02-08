package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response payload from authentication endpoint.
 * Contains access token and user information after successful login.
 */
data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String?,

    @SerializedName("token_type")
    val tokenType: String = "Bearer",

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("user")
    val user: UserDto
)

/**
 * User information included in authentication response.
 */
data class UserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("avatar_url")
    val avatarUrl: String?,

    @SerializedName("tier")
    val tier: String,

    @SerializedName("created_at")
    val createdAt: Long
)
