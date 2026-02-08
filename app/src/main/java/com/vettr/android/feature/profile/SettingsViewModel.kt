package com.vettr.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.SettingsRepository
import com.vettr.android.core.util.BiometricCheckResult
import com.vettr.android.core.util.BiometricService
import com.vettr.android.core.util.HapticService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    val currency: String = "CAD",
    val darkMode: Boolean = true,
    val language: String = "English",
    val filingNotifications: Boolean = true,
    val priceAlertNotifications: Boolean = true,
    val insiderNotifications: Boolean = true,
    val redFlagNotifications: Boolean = true,
    val notificationFrequency: String = "Real-time",
    val analyticsOptOut: Boolean = false,
    val crashReportingOptOut: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val biometricLoginEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val biometricUnavailableReason: String? = null
)

/**
 * ViewModel for the Settings screen.
 * Manages app preferences and settings using DataStore.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val biometricService: BiometricService,
    val hapticService: HapticService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Check biometric availability
        val biometricResult = biometricService.isBiometricAvailable()
        val biometricAvailable = biometricResult is BiometricCheckResult.Available
        val biometricUnavailableReason = if (biometricResult is BiometricCheckResult.Unavailable) {
            biometricResult.reason
        } else null

        // Collect all settings flows and update UI state
        viewModelScope.launch {
            combine(
                settingsRepository.currency,
                settingsRepository.darkMode,
                settingsRepository.language,
                settingsRepository.filingNotifications,
                settingsRepository.priceAlertNotifications,
                settingsRepository.insiderNotifications,
                settingsRepository.redFlagNotifications,
                settingsRepository.notificationFrequency,
                settingsRepository.analyticsOptOut,
                settingsRepository.crashReportingOptOut,
                settingsRepository.hapticFeedbackEnabled,
                settingsRepository.biometricLoginEnabled
            ) { values ->
                SettingsUiState(
                    currency = values[0] as String,
                    darkMode = values[1] as Boolean,
                    language = values[2] as String,
                    filingNotifications = values[3] as Boolean,
                    priceAlertNotifications = values[4] as Boolean,
                    insiderNotifications = values[5] as Boolean,
                    redFlagNotifications = values[6] as Boolean,
                    notificationFrequency = values[7] as String,
                    analyticsOptOut = values[8] as Boolean,
                    crashReportingOptOut = values[9] as Boolean,
                    hapticFeedbackEnabled = values[10] as Boolean,
                    biometricLoginEnabled = values[11] as Boolean,
                    biometricAvailable = biometricAvailable,
                    biometricUnavailableReason = biometricUnavailableReason
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    // General settings
    fun setCurrency(value: String) {
        viewModelScope.launch {
            settingsRepository.setCurrency(value)
        }
    }

    fun setDarkMode(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(value)
        }
    }

    fun setLanguage(value: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(value)
        }
    }

    // Notification settings
    fun setFilingNotifications(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFilingNotifications(value)
        }
    }

    fun setPriceAlertNotifications(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPriceAlertNotifications(value)
        }
    }

    fun setInsiderNotifications(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setInsiderNotifications(value)
        }
    }

    fun setRedFlagNotifications(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRedFlagNotifications(value)
        }
    }

    fun setNotificationFrequency(value: String) {
        viewModelScope.launch {
            settingsRepository.setNotificationFrequency(value)
        }
    }

    // Privacy settings
    fun setAnalyticsOptOut(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAnalyticsOptOut(value)
        }
    }

    fun setCrashReportingOptOut(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCrashReportingOptOut(value)
        }
    }

    // Accessibility settings
    fun setHapticFeedbackEnabled(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackEnabled(value)
        }
    }

    // Security settings
    fun setBiometricLoginEnabled(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricLoginEnabled(value)
        }
    }

    // Reset app data
    fun resetApp(view: android.view.View?) {
        viewModelScope.launch {
            settingsRepository.resetAllSettings()
            // Heavy haptic for reset action
            hapticService.heavy(view)
            // TODO: Clear database and other app data in future story
        }
    }
}
