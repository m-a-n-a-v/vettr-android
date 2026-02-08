package com.vettr.android.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

/**
 * Repository for managing onboarding state using DataStore Preferences.
 * Tracks whether the user has completed the onboarding carousel.
 */
@Singleton
class OnboardingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.onboardingDataStore

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    /**
     * Flow that emits whether onboarding has been completed.
     */
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Marks onboarding as completed.
     */
    suspend fun markOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    /**
     * Resets onboarding completion state (useful for replaying from Profile > About).
     */
    suspend fun resetOnboarding() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = false
        }
    }

    /**
     * Synchronously check if onboarding is completed (for initial routing decisions).
     */
    suspend fun isOnboardingCompletedSync(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }.first()
    }
}
