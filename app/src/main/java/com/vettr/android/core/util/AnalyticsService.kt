package com.vettr.android.core.util

/**
 * Interface for analytics tracking in the VETTR app.
 * Privacy-first: implementations must sanitize PII and never log email/password data.
 */
interface AnalyticsService {
    /**
     * Track a screen view event.
     * @param name The name of the screen being viewed
     */
    fun trackScreen(name: String)

    /**
     * Track a custom event with optional parameters.
     * @param name The name of the event
     * @param params Optional map of parameters (sanitized for PII)
     */
    fun trackEvent(name: String, params: Map<String, Any> = emptyMap())

    /**
     * Track a predefined analytics event.
     * @param event The AnalyticsEvent to track
     */
    fun trackEvent(event: AnalyticsEvent) {
        trackEvent(event.name, event.params)
    }

    /**
     * Set the user ID for analytics tracking.
     * Note: Should be an anonymized/hashed ID, never raw email or PII.
     * @param id The anonymized user ID
     */
    fun setUserId(id: String)

    /**
     * Set a user property for analytics segmentation.
     * @param key The property key (e.g., "subscription_tier", "platform")
     * @param value The property value (sanitized for PII)
     */
    fun setUserProperty(key: String, value: String)
}
