package com.vettr.android.feature.profile

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.BuildConfig
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrTextPrimary
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Settings screen - provides granular control over app preferences and notifications.
 * Features:
 * - General settings (currency, dark mode, language)
 * - Notification settings (per-type toggles, frequency)
 * - Privacy settings (analytics/crash opt-out)
 * - Developer settings (build info, memory, reset) - debug only
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current

    SettingsScreenContent(
        uiState = uiState,
        onCurrencyChange = { viewModel.setCurrency(it) },
        onDarkModeChange = { viewModel.setDarkMode(it) },
        onLanguageChange = { viewModel.setLanguage(it) },
        onFilingNotificationsChange = { viewModel.setFilingNotifications(it) },
        onPriceAlertNotificationsChange = { viewModel.setPriceAlertNotifications(it) },
        onInsiderNotificationsChange = { viewModel.setInsiderNotifications(it) },
        onRedFlagNotificationsChange = { viewModel.setRedFlagNotifications(it) },
        onNotificationFrequencyChange = { viewModel.setNotificationFrequency(it) },
        onAnalyticsOptOutChange = { viewModel.setAnalyticsOptOut(it) },
        onCrashReportingOptOutChange = { viewModel.setCrashReportingOptOut(it) },
        onHapticFeedbackChange = { viewModel.setHapticFeedbackEnabled(it) },
        onBiometricLoginChange = { viewModel.setBiometricLoginEnabled(it) },
        onResetApp = { viewModel.resetApp(view) },
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onCurrencyChange: (String) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onFilingNotificationsChange: (Boolean) -> Unit,
    onPriceAlertNotificationsChange: (Boolean) -> Unit,
    onInsiderNotificationsChange: (Boolean) -> Unit,
    onRedFlagNotificationsChange: (Boolean) -> Unit,
    onNotificationFrequencyChange: (String) -> Unit,
    onAnalyticsOptOutChange: (Boolean) -> Unit,
    onCrashReportingOptOutChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onBiometricLoginChange: (Boolean) -> Unit,
    onResetApp: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showBiometricUnavailableDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VettrNavy,
                    titleContentColor = VettrTextPrimary,
                    navigationIconContentColor = VettrTextPrimary
                )
            )
        },
        containerColor = VettrNavy
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md)
        ) {
            // General Section
            SectionHeader(title = "General")
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingCard {
                Column {
                    SettingRow(
                        title = "Currency",
                        subtitle = uiState.currency
                    ) {
                        // TODO: Add dropdown for currency selection in future story
                    }

                    SettingDivider()

                    SettingToggleRow(
                        title = "Dark Mode",
                        subtitle = "Currently using dark theme",
                        checked = uiState.darkMode,
                        onCheckedChange = onDarkModeChange
                    )

                    SettingDivider()

                    SettingRow(
                        title = "Language",
                        subtitle = uiState.language
                    ) {
                        // TODO: Add dropdown for language selection in future story
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Notifications Section
            SectionHeader(title = "Notifications")
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingCard {
                Column {
                    SettingToggleRow(
                        title = "Filing Notifications",
                        subtitle = "Get notified about new SEC filings",
                        checked = uiState.filingNotifications,
                        onCheckedChange = onFilingNotificationsChange
                    )

                    SettingDivider()

                    SettingToggleRow(
                        title = "Price Alert Notifications",
                        subtitle = "Get notified when price targets are hit",
                        checked = uiState.priceAlertNotifications,
                        onCheckedChange = onPriceAlertNotificationsChange
                    )

                    SettingDivider()

                    SettingToggleRow(
                        title = "Insider Activity Notifications",
                        subtitle = "Get notified about insider trades",
                        checked = uiState.insiderNotifications,
                        onCheckedChange = onInsiderNotificationsChange
                    )

                    SettingDivider()

                    SettingToggleRow(
                        title = "Red Flag Notifications",
                        subtitle = "Get notified about red flags",
                        checked = uiState.redFlagNotifications,
                        onCheckedChange = onRedFlagNotificationsChange
                    )

                    SettingDivider()

                    SettingRow(
                        title = "Notification Frequency",
                        subtitle = uiState.notificationFrequency
                    ) {
                        // TODO: Add dropdown for frequency selection in future story
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Privacy Section
            SectionHeader(title = "Privacy")
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingCard {
                Column {
                    SettingToggleRow(
                        title = "Opt Out of Analytics",
                        subtitle = "Disable usage analytics collection",
                        checked = uiState.analyticsOptOut,
                        onCheckedChange = onAnalyticsOptOutChange
                    )

                    SettingDivider()

                    SettingToggleRow(
                        title = "Opt Out of Crash Reporting",
                        subtitle = "Disable crash report collection",
                        checked = uiState.crashReportingOptOut,
                        onCheckedChange = onCrashReportingOptOutChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Security Section
            SectionHeader(title = "Security")
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingCard {
                Column {
                    SettingToggleRow(
                        title = "Enable Biometric Login",
                        subtitle = if (uiState.biometricAvailable) {
                            if (uiState.biometricLoginEnabled) "Unlock with fingerprint or face" else "Use fingerprint or face to unlock app"
                        } else {
                            uiState.biometricUnavailableReason ?: "Biometric authentication not available"
                        },
                        checked = uiState.biometricLoginEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !uiState.biometricAvailable) {
                                // Show dialog if trying to enable but biometric is unavailable
                                showBiometricUnavailableDialog = true
                            } else {
                                onBiometricLoginChange(enabled)
                            }
                        },
                        enabled = uiState.biometricAvailable
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Accessibility Section
            SectionHeader(title = "Accessibility")
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingCard {
                Column {
                    SettingToggleRow(
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on taps and interactions",
                        checked = uiState.hapticFeedbackEnabled,
                        onCheckedChange = onHapticFeedbackChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // About Section
            SectionHeader(title = "About")
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingCard {
                Column {
                    SettingRow(
                        title = "App Version",
                        subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    ) { }

                    SettingDivider()

                    SettingRow(
                        title = "Build Type",
                        subtitle = if (BuildConfig.DEBUG) "Debug" else "Release"
                    ) { }
                }
            }

            // Developer Section (only in debug builds)
            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(Spacing.lg))

                SectionHeader(title = "Developer")
                Spacer(modifier = Modifier.height(Spacing.sm))

                SettingCard {
                    Column {
                        SettingRow(
                            title = "Device",
                            subtitle = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
                        ) { }

                        SettingDivider()

                        SettingRow(
                            title = "Memory Usage",
                            subtitle = getMemoryUsage()
                        ) { }

                        SettingDivider()

                        SettingActionRow(
                            title = "Reset App Data",
                            subtitle = "Clear all local data and settings",
                            onClick = { showResetDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            } else {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                showResetDialog = false
                onResetApp()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Biometric unavailable dialog
    if (showBiometricUnavailableDialog) {
        BiometricUnavailableDialog(
            reason = uiState.biometricUnavailableReason ?: "Biometric authentication is not available on this device",
            onDismiss = { showBiometricUnavailableDialog = false }
        )
    }
}

/**
 * Section header for settings groups.
 */
@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = VettrTextPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

/**
 * Card container for settings items.
 */
@Composable
private fun SettingCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VettrCardBackground
        )
    ) {
        content()
    }
}

/**
 * Setting row with title and subtitle.
 */
@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = VettrTextPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        }
    }
}

/**
 * Setting row with toggle switch.
 */
@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) VettrTextPrimary else VettrTextSecondary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VettrAccent,
                checkedTrackColor = VettrAccent.copy(alpha = 0.5f),
                uncheckedThumbColor = VettrTextSecondary,
                uncheckedTrackColor = VettrTextSecondary.copy(alpha = 0.3f),
                disabledCheckedThumbColor = VettrTextSecondary.copy(alpha = 0.5f),
                disabledCheckedTrackColor = VettrTextSecondary.copy(alpha = 0.3f),
                disabledUncheckedThumbColor = VettrTextSecondary.copy(alpha = 0.5f),
                disabledUncheckedTrackColor = VettrTextSecondary.copy(alpha = 0.2f)
            )
        )
    }
}

/**
 * Setting row with action button (for developer settings).
 */
@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = VettrTextPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Reset",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Divider between setting items.
 */
@Composable
private fun SettingDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = Spacing.md)
            .background(VettrTextSecondary.copy(alpha = 0.1f))
    )
}

/**
 * Get current memory usage for developer info.
 */
private fun getMemoryUsage(): String {
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMemory = runtime.maxMemory() / (1024 * 1024)
    return "$usedMemory MB / $maxMemory MB"
}

/**
 * Reset app confirmation dialog.
 */
@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VettrCardBackground,
        title = {
            Text(
                text = "Reset App Data",
                style = MaterialTheme.typography.titleMedium,
                color = VettrTextPrimary
            )
        },
        text = {
            Text(
                text = "This will clear all local data and settings. You will be logged out and the app will restart. Are you sure?",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Reset",
                    color = VettrAccent
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = VettrTextSecondary
                )
            }
        }
    )
}

