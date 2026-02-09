package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName
import com.vettr.android.core.model.Filing
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Data Transfer Object for Filing entity from admin API.
 * Maps JSON response fields to Kotlin properties using Gson serialization.
 * Uses camelCase field names matching the admin endpoint response format.
 */
data class FilingDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("stockId")
    val stockId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("summary")
    val summary: String,

    @SerializedName("isMaterial")
    val isMaterial: Boolean = false,

    @SerializedName("sourceUrl")
    val sourceUrl: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null
)

/**
 * Convert FilingDto from admin API to Filing domain model.
 * Parses ISO date string to Unix epoch milliseconds.
 */
fun FilingDto.toFiling(): Filing {
    val dateMillis = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.parse(date)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }

    return Filing(
        id = id,
        stockId = stockId,
        type = type,
        title = title,
        date = dateMillis,
        summary = summary,
        isRead = false,
        isMaterial = isMaterial
    )
}
