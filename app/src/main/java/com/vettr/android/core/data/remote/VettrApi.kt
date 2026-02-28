package com.vettr.android.core.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
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
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): AdminListResponse<ExecutiveDto>

    @GET("admin/executives")
    suspend fun getExecutivesForStock(
        @Header("X-Admin-Secret") adminSecret: String = ADMIN_SECRET,
        @Query("filter_stockId") stockId: String,
        @Query("limit") limit: Int = 50
    ): AdminListResponse<ExecutiveDto>

    /**
     * Fetch pulse summary data for the dashboard.
     * Uses JWT authentication (Authorization: Bearer token) automatically added by AuthInterceptor.
     * @return Pulse summary response with watchlist health, sector exposure, and red flags
     */
    @GET("pulse/summary")
    suspend fun getPulseSummary(): PulseSummaryResponse

    /**
     * Fetch discovery collections with curated stock lists.
     * Uses JWT authentication (Authorization: Bearer token) automatically added by AuthInterceptor.
     * @return Discovery collections response with list of collections
     */
    @GET("discovery/collections")
    suspend fun getDiscoveryCollections(): DiscoveryCollectionsResponse

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

    // ═══════ Portfolio Endpoints (JWT auth via AuthInterceptor) ═══════

    @GET("portfolio")
    suspend fun listPortfolios(): Response<List<PortfolioResponse>>

    @POST("portfolio")
    suspend fun createPortfolio(@Body request: CreatePortfolioRequest): Response<PortfolioResponse>

    @GET("portfolio/{id}")
    suspend fun getPortfolio(@Path("id") id: String): Response<PortfolioResponse>

    @DELETE("portfolio/{id}")
    suspend fun deletePortfolio(@Path("id") id: String): Response<Unit>

    @GET("portfolio/holdings")
    suspend fun listAllHoldings(): Response<List<HoldingResponse>>

    @GET("portfolio/{id}/holdings")
    suspend fun listHoldings(@Path("id") portfolioId: String): Response<List<HoldingResponse>>

    @POST("portfolio/{id}/holdings")
    suspend fun addHolding(
        @Path("id") portfolioId: String,
        @Body request: AddHoldingRequest
    ): Response<HoldingResponse>

    @PUT("portfolio/{id}/holdings/{holdingId}")
    suspend fun updateHolding(
        @Path("id") portfolioId: String,
        @Path("holdingId") holdingId: String,
        @Body request: UpdateHoldingRequest
    ): Response<HoldingResponse>

    @DELETE("portfolio/{id}/holdings/{holdingId}")
    suspend fun deleteHolding(
        @Path("id") portfolioId: String,
        @Path("holdingId") holdingId: String
    ): Response<Unit>

    @GET("portfolio/summary")
    suspend fun getPortfolioSummary(): Response<PortfolioSummaryResponse>

    // ═══════ Portfolio Alerts Endpoints (JWT auth via AuthInterceptor) ═══════

    @GET("portfolio-alerts")
    suspend fun getPortfolioAlerts(): Response<List<PortfolioAlertResponse>>

    @GET("portfolio-alerts/unread-count")
    suspend fun getPortfolioAlertUnreadCount(): Response<UnreadCountResponse>

    @POST("portfolio-alerts/{id}/read")
    suspend fun markPortfolioAlertRead(@Path("id") id: String): Response<Unit>

    @POST("portfolio-alerts/read-all")
    suspend fun markAllPortfolioAlertsRead(): Response<Unit>

    // ═══════ Portfolio Insights Endpoints (JWT auth via AuthInterceptor) ═══════

    @GET("portfolio-insights")
    suspend fun getPortfolioInsights(): Response<List<PortfolioInsightResponse>>

    @POST("portfolio-insights/{id}/dismiss")
    suspend fun dismissInsight(@Path("id") id: String): Response<Unit>

    // ═══════ Sample Portfolios (No Auth) ═══════

    @GET("sample-portfolios")
    suspend fun getSamplePortfolios(): Response<List<SamplePortfolioResponse>>

    // ═══════ News (No Auth) ═══════

    @GET("news")
    suspend fun getNews(
        @Query("source") source: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<NewsArticleResponse>>

    // ═══════ AI Agent Endpoints (JWT auth via AuthInterceptor) ═══════

    @POST("ai-agent/ask")
    suspend fun askAiAgent(@Body request: AiAgentRequest): Response<AiAgentResponse>

    @GET("ai-agent/questions")
    suspend fun getAiAgentQuestions(): Response<List<AiAgentQuestionResponse>>

    @GET("ai-agent/usage")
    suspend fun getAiAgentUsage(): Response<AiAgentUsageResponse>

    // ═══════ Fundamentals & Score Comparison (JWT auth) ═══════

    @GET("stocks/{ticker}/fundamentals")
    suspend fun getFundamentals(@Path("ticker") ticker: String): Response<FundamentalsResponse>

    @GET("stocks/{ticker}/vetr-score/compare")
    suspend fun getScoreComparison(@Path("ticker") ticker: String): Response<ScoreComparisonResponse>

    @GET("red-flags/trend")
    suspend fun getRedFlagTrends(
        @Query("period") period: String? = null
    ): Response<RedFlagTrendResponse>

    // ═══════ Password Reset (No Auth) ═══════

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    // ═══════ Password Change (JWT auth) ═══════

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // ═══════ Google Sign-In (No Auth) ═══════

    @POST("auth/google")
    suspend fun googleSignIn(@Body request: LoginRequest): AuthResponse

    // ═══════ Account Deletion (JWT auth via AuthInterceptor) ═══════

    @DELETE("users/me")
    suspend fun deleteAccount(): Response<Unit>

    // ═══════ Device Registration (JWT auth via AuthInterceptor) ═══════

    @POST("devices/register")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "devices/unregister", hasBody = true)
    suspend fun unregisterDevice(@Body request: UnregisterDeviceRequest): Response<Unit>

    companion object {
        const val ADMIN_SECRET = "vettr-admin-fd885f9b154cc74249c566e4cf66b4dd"
    }
}
