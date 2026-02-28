package com.vettr.android.core.di

import com.vettr.android.BuildConfig
import com.vettr.android.core.data.remote.AuthInterceptor
import com.vettr.android.core.data.remote.PublicApi
import com.vettr.android.core.data.remote.VettrApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Qualifier for the authenticated OkHttpClient (includes AuthInterceptor). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient

/** Qualifier for the public OkHttpClient (no AuthInterceptor). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicClient

/**
 * Hilt module providing network-related dependencies.
 * Provides two OkHttpClient/Retrofit pairs:
 * 1. Authenticated: includes AuthInterceptor for JWT-protected endpoints (VettrApi)
 * 2. Public: no auth interceptor for guest-accessible endpoints (PublicApi)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // API_BASE_URL is set per build type in app/build.gradle.kts:
    // debug  -> http://10.0.2.2:3000/v1/ (local Docker backend via emulator)
    // release -> https://api.vettr.com/v1/ (production)
    private const val BASE_URL = BuildConfig.API_BASE_URL
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    /**
     * Provides a shared logging interceptor for both clients.
     * Logs HTTP request/response body in debug builds, nothing in release.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // ═══════ Authenticated (JWT) client ═══════

    /**
     * Provides singleton OkHttpClient with auth and logging interceptors.
     * AuthInterceptor adds Bearer token and handles token refresh on 401.
     */
    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides the default (unqualified) OkHttpClient for backward compatibility.
     * Delegates to the authenticated client so existing injection sites continue to work.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides singleton Retrofit instance with Gson converter.
     * Uses the authenticated OkHttpClient.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides VettrApi instance from Retrofit.
     * Used for all authenticated network operations throughout the app.
     */
    @Provides
    @Singleton
    fun provideVettrApi(retrofit: Retrofit): VettrApi {
        return retrofit.create(VettrApi::class.java)
    }

    // ═══════ Public (unauthenticated) client ═══════

    /**
     * Provides a public OkHttpClient without AuthInterceptor.
     * Used for guest-accessible endpoints that do not require JWT.
     */
    @Provides
    @Singleton
    @PublicClient
    fun providePublicOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides PublicApi instance using the public (no-auth) OkHttpClient.
     * Used for guest landing screen autocomplete and stock preview.
     */
    @Provides
    @Singleton
    fun providePublicApi(@PublicClient publicClient: OkHttpClient): PublicApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(publicClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PublicApi::class.java)
    }
}
