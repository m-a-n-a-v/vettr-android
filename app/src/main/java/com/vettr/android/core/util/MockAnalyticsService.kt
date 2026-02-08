package com.vettr.android.core.util

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of AnalyticsService that logs events to Logcat.
 * Used in debug builds for analytics verification without external dependencies.
 * Privacy-first: sanitizes PII from params before logging.
 */
@Singleton
class MockAnalyticsService @Inject constructor() : AnalyticsService {

    companion object {
        private const val TAG = "Analytics"

        // PII keywords that should be sanitized from logs
        private val PII_KEYWORDS = setOf(
            "email",
            "password",
            "phone",
            "address",
            "ssn",
            "credit_card",
            "name",
            "first_name",
            "last_name",
            "full_name"
        )
    }

    private var currentUserId: String? = null

    override fun trackScreen(name: String) {
        Log.d(TAG, "📱 Screen Viewed: $name")
    }

    override fun trackEvent(name: String, params: Map<String, Any>) {
        val sanitizedParams = sanitizeParams(params)
        val paramsString = if (sanitizedParams.isEmpty()) {
            ""
        } else {
            " | Params: ${sanitizedParams.entries.joinToString { "${it.key}=${it.value}" }}"
        }

        Log.d(TAG, "🎯 Event: $name$paramsString")
    }

    override fun setUserId(id: String) {
        currentUserId = id
        Log.d(TAG, "👤 User ID Set: $id")
    }

    override fun setUserProperty(key: String, value: String) {
        val sanitizedKey = sanitizeKey(key)
        val sanitizedValue = if (isPIIKey(key)) "[REDACTED]" else value

        Log.d(TAG, "🏷️  User Property: $sanitizedKey = $sanitizedValue")
    }

    /**
     * Sanitize params to remove or redact PII.
     */
    private fun sanitizeParams(params: Map<String, Any>): Map<String, Any> {
        return params.mapValues { (key, value) ->
            if (isPIIKey(key)) {
                "[REDACTED]"
            } else {
                value
            }
        }
    }

    /**
     * Sanitize a parameter key by checking against PII keywords.
     */
    private fun sanitizeKey(key: String): String {
        return if (isPIIKey(key)) "[REDACTED_KEY]" else key
    }

    /**
     * Check if a key contains PII-related keywords.
     */
    private fun isPIIKey(key: String): Boolean {
        val lowerKey = key.lowercase()
        return PII_KEYWORDS.any { piiKeyword ->
            lowerKey.contains(piiKeyword)
        }
    }
}
