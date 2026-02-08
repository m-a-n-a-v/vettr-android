package com.vettr.android.core.util

/**
 * Sealed class hierarchy representing all analytics events in the VETTR app.
 * Privacy-first: no PII (email, password, names) should be tracked.
 */
sealed class AnalyticsEvent(
    open val name: String,
    open val params: Map<String, Any> = emptyMap()
) {
    // Screen Events
    data class ScreenViewed(val screenName: String) : AnalyticsEvent(
        name = "screen_viewed",
        params = mapOf("screen_name" to screenName)
    )

    // Auth Events
    data object SignInStarted : AnalyticsEvent("sign_in_started")
    data object SignInCompleted : AnalyticsEvent("sign_in_completed")
    data object SignInFailed : AnalyticsEvent("sign_in_failed")
    data object SignOutCompleted : AnalyticsEvent("sign_out_completed")
    data object BiometricAuthEnabled : AnalyticsEvent("biometric_auth_enabled")
    data object BiometricAuthDisabled : AnalyticsEvent("biometric_auth_disabled")
    data object BiometricAuthSuccess : AnalyticsEvent("biometric_auth_success")
    data object BiometricAuthFailed : AnalyticsEvent("biometric_auth_failed")

    // Stock Events
    data class StockViewed(val ticker: String, val stockId: String) : AnalyticsEvent(
        name = "stock_viewed",
        params = mapOf("ticker" to ticker, "stock_id" to stockId)
    )
    data class StockAddedToWatchlist(val ticker: String, val stockId: String) : AnalyticsEvent(
        name = "stock_added_to_watchlist",
        params = mapOf("ticker" to ticker, "stock_id" to stockId)
    )
    data class StockRemovedFromWatchlist(val ticker: String, val stockId: String) : AnalyticsEvent(
        name = "stock_removed_from_watchlist",
        params = mapOf("ticker" to ticker, "stock_id" to stockId)
    )
    data class StockFavorited(val ticker: String, val stockId: String) : AnalyticsEvent(
        name = "stock_favorited",
        params = mapOf("ticker" to ticker, "stock_id" to stockId)
    )
    data class StockUnfavorited(val ticker: String, val stockId: String) : AnalyticsEvent(
        name = "stock_unfavorited",
        params = mapOf("ticker" to ticker, "stock_id" to stockId)
    )
    data class StockShared(val ticker: String, val stockId: String) : AnalyticsEvent(
        name = "stock_shared",
        params = mapOf("ticker" to ticker, "stock_id" to stockId)
    )

    // Alert Events
    data class AlertCreated(val ruleType: String) : AnalyticsEvent(
        name = "alert_created",
        params = mapOf("rule_type" to ruleType)
    )
    data class AlertDeleted(val alertId: String) : AnalyticsEvent(
        name = "alert_deleted",
        params = mapOf("alert_id" to alertId)
    )
    data class AlertTriggered(val alertId: String, val ruleType: String) : AnalyticsEvent(
        name = "alert_triggered",
        params = mapOf("alert_id" to alertId, "rule_type" to ruleType)
    )
    data class NotificationOpened(val alertId: String) : AnalyticsEvent(
        name = "notification_opened",
        params = mapOf("alert_id" to alertId)
    )
    data class AlertShared(val alertId: String, val ruleType: String) : AnalyticsEvent(
        name = "alert_shared",
        params = mapOf("alert_id" to alertId, "rule_type" to ruleType)
    )

    // Search & Discovery Events
    data class SearchPerformed(val query: String, val resultCount: Int) : AnalyticsEvent(
        name = "search_performed",
        params = mapOf("query" to query, "result_count" to resultCount)
    )
    data class FilterApplied(val filterType: String, val filterValue: String) : AnalyticsEvent(
        name = "filter_applied",
        params = mapOf("filter_type" to filterType, "filter_value" to filterValue)
    )
    data class SortApplied(val sortBy: String) : AnalyticsEvent(
        name = "sort_applied",
        params = mapOf("sort_by" to sortBy)
    )

    // Filing Events
    data class FilingViewed(val filingId: String, val filingType: String) : AnalyticsEvent(
        name = "filing_viewed",
        params = mapOf("filing_id" to filingId, "filing_type" to filingType)
    )
    data class FilingShared(val filingId: String, val filingType: String) : AnalyticsEvent(
        name = "filing_shared",
        params = mapOf("filing_id" to filingId, "filing_type" to filingType)
    )

    // Executive/Pedigree Events
    data class ExecutiveViewed(val executiveId: String) : AnalyticsEvent(
        name = "executive_viewed",
        params = mapOf("executive_id" to executiveId)
    )
    data class PedigreeTabViewed(val tabName: String) : AnalyticsEvent(
        name = "pedigree_tab_viewed",
        params = mapOf("tab_name" to tabName)
    )
    data class ExecutiveShared(val executiveId: String) : AnalyticsEvent(
        name = "executive_shared",
        params = mapOf("executive_id" to executiveId)
    )

    // Sync Events
    data object SyncStarted : AnalyticsEvent("sync_started")
    data object SyncCompleted : AnalyticsEvent("sync_completed")
    data class SyncFailed(val error: String) : AnalyticsEvent(
        name = "sync_failed",
        params = mapOf("error" to error)
    )
    data class ConflictResolved(val conflictType: String) : AnalyticsEvent(
        name = "conflict_resolved",
        params = mapOf("conflict_type" to conflictType)
    )

    // Settings Events
    data class SettingChanged(val settingName: String, val settingValue: String) : AnalyticsEvent(
        name = "setting_changed",
        params = mapOf("setting_name" to settingName, "setting_value" to settingValue)
    )
    data object DarkModeToggled : AnalyticsEvent("dark_mode_toggled")
    data object NotificationsEnabled : AnalyticsEvent("notifications_enabled")
    data object NotificationsDisabled : AnalyticsEvent("notifications_disabled")

    // Error Events
    data class ErrorOccurred(val errorType: String, val errorMessage: String) : AnalyticsEvent(
        name = "error_occurred",
        params = mapOf("error_type" to errorType, "error_message" to errorMessage)
    )

    // Feedback Events
    data class FeedbackSubmitted(val category: String, val hasEmail: Boolean, val submissionCount: Int) : AnalyticsEvent(
        name = "feedback_submitted",
        params = mapOf("category" to category, "has_email" to hasEmail, "submission_count" to submissionCount)
    )
    data object PlayStoreReviewRequested : AnalyticsEvent("play_store_review_requested")
    data object PlayStoreReviewCompleted : AnalyticsEvent("play_store_review_completed")
}
