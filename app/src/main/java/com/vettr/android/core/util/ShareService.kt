package com.vettr.android.core.util

import android.content.Context
import android.content.Intent
import com.vettr.android.core.model.AlertRule
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Stock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for generating formatted share text for stocks, executives, and alerts.
 * Creates shareable content with deep links for easy sharing via Intent.ACTION_SEND.
 */
@Singleton
class ShareService @Inject constructor(
    private val analyticsService: AnalyticsService
) {
    /**
     * Share a stock with formatted text and deep link.
     *
     * @param context Android context for launching the share intent.
     * @param stock The stock to share.
     */
    fun shareStock(context: Context, stock: Stock) {
        val shareText = buildString {
            append("${stock.ticker} - ${stock.name}\n")
            append("VETR Score: ${stock.vetrScore}/100\n")
            append("Exchange: ${stock.exchange}\n")
            append("Sector: ${stock.sector}\n")
            append("\nView more on VETTR: https://vettr.com/stocks/${stock.ticker}")
        }

        launchShareIntent(context, shareText)

        // Track analytics
        analyticsService.trackEvent(
            AnalyticsEvent.StockShared(
                ticker = stock.ticker,
                stockId = stock.id
            )
        )
    }

    /**
     * Share an executive with formatted text and deep link.
     *
     * @param context Android context for launching the share intent.
     * @param executive The executive to share.
     * @param stockTicker The ticker of the associated stock.
     */
    fun shareExecutive(context: Context, executive: Executive, stockTicker: String) {
        val shareText = buildString {
            append("${executive.name} - ${executive.title}\n")
            append("Tenure: ${String.format("%.1f", executive.yearsAtCompany)} years\n")
            append("Education: ${executive.education}\n")
            if (executive.specialization.isNotBlank()) {
                append("Specialization: ${executive.specialization}\n")
            }
            append("\nView executive profile on VETTR: https://vettr.com/stocks/$stockTicker/executives/${executive.id}")
        }

        launchShareIntent(context, shareText)

        // Track analytics
        analyticsService.trackEvent(
            AnalyticsEvent.ExecutiveShared(
                executiveId = executive.id
            )
        )
    }

    /**
     * Share an alert rule with formatted text and deep link.
     *
     * @param context Android context for launching the share intent.
     * @param alertRule The alert rule to share.
     */
    fun shareAlert(context: Context, alertRule: AlertRule) {
        val shareText = buildString {
            append("VETTR Alert: ${alertRule.stockTicker}\n")
            append("${alertRule.triggerCondition}\n")
            append("Frequency: ${alertRule.frequency}\n")

            alertRule.lastTriggeredAt?.let { timestamp ->
                val timeAgo = formatTimestamp(timestamp)
                append("Last triggered: $timeAgo\n")
            }

            append("\nCreate your own alerts on VETTR: https://vettr.com/alerts")
        }

        launchShareIntent(context, shareText)

        // Track analytics
        analyticsService.trackEvent(
            AnalyticsEvent.AlertShared(
                alertId = alertRule.id,
                ruleType = alertRule.ruleType
            )
        )
    }

    /**
     * Launch the Android share sheet with the provided text.
     *
     * @param context Android context for launching the intent.
     * @param text The text to share.
     */
    private fun launchShareIntent(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    /**
     * Format timestamp to relative time.
     */
    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> {
                val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                dateFormat.format(java.util.Date(timestamp))
            }
        }
    }
}
