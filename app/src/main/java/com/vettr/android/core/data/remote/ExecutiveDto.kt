package com.vettr.android.core.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.vettr.android.core.model.Executive

/**
 * Data Transfer Object for Executive entity from admin API.
 * Maps JSON response fields to Kotlin properties using Gson serialization.
 * Uses camelCase field names matching the admin endpoint response format.
 */
data class ExecutiveDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("stockId")
    val stockId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("yearsAtCompany")
    val yearsAtCompany: Double,

    @SerializedName("previousCompanies")
    val previousCompanies: List<String>,

    @SerializedName("education")
    val education: String,

    @SerializedName("specialization")
    val specialization: String,

    @SerializedName("socialLinkedin")
    val socialLinkedin: String? = null,

    @SerializedName("socialTwitter")
    val socialTwitter: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

/**
 * Convert ExecutiveDto from admin API to Executive domain model.
 * Serializes previousCompanies list to JSON string for Room storage.
 */
fun ExecutiveDto.toExecutive(): Executive {
    val previousCompaniesJson = Gson().toJson(previousCompanies)

    return Executive(
        id = id,
        stockId = stockId,
        name = name,
        title = title,
        yearsAtCompany = yearsAtCompany,
        previousCompanies = previousCompaniesJson,
        education = education,
        specialization = specialization,
        socialLinkedIn = socialLinkedin,
        socialTwitter = socialTwitter
    )
}