/**
 * Biometric unavailable dialog.
 */
@Composable
private fun BiometricUnavailableDialog(
    reason: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VettrCardBackground,
        title = {
            Text(
                text = "Biometric Login Unavailable",
                style = MaterialTheme.typography.titleMedium,
                color = VettrTextPrimary
            )
        },
        text = {
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = VettrAccent
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    VettrTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(
                currency = "CAD",
                darkMode = true,
                language = "English",
                filingNotifications = true,
                priceAlertNotifications = true,
                insiderNotifications = false,
                redFlagNotifications = true,
                notificationFrequency = "Real-time",
                analyticsOptOut = false,
                crashReportingOptOut = false,
                hapticFeedbackEnabled = true,
                biometricLoginEnabled = true,
                biometricAvailable = true,
                biometricUnavailableReason = null
            ),
            onCurrencyChange = {},
            onDarkModeChange = {},
            onLanguageChange = {},
            onFilingNotificationsChange = {},
            onPriceAlertNotificationsChange = {},
            onInsiderNotificationsChange = {},
            onRedFlagNotificationsChange = {},
            onNotificationFrequencyChange = {},
            onAnalyticsOptOutChange = {},
            onCrashReportingOptOutChange = {},
            onHapticFeedbackChange = {},
            onBiometricLoginChange = {},
            onResetApp = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResetDialogPreview() {
    VettrTheme {
        Surface(color = VettrNavy) {
            ResetConfirmationDialog(
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
