package com.vettr.android.core.di

import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.FilingRepositoryImpl
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.data.repository.StockRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl
    ): StockRepository

    @Binds
    @Singleton
    abstract fun bindFilingRepository(
        filingRepositoryImpl: FilingRepositoryImpl
    ): FilingRepository
}
