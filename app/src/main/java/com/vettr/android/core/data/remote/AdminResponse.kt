package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Wrapper for admin API paginated responses.
 */
data class AdminListResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: AdminListData<T>
)

data class AdminListData<T>(
    @SerializedName("items")
    val items: List<T>,

    @SerializedName("pagination")
    val pagination: PaginationDto
)

data class PaginationDto(
    @SerializedName("total")
    val total: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("offset")
    val offset: Int,

    @SerializedName("has_more")
    val hasMore: Boolean
)
