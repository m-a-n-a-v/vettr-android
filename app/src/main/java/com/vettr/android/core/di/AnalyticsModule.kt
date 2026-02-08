package com.vettr.android.core.di

import com.vettr.android.core.util.AnalyticsService
import com.vettr.android.core.util.MockAnalyticsService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing analytics-related dependencies.
 * Uses MockAnalyticsService for debug builds with Logcat logging.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsService(
        mockAnalyticsService: MockAnalyticsService
    ): AnalyticsService
}
