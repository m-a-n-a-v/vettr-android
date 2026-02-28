package com.vettr.android.core.data.remote

import clerk.android.Clerk
import com.vettr.android.core.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that automatically adds the Clerk session token as
 * Authorization: Bearer header on every authenticated request.
 *
 * Clerk handles token refresh automatically — if the cached session token
 * is close to expiry, Clerk rotates it before returning it here.
 *
 * Uses dagger.Lazy<AuthRepository> to break the circular dependency:
 * OkHttpClient → Retrofit → VettrApi → AuthRepository → AuthInterceptor → OkHttpClient
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val authRepository: dagger.Lazy<AuthRepository>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for admin endpoints
        if (originalRequest.url.encodedPath.contains("/admin/")) {
            return chain.proceed(originalRequest)
        }

        // Fetch the current Clerk session token (blocking call on the OkHttp thread).
        val token: String? = runBlocking {
            try {
                Clerk.shared.session?.getToken()?.jwt
            } catch (_: Exception) {
                null
            }
        }

        val requestWithAuth = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(requestWithAuth)

        // If the backend returns 401, sign the user out so they can re-authenticate.
        if (response.code == 401) {
            response.close()
            runBlocking {
                try {
                    authRepository.get().signOut()
                } catch (_: Exception) { /* best effort */ }
            }
            return chain.proceed(originalRequest)
        }

        return response
    }
}
