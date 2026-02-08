package com.vettr.android.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
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
        // Seed stocks (will be implemented in US-058)
        // TODO: Add 25 pilot stocks

        // Seed filings (will be implemented in US-059)
        // TODO: Add 3-5 filings per stock

        // Seed executives (will be implemented in US-059)
        // TODO: Add 2-4 executives per stock
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
