package com.vettr.android.core.di

import com.vettr.android.core.util.MockObservabilityService
import com.vettr.android.core.util.ObservabilityService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing observability and monitoring services.
 * Currently uses mock implementation for development.
 * Replace with Firebase Performance Monitoring in production.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ObservabilityModule {

    @Binds
    @Singleton
    abstract fun bindObservabilityService(
        mockObservabilityService: MockObservabilityService
    ): ObservabilityService
}
