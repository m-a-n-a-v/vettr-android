package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Public (unauthenticated) Retrofit API interface for guest-accessible endpoints.
 * Uses a separate OkHttpClient without AuthInterceptor so requests are made without JWT.
 *
 * Provides:
 * - Stock autocomplete search for the guest landing screen
 * - Stock preview data (limited) for the guest stock preview screen
 */
interface PublicApi {

    /**
     * Search for stocks by ticker or company name.
     * Returns lightweight autocomplete results for the search bar.
     *
     * @param query Search query string
     * @param limit Maximum number of results to return
     * @return AutocompleteResponse wrapping a list of autocomplete results
     */
    @GET("stocks/autocomplete")
    suspend fun autocomplete(
        @Query("q") query: String,
        @Query("limit") limit: Int = 8
    ): AutocompleteResponse

    /**
     * Fetch distinct sector strings from the stocks table.
     * Returns an array of sector names for filtering.
     *
     * @return List of distinct sector strings
     */
    @GET("stocks/sectors")
    suspend fun getSectors(): List<String>

    /**
     * Fetch a limited stock preview for unauthenticated users.
     * Returns basic stock info, current price, and VETR score without full analysis.
     *
     * @param ticker Stock ticker symbol (e.g., "AAPL")
     * @return StockPreviewResponse wrapping the stock preview data
     */
    @GET("stocks/{ticker}/preview")
    suspend fun stockPreview(
        @Path("ticker") ticker: String
    ): StockPreviewResponse
}

// ═══════ Response DTOs ═══════

data class AutocompleteResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<AutocompleteResultDto>?
)

data class AutocompleteResultDto(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("company_name")
    val companyName: String?,

    @SerializedName("exchange")
    val exchange: String?,

    @SerializedName("sector")
    val sector: String?
)

data class StockPreviewResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: StockPreviewDto?
)

data class StockPreviewDto(
    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("company_name")
    val companyName: String?,

    @SerializedName("exchange")
    val exchange: String?,

    @SerializedName("sector")
    val sector: String?,

    @SerializedName("market_cap")
    val marketCap: Double?,

    @SerializedName("current_price")
    val currentPrice: Double?,

    @SerializedName("price_change_percent")
    val priceChangePercent: Double?,

    @SerializedName("vetr_score")
    val vetrScore: Int?,

    @SerializedName("pillars")
    val pillars: PillarBreakdownDto?
)

data class PillarBreakdownDto(
    @SerializedName("financial_survival")
    val financialSurvival: PillarScoreDto?,

    @SerializedName("operational_efficiency")
    val operationalEfficiency: PillarScoreDto?,

    @SerializedName("shareholder_structure")
    val shareholderStructure: PillarScoreDto?,

    @SerializedName("market_sentiment")
    val marketSentiment: PillarScoreDto?
)

data class PillarScoreDto(
    @SerializedName("score")
    val score: Double?,

    @SerializedName("weight")
    val weight: Double?
)
