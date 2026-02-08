package com.vettr.android.core.di

import android.content.Context
import com.vettr.android.core.util.notification.NotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing notification-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationService(
        @ApplicationContext context: Context
    ): NotificationService {
        return NotificationService(context)
    }
}
