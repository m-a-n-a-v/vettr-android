package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Filing entity from API.
 * Maps JSON response fields to Kotlin properties using Gson serialization.
 */
data class FilingDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("stock_id")
    val stockId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("date")
    val date: Long,

    @SerializedName("summary")
    val summary: String,

    @SerializedName("is_read")
    val isRead: Boolean = false,

    @SerializedName("is_material")
    val isMaterial: Boolean = false
)
