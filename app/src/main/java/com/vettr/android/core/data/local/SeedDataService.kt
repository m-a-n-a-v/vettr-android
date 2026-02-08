package com.vettr.android.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.vettr.android.core.model.Stock
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "seed_prefs")

@Singleton
class SeedDataService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stockDao: StockDao,
    private val filingDao: FilingDao,
    private val executiveDao: ExecutiveDao
) {
    private val SEED_COMPLETE_KEY = booleanPreferencesKey("seed_complete")

    /**
     * Populates database with pilot seed data.
     * This will be populated with actual data in US-058 and US-059.
     */
    suspend fun seedAllData() {
        // Seed stocks (US-058)
        seedStocks()

        // Seed filings (will be implemented in US-059)
        // TODO: Add 3-5 filings per stock

        // Seed executives (will be implemented in US-059)
        // TODO: Add 2-4 executives per stock
    }

    private suspend fun seedStocks() {
        val stocks = listOf(
            // Strong Buy stocks (score 80-95)
            Stock(
                ticker = "BTO.TO",
                name = "B2Gold Corp",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 5_400_000_000.0,
                price = 4.85,
                priceChange = 0.12,
                vetrScore = 88,
                isFavorite = false
            ),
            Stock(
                ticker = "TVE.TO",
                name = "Tamarack Valley Energy",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 2_800_000_000.0,
                price = 4.23,
                priceChange = 0.18,
                vetrScore = 85,
                isFavorite = false
            ),
            Stock(
                ticker = "LSPD.TO",
                name = "Lightspeed Commerce",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 3_200_000_000.0,
                price = 21.45,
                priceChange = -0.35,
                vetrScore = 82,
                isFavorite = false
            ),
            Stock(
                ticker = "IE.TO",
                name = "Ivanhoe Electric",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 1_500_000_000.0,
                price = 12.80,
                priceChange = 0.45,
                vetrScore = 90,
                isFavorite = false
            ),
            Stock(
                ticker = "CJ.TO",
                name = "Cardinal Energy",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 1_100_000_000.0,
                price = 7.15,
                priceChange = 0.22,
                vetrScore = 83,
                isFavorite = false
            ),

            // Buy stocks (score 60-79)
            Stock(
                ticker = "DML.TO",
                name = "Denison Mines",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 1_800_000_000.0,
                price = 2.35,
                priceChange = -0.08,
                vetrScore = 75,
                isFavorite = false
            ),
            Stock(
                ticker = "LAAC.TO",
                name = "Lithium Americas (Argentina)",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 900_000_000.0,
                price = 4.60,
                priceChange = 0.15,
                vetrScore = 72,
                isFavorite = false
            ),
            Stock(
                ticker = "GRN.TO",
                name = "Greenlane Renewables",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 180_000_000.0,
                price = 1.12,
                priceChange = -0.03,
                vetrScore = 68,
                isFavorite = false
            ),
            Stock(
                ticker = "NPI.TO",
                name = "Northland Power",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 4_200_000_000.0,
                price = 18.75,
                priceChange = 0.28,
                vetrScore = 70,
                isFavorite = false
            ),
            Stock(
                ticker = "DCBO.TO",
                name = "Docebo Inc",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 1_600_000_000.0,
                price = 52.30,
                priceChange = -0.95,
                vetrScore = 73,
                isFavorite = false
            ),
            Stock(
                ticker = "DND.TO",
                name = "Dye & Durham",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 1_400_000_000.0,
                price = 19.85,
                priceChange = 0.42,
                vetrScore = 67,
                isFavorite = false
            ),
            Stock(
                ticker = "ENGH.TO",
                name = "Enghouse Systems",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 1_900_000_000.0,
                price = 34.20,
                priceChange = -0.18,
                vetrScore = 71,
                isFavorite = false
            ),
            Stock(
                ticker = "PAID.CSE",
                name = "PAID Inc",
                exchange = "CSE",
                sector = "Technology",
                marketCap = 45_000_000.0,
                price = 0.65,
                priceChange = 0.05,
                vetrScore = 65,
                isFavorite = false
            ),
            Stock(
                ticker = "FOM.V",
                name = "Foran Mining",
                exchange = "TSXV",
                sector = "Mining",
                marketCap = 620_000_000.0,
                price = 3.15,
                priceChange = -0.12,
                vetrScore = 62,
                isFavorite = false
            ),

            // Hold stocks (score 40-59)
            Stock(
                ticker = "AMC.V",
                name = "Arizona Metals",
                exchange = "TSXV",
                sector = "Mining",
                marketCap = 280_000_000.0,
                price = 2.88,
                priceChange = 0.08,
                vetrScore = 52,
                isFavorite = false
            ),
            Stock(
                ticker = "GMIN.TO",
                name = "G Mining Ventures",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 350_000_000.0,
                price = 2.45,
                priceChange = -0.05,
                vetrScore = 48,
                isFavorite = false
            ),
            Stock(
                ticker = "SOIL.V",
                name = "SOiL Innovations",
                exchange = "TSXV",
                sector = "Agriculture",
                marketCap = 25_000_000.0,
                price = 0.38,
                priceChange = 0.02,
                vetrScore = 50,
                isFavorite = false
            ),
            Stock(
                ticker = "QUIS.V",
                name = "Quisitive Technology Solutions",
                exchange = "TSXV",
                sector = "Technology",
                marketCap = 95_000_000.0,
                price = 1.42,
                priceChange = -0.07,
                vetrScore = 53,
                isFavorite = false
            ),
            Stock(
                ticker = "PNG.V",
                name = "PNG Energy",
                exchange = "TSXV",
                sector = "Energy",
                marketCap = 12_000_000.0,
                price = 0.15,
                priceChange = 0.01,
                vetrScore = 45,
                isFavorite = false
            ),
            Stock(
                ticker = "PLUR.V",
                name = "Plurilock Security",
                exchange = "TSXV",
                sector = "Technology",
                marketCap = 32_000_000.0,
                price = 0.52,
                priceChange = -0.03,
                vetrScore = 49,
                isFavorite = false
            ),
            Stock(
                ticker = "NDA.V",
                name = "New Destiny Mining",
                exchange = "TSXV",
                sector = "Mining",
                marketCap = 18_000_000.0,
                price = 0.22,
                priceChange = 0.00,
                vetrScore = 47,
                isFavorite = false
            ),

            // Caution stocks (score 30-39)
            Stock(
                ticker = "ACT.CSE",
                name = "Applied Blockchain",
                exchange = "CSE",
                sector = "Technology",
                marketCap = 8_000_000.0,
                price = 0.08,
                priceChange = -0.02,
                vetrScore = 35,
                isFavorite = false
            ),
            Stock(
                ticker = "NTAR.CSE",
                name = "Nextech AR Solutions",
                exchange = "CSE",
                sector = "Technology",
                marketCap = 22_000_000.0,
                price = 0.28,
                priceChange = -0.04,
                vetrScore = 38,
                isFavorite = false
            ),

            // Additional stocks to reach 25
            Stock(
                ticker = "BIR.TO",
                name = "Birchcliff Energy",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 1_100_000_000.0,
                price = 4.15,
                priceChange = 0.18,
                vetrScore = 33,
                isFavorite = false
            ),
            Stock(
                ticker = "VET.TO",
                name = "Vermilion Energy",
                exchange = "TSX",
                sector = "Energy",
                marketCap = 1_800_000_000.0,
                price = 11.20,
                priceChange = -0.25,
                vetrScore = 37,
                isFavorite = false
            )
        )

        stockDao.insertAll(stocks)
    }

    /**
     * Checks if seed data has already been populated.
     */
    suspend fun isSeedComplete(): Boolean {
        return context.dataStore.data
            .map { preferences ->
                preferences[SEED_COMPLETE_KEY] ?: false
            }
            .first()
    }

    /**
     * Marks seed data as complete in DataStore preferences.
     */
    suspend fun markSeedComplete() {
        context.dataStore.edit { preferences ->
            preferences[SEED_COMPLETE_KEY] = true
        }
    }
}
