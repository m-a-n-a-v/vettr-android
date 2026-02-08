package com.vettr.android.core.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.vettr.android.core.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling biometric authentication using BiometricPrompt API.
 * Supports fingerprint, face, and iris authentication with BIOMETRIC_STRONG security.
 *
 * Features:
 * - Checks for biometric hardware availability
 * - Shows biometric authentication prompt
 * - Tracks failure count with fallback to password after 3 failures
 * - Integrates with SettingsRepository for enable/disable state
 */
@Singleton
class BiometricService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val MAX_BIOMETRIC_FAILURES = 3
        private var failureCount = 0
    }

    /**
     * Checks if biometric authentication is available on this device.
     * Returns BiometricCheckResult with availability status and reason.
     */
    fun isBiometricAvailable(): BiometricCheckResult {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                BiometricCheckResult.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricCheckResult.Unavailable("No biometric hardware detected")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricCheckResult.Unavailable("Biometric hardware is currently unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricCheckResult.Unavailable("No biometrics enrolled. Please set up fingerprint or face recognition in device settings")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricCheckResult.Unavailable("Security update required for biometric authentication")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                BiometricCheckResult.Unavailable("Biometric authentication is not supported on this device")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                BiometricCheckResult.Unavailable("Biometric status unknown")
            else ->
                BiometricCheckResult.Unavailable("Biometric authentication is not available")
        }
    }

    /**
     * Shows biometric authentication prompt.
     * @param activity FragmentActivity required for BiometricPrompt
     * @param onSuccess Callback invoked when authentication succeeds
     * @param onError Callback invoked when authentication fails (with error message)
     * @param onFallbackToPassword Callback invoked after 3 failures to fallback to password
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFallbackToPassword: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock VETTR")
            .setSubtitle("Use your fingerprint or face to unlock")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Use Password")
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Reset failure count on success
                    failureCount = 0
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // User clicked "Use Password"
                            onFallbackToPassword()
                        }
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> {
                            // User canceled - don't call error callback
                        }
                        else -> {
                            onError(errString.toString())
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    failureCount++

                    if (failureCount >= MAX_BIOMETRIC_FAILURES) {
                        failureCount = 0
                        onError("Too many failed attempts. Please use password to unlock.")
                        onFallbackToPassword()
                    } else {
                        onError("Authentication failed. Try again.")
                    }
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Checks if biometric login is enabled in settings.
     */
    suspend fun isBiometricLoginEnabled(): Boolean {
        return settingsRepository.biometricLoginEnabled.first()
    }

    /**
     * Resets the failure count (useful when user successfully authenticates via password).
     */
    fun resetFailureCount() {
        failureCount = 0
    }
}

/**
 * Result of biometric availability check.
 */
sealed class BiometricCheckResult {
    object Available : BiometricCheckResult()
    data class Unavailable(val reason: String) : BiometricCheckResult()
}
