package com.vettr.android.core.di

import android.content.Context
import androidx.room.Room
import com.vettr.android.BuildConfig
import com.vettr.android.core.data.local.AlertRuleDao
import com.vettr.android.core.data.local.ExecutiveDao
import com.vettr.android.core.data.local.FilingDao
import com.vettr.android.core.data.local.MIGRATION_1_2
import com.vettr.android.core.data.local.PortfolioAlertDao
import com.vettr.android.core.data.local.PortfolioDao
import com.vettr.android.core.data.local.PortfolioHoldingDao
import com.vettr.android.core.data.local.PortfolioInsightDao
import com.vettr.android.core.data.local.RedFlagHistoryDao
import com.vettr.android.core.data.local.StockDao
import com.vettr.android.core.data.local.SyncHistoryDao
import com.vettr.android.core.data.local.UserDao
import com.vettr.android.core.data.local.VettrDatabase
import com.vettr.android.core.data.local.VetrScoreHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVettrDatabase(@ApplicationContext context: Context): VettrDatabase {
        val builder = Room.databaseBuilder(
            context,
            VettrDatabase::class.java,
            "vettr-db"
        )

        // Add migrations
        builder.addMigrations(MIGRATION_1_2)

        // For debug builds, allow destructive migrations
        // This will drop and recreate tables if migration is missing
        // Production builds MUST have explicit migrations
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration()
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideStockDao(database: VettrDatabase): StockDao {
        return database.stockDao()
    }

    @Provides
    @Singleton
    fun provideFilingDao(database: VettrDatabase): FilingDao {
        return database.filingDao()
    }

    @Provides
    @Singleton
    fun provideExecutiveDao(database: VettrDatabase): ExecutiveDao {
        return database.executiveDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: VettrDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideAlertRuleDao(database: VettrDatabase): AlertRuleDao {
        return database.alertRuleDao()
    }

    @Provides
    @Singleton
    fun provideRedFlagHistoryDao(database: VettrDatabase): RedFlagHistoryDao {
        return database.redFlagHistoryDao()
    }

    @Provides
    @Singleton
    fun provideVetrScoreHistoryDao(database: VettrDatabase): VetrScoreHistoryDao {
        return database.vetrScoreHistoryDao()
    }

    @Provides
    @Singleton
    fun provideSyncHistoryDao(database: VettrDatabase): SyncHistoryDao {
        return database.syncHistoryDao()
    }

    @Provides
    @Singleton
    fun providePortfolioDao(database: VettrDatabase): PortfolioDao {
        return database.portfolioDao()
    }

    @Provides
    @Singleton
    fun providePortfolioHoldingDao(database: VettrDatabase): PortfolioHoldingDao {
        return database.portfolioHoldingDao()
    }

    @Provides
    @Singleton
    fun providePortfolioAlertDao(database: VettrDatabase): PortfolioAlertDao {
        return database.portfolioAlertDao()
    }

    @Provides
    @Singleton
    fun providePortfolioInsightDao(database: VettrDatabase): PortfolioInsightDao {
        return database.portfolioInsightDao()
    }
}
