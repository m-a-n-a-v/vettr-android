package com.vettr.android.core.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for VETTR backend endpoints.
 * Defines REST API operations for stocks, filings, executives, and authentication.
 */
interface VettrApi {

    /**
     * Fetch all available stocks from the admin endpoint.
     * Uses X-Admin-Secret header for authentication.
     * @return Admin paginated response wrapping stock DTOs
     */
    @GET("admin/stocks")
    suspend fun getStocks(
        @Header("X-Admin-Secret") adminSecret: String = ADMIN_SECRET,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): AdminListResponse<StockDto>

    /**
     * Search stocks from the admin endpoint.
     * @return Admin paginated response wrapping stock DTOs
     */
    @GET("admin/stocks")
    suspend fun searchStocks(
        @Header("X-Admin-Secret") adminSecret: String = ADMIN_SECRET,
        @Query("search") query: String,
        @Query("limit") limit: Int = 25
    ): AdminListResponse<StockDto>

    /**
     * Fetch all filings from the admin endpoint.
     * Uses X-Admin-Secret header for authentication.
     * @return Admin paginated response wrapping filing DTOs
     */
    @GET("admin/filings")
    suspend fun getFilings(
        @Header("X-Admin-Secret") adminSecret: String = ADMIN_SECRET,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): AdminListResponse<FilingDto>

    @GET("admin/filings")
    suspend fun getFilingsForStock(
        @Header("X-Admin-Secret") adminSecret: String = ADMIN_SECRET,
        @Query("filter_stockId") stockId: String,
        @Query("limit") limit: Int = 50
    ): AdminListResponse<FilingDto>

    @GET("admin/executives")
    suspend fun getExecutives(
        @Header("X-Admin-Secret") adminSecret: String = ADMIN_SECRET,
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0
    ): AdminListResponse<ExecutiveDto>

    @GET("admin/executives")
    suspend fun getExecutivesForStock(
        @Header("X-Admin-Secret") adminSecret: String = ADMIN_SECRET,
        @Query("filter_stockId") stockId: String,
        @Query("limit") limit: Int = 50
    ): AdminListResponse<ExecutiveDto>

    /**
     * Authenticate user and obtain access token.
     * @param request Login request containing credentials
     * @return Authentication response with token and user info
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    /**
     * Refresh access token using refresh token.
     * @param request Refresh token request containing the refresh token
     * @return Authentication response with new access token
     */
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    companion object {
        const val ADMIN_SECRET = "vettr-admin-fd885f9b154cc74249c566e4cf66b4dd"
    }
}
