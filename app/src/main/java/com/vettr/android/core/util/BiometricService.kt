package com.vettr.android.core.util

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
 * - Tracks failure count with persistent EncryptedSharedPreferences (survives app restarts)
 * - Enforces 15-minute lockout after 3 failed attempts
 * - Integrates with SettingsRepository for enable/disable state
 */
@Singleton
class BiometricService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val PREFS_NAME = "biometric_security"
        private const val KEY_FAILURE_COUNT = "failure_count"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until_ms"
        private const val MAX_FAILURES = 3
        private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutes
    }

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ── Persistent failure tracking ────────────────────────────────────────────

    private fun getFailureCount(): Int = prefs.getInt(KEY_FAILURE_COUNT, 0)

    private fun setFailureCount(count: Int) {
        prefs.edit().putInt(KEY_FAILURE_COUNT, count).apply()
    }

    private fun getLockoutUntil(): Long = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)

    private fun setLockoutUntil(timestampMs: Long) {
        prefs.edit().putLong(KEY_LOCKOUT_UNTIL, timestampMs).apply()
    }

    /**
     * Returns true if the account is currently locked out due to too many failed attempts.
     */
    fun isLockedOut(): Boolean {
        val lockoutUntil = getLockoutUntil()
        if (lockoutUntil == 0L) return false
        return System.currentTimeMillis() < lockoutUntil
    }

    /**
     * Returns the remaining lockout duration in milliseconds, or 0 if not locked out.
     */
    fun remainingLockoutMs(): Long {
        val lockoutUntil = getLockoutUntil()
        if (lockoutUntil == 0L) return 0L
        return maxOf(0L, lockoutUntil - System.currentTimeMillis())
    }

    private fun recordFailure(): Boolean {
        val newCount = getFailureCount() + 1
        setFailureCount(newCount)
        if (newCount >= MAX_FAILURES) {
            setLockoutUntil(System.currentTimeMillis() + LOCKOUT_DURATION_MS)
            setFailureCount(0)
            return true // locked out
        }
        return false
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
     * Refuses to show the prompt if the account is currently locked out.
     *
     * @param activity FragmentActivity required for BiometricPrompt
     * @param onSuccess Callback invoked when authentication succeeds
     * @param onError Callback invoked when authentication fails (with error message)
     * @param onFallbackToPassword Callback invoked after MAX_FAILURES failures to fallback to password
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFallbackToPassword: () -> Unit
    ) {
        // Enforce persistent lockout before showing the prompt
        if (isLockedOut()) {
            val remainingMinutes = (remainingLockoutMs() / 60_000) + 1
            onError("Too many failed attempts. Please wait ${remainingMinutes} minute(s) before trying again.")
            onFallbackToPassword()
            return
        }

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
                    setFailureCount(0)
                    setLockoutUntil(0L)
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
                    val lockedOut = recordFailure()
                    if (lockedOut) {
                        onError("Too many failed attempts. Locked out for 15 minutes.")
                        onFallbackToPassword()
                    } else {
                        val remaining = MAX_FAILURES - getFailureCount()
                        onError("Authentication failed. $remaining attempt(s) remaining.")
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
     * Resets the failure count and lockout (useful when user successfully authenticates via password).
     */
    fun resetFailureCount() {
        setFailureCount(0)
        setLockoutUntil(0L)
    }
}

/**
 * Result of biometric availability check.
 */
sealed class BiometricCheckResult {
    object Available : BiometricCheckResult()
    data class Unavailable(val reason: String) : BiometricCheckResult()
}
