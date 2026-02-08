package com.vettr.android.core.data.remote

import com.vettr.android.core.data.local.TokenManager
import com.vettr.android.core.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that automatically adds authentication headers
 * and handles token refresh on 401 responses.
 *
 * Flow:
 * 1. Add Authorization: Bearer {token} header to all requests
 * 2. If 401 response: attempt token refresh
 * 3. On refresh success: retry original request with new token
 * 4. On refresh failure: clear tokens and emit unauthorized state
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for auth endpoints
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        // Add Authorization header if token exists
        val token = tokenManager.getToken()
        val requestWithAuth = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        // Execute request
        val response = chain.proceed(requestWithAuth)

        // Handle 401 Unauthorized
        if (response.code == 401 && token != null) {
            response.close()

            // Attempt token refresh
            val refreshSuccess = runBlocking {
                refreshToken()
            }

            if (refreshSuccess) {
                // Retry original request with new token
                val newToken = tokenManager.getToken()
                val retryRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()

                return chain.proceed(retryRequest)
            } else {
                // Refresh failed - sign out user
                runBlocking {
                    authRepository.signOut()
                }

                // Return original 401 response
                return chain.proceed(originalRequest)
            }
        }

        return response
    }

    /**
     * Attempt to refresh the access token using the refresh token.
     * @return true if refresh succeeded, false otherwise
     */
    private suspend fun refreshToken(): Boolean {
        return try {
            val refreshToken = tokenManager.getRefreshToken() ?: return false

            // TODO: Call actual refresh endpoint when backend is ready
            // For now, this is a placeholder that returns false
            // In production, this would call:
            // val response = vettrApi.refreshToken(RefreshTokenRequest(refreshToken))
            // tokenManager.saveToken(response.accessToken)
            // tokenManager.saveRefreshToken(response.refreshToken)
            // return true

            false
        } catch (e: Exception) {
            false
        }
    }
}
