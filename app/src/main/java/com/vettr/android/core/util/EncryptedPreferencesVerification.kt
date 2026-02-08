package com.vettr.android.core.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Verification file to ensure EncryptedSharedPreferences dependency compiles correctly.
 * This demonstrates the basic usage pattern for secure storage of authentication tokens.
 */
object EncryptedPreferencesVerification {

    /**
     * Creates an EncryptedSharedPreferences instance for secure token storage.
     * This will be used for storing OAuth tokens, API keys, and other sensitive data.
     */
    fun createEncryptedPreferences(context: Context): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "vettr_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
