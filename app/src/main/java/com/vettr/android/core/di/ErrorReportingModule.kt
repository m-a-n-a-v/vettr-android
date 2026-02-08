package com.vettr.android.core.di

import com.vettr.android.core.util.ErrorReportingService
import com.vettr.android.core.util.MockErrorReportingService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing error reporting dependencies.
 * Uses MockErrorReportingService for debug builds with Logcat logging.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorReportingModule {

    @Binds
    @Singleton
    abstract fun bindErrorReportingService(
        mockErrorReportingService: MockErrorReportingService
    ): ErrorReportingService
}
