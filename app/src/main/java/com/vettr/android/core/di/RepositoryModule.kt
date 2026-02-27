package com.vettr.android.core.di

import com.vettr.android.core.data.repository.AiAgentRepository
import com.vettr.android.core.data.repository.AiAgentRepositoryImpl
import com.vettr.android.core.data.repository.AlertRuleRepository
import com.vettr.android.core.data.repository.AlertRuleRepositoryImpl
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.data.repository.AuthRepositoryImpl
import com.vettr.android.core.data.repository.ExecutiveRepository
import com.vettr.android.core.data.repository.ExecutiveRepositoryImpl
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.data.repository.FilingRepositoryImpl
import com.vettr.android.core.data.repository.FundamentalsRepository
import com.vettr.android.core.data.repository.FundamentalsRepositoryImpl
import com.vettr.android.core.data.repository.NewsRepository
import com.vettr.android.core.data.repository.NewsRepositoryImpl
import com.vettr.android.core.data.repository.PortfolioAlertsRepository
import com.vettr.android.core.data.repository.PortfolioAlertsRepositoryImpl
import com.vettr.android.core.data.repository.PortfolioInsightsRepository
import com.vettr.android.core.data.repository.PortfolioInsightsRepositoryImpl
import com.vettr.android.core.data.repository.PortfolioRepository
import com.vettr.android.core.data.repository.PortfolioRepositoryImpl
import com.vettr.android.core.data.repository.PulseRepository
import com.vettr.android.core.data.repository.PulseRepositoryImpl
import com.vettr.android.core.data.repository.RedFlagRepository
import com.vettr.android.core.data.repository.RedFlagRepositoryImpl
import com.vettr.android.core.data.repository.SamplePortfolioRepository
import com.vettr.android.core.data.repository.SamplePortfolioRepositoryImpl
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.data.repository.StockRepositoryImpl
import com.vettr.android.core.data.repository.VetrScoreRepository
import com.vettr.android.core.data.repository.VetrScoreRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindExecutiveRepository(
        executiveRepositoryImpl: ExecutiveRepositoryImpl
    ): ExecutiveRepository

    @Binds
    @Singleton
    abstract fun bindAlertRuleRepository(
        alertRuleRepositoryImpl: AlertRuleRepositoryImpl
    ): AlertRuleRepository

    @Binds
    @Singleton
    abstract fun bindRedFlagRepository(
        redFlagRepositoryImpl: RedFlagRepositoryImpl
    ): RedFlagRepository

    @Binds
    @Singleton
    abstract fun bindVetrScoreRepository(
        vetrScoreRepositoryImpl: VetrScoreRepositoryImpl
    ): VetrScoreRepository

    @Binds
    @Singleton
    abstract fun bindPulseRepository(
        pulseRepositoryImpl: PulseRepositoryImpl
    ): PulseRepository

    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(
        portfolioRepositoryImpl: PortfolioRepositoryImpl
    ): PortfolioRepository

    @Binds
    @Singleton
    abstract fun bindPortfolioAlertsRepository(
        portfolioAlertsRepositoryImpl: PortfolioAlertsRepositoryImpl
    ): PortfolioAlertsRepository

    @Binds
    @Singleton
    abstract fun bindPortfolioInsightsRepository(
        portfolioInsightsRepositoryImpl: PortfolioInsightsRepositoryImpl
    ): PortfolioInsightsRepository

    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository

    @Binds
    @Singleton
    abstract fun bindAiAgentRepository(
        aiAgentRepositoryImpl: AiAgentRepositoryImpl
    ): AiAgentRepository

    @Binds
    @Singleton
    abstract fun bindFundamentalsRepository(
        fundamentalsRepositoryImpl: FundamentalsRepositoryImpl
    ): FundamentalsRepository

    @Binds
    @Singleton
    abstract fun bindSamplePortfolioRepository(
        samplePortfolioRepositoryImpl: SamplePortfolioRepositoryImpl
    ): SamplePortfolioRepository
}
