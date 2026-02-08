package com.vettr.android.core.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for VETTR backend endpoints.
 * Defines REST API operations for stocks, filings, and authentication.
 */
interface VettrApi {

    /**
     * Fetch all available stocks from the backend.
     * @return List of stock DTOs with market data and Vetr scores
     */
    @GET("stocks")
    suspend fun getStocks(): List<StockDto>

    /**
     * Fetch detailed information for a specific stock by ticker.
     * @param ticker Stock ticker symbol (e.g., "SHOP", "RY")
     * @return Stock DTO with detailed market data
     */
    @GET("stocks/{ticker}")
    suspend fun getStock(@Path("ticker") ticker: String): StockDto

    /**
     * Fetch filings for a specific stock.
     * @param stockId ID of the stock to fetch filings for
     * @return List of filing DTOs for the specified stock
     */
    @GET("filings")
    suspend fun getFilings(@Query("stockId") stockId: String): List<FilingDto>

    /**
     * Authenticate user and obtain access token.
     * @param request Login request containing credentials
     * @return Authentication response with token and user info
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}
