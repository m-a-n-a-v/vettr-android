package com.vettr.android.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing app settings using DataStore Preferences.
 * Handles persistence of user preferences for:
 * - General settings (currency, dark mode, language)
 * - Notification preferences (per-type toggles, frequency)
 * - Privacy settings (analytics/crash opt-out)
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    companion object {
        // General settings keys
        val CURRENCY = stringPreferencesKey("currency")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")

        // Notification settings keys
        val FILING_NOTIFICATIONS = booleanPreferencesKey("filing_notifications")
        val PRICE_ALERT_NOTIFICATIONS = booleanPreferencesKey("price_alert_notifications")
        val INSIDER_NOTIFICATIONS = booleanPreferencesKey("insider_notifications")
        val RED_FLAG_NOTIFICATIONS = booleanPreferencesKey("red_flag_notifications")
        val NOTIFICATION_FREQUENCY = stringPreferencesKey("notification_frequency")

        // Privacy settings keys
        val ANALYTICS_OPT_OUT = booleanPreferencesKey("analytics_opt_out")
        val CRASH_REPORTING_OPT_OUT = booleanPreferencesKey("crash_reporting_opt_out")

        // Default values
        const val DEFAULT_CURRENCY = "CAD"
        const val DEFAULT_DARK_MODE = true
        const val DEFAULT_LANGUAGE = "English"
        const val DEFAULT_FILING_NOTIFICATIONS = true
        const val DEFAULT_PRICE_ALERT_NOTIFICATIONS = true
        const val DEFAULT_INSIDER_NOTIFICATIONS = true
        const val DEFAULT_RED_FLAG_NOTIFICATIONS = true
        const val DEFAULT_NOTIFICATION_FREQUENCY = "Real-time"
        const val DEFAULT_ANALYTICS_OPT_OUT = false
        const val DEFAULT_CRASH_REPORTING_OPT_OUT = false
    }

    // General settings flows
    val currency: Flow<String> = dataStore.data.map { preferences ->
        preferences[CURRENCY] ?: DEFAULT_CURRENCY
    }

    val darkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: DEFAULT_DARK_MODE
    }

    val language: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: DEFAULT_LANGUAGE
    }

    // Notification settings flows
    val filingNotifications: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FILING_NOTIFICATIONS] ?: DEFAULT_FILING_NOTIFICATIONS
    }

    val priceAlertNotifications: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PRICE_ALERT_NOTIFICATIONS] ?: DEFAULT_PRICE_ALERT_NOTIFICATIONS
    }

    val insiderNotifications: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[INSIDER_NOTIFICATIONS] ?: DEFAULT_INSIDER_NOTIFICATIONS
    }

    val redFlagNotifications: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[RED_FLAG_NOTIFICATIONS] ?: DEFAULT_RED_FLAG_NOTIFICATIONS
    }

    val notificationFrequency: Flow<String> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_FREQUENCY] ?: DEFAULT_NOTIFICATION_FREQUENCY
    }

    // Privacy settings flows
    val analyticsOptOut: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ANALYTICS_OPT_OUT] ?: DEFAULT_ANALYTICS_OPT_OUT
    }

    val crashReportingOptOut: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CRASH_REPORTING_OPT_OUT] ?: DEFAULT_CRASH_REPORTING_OPT_OUT
    }

    // Setters for general settings
    suspend fun setCurrency(value: String) {
        dataStore.edit { preferences ->
            preferences[CURRENCY] = value
        }
    }

    suspend fun setDarkMode(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE] = value
        }
    }

    suspend fun setLanguage(value: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = value
        }
    }

    // Setters for notification settings
    suspend fun setFilingNotifications(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[FILING_NOTIFICATIONS] = value
        }
    }

    suspend fun setPriceAlertNotifications(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PRICE_ALERT_NOTIFICATIONS] = value
        }
    }

    suspend fun setInsiderNotifications(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[INSIDER_NOTIFICATIONS] = value
        }
    }

    suspend fun setRedFlagNotifications(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[RED_FLAG_NOTIFICATIONS] = value
        }
    }

    suspend fun setNotificationFrequency(value: String) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_FREQUENCY] = value
        }
    }

    // Setters for privacy settings
    suspend fun setAnalyticsOptOut(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[ANALYTICS_OPT_OUT] = value
        }
    }

    suspend fun setCrashReportingOptOut(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[CRASH_REPORTING_OPT_OUT] = value
        }
    }

    // Reset all settings to defaults
    suspend fun resetAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
