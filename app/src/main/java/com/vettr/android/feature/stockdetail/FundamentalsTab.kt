package com.vettr.android.feature.stockdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vettr.android.core.data.remote.FundamentalsResponse
import com.vettr.android.designsystem.component.SectionHeader
import com.vettr.android.designsystem.component.cardStyle
import com.vettr.android.designsystem.component.vettrPadding
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrSurfaceVariant
import com.vettr.android.designsystem.theme.VettrTextSecondary

/**
 * Fundamentals tab content for Stock Detail screen.
 * Shows valuation, earnings, analyst consensus, short interest, institutional holders, etc.
 */
@Composable
fun FundamentalsTab(
    fundamentals: FundamentalsResponse?,
    modifier: Modifier = Modifier
) {
    if (fundamentals == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Fundamentals data not available",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Valuation
        fundamentals.valuation?.let { val_ ->
            SectionHeader(title = "Valuation")
            Column(
                modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FundamentalRow("Market Cap", formatLargeNumber(val_.marketCap))
                FundamentalRow("Enterprise Value", formatLargeNumber(val_.enterpriseValue))
                FundamentalRow("P/E Ratio", formatDouble(val_.peRatio))
                FundamentalRow("Forward P/E", formatDouble(val_.forwardPe))
                FundamentalRow("P/B Ratio", formatDouble(val_.pbRatio))
                FundamentalRow("EV/EBITDA", formatDouble(val_.evEbitda))
            }
        }

        // Earnings
        fundamentals.earnings?.let { earn ->
            SectionHeader(title = "Earnings")
            Column(
                modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FundamentalRow("EPS", formatDouble(earn.eps))
                FundamentalRow("Revenue", formatLargeNumber(earn.revenue))
                FundamentalRow("Net Income", formatLargeNumber(earn.netIncome))
                FundamentalRow("Profit Margin", formatPercent(earn.profitMargin))
                FundamentalRow("Operating Margin", formatPercent(earn.operatingMargin))
            }
        }

        // Analyst Consensus
        fundamentals.analystConsensus?.let { analyst ->
            SectionHeader(title = "Analyst Consensus")
            Column(
                modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FundamentalRow("Rating", analyst.rating)
                FundamentalRow("Target Price", "$${formatDouble(analyst.targetPrice)}")
                FundamentalRow("# Analysts", "${analyst.numberOfAnalysts}")
                // Buy/Hold/Sell distribution
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    RatingBox(label = "Buy", count = analyst.buy, color = VettrGreen, modifier = Modifier.weight(1f))
                    RatingBox(label = "Hold", count = analyst.hold, color = VettrAccent, modifier = Modifier.weight(1f))
                    RatingBox(label = "Sell", count = analyst.sell, color = VettrRed, modifier = Modifier.weight(1f))
                }
            }
        }

        // Short Interest
        fundamentals.shortInterest?.let { si ->
            SectionHeader(title = "Short Interest")
            Column(
                modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FundamentalRow("Short Ratio", formatDouble(si.shortRatio))
                FundamentalRow("Short % Float", formatPercent(si.shortPercentFloat))
                FundamentalRow("Shares Short", formatLargeNumber(si.sharesShort?.toDouble()))
            }
        }

        // Institutional Holders
        if (fundamentals.institutionalHolders.isNotEmpty()) {
            SectionHeader(title = "Top Institutional Holders")
            Column(
                modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                fundamentals.institutionalHolders.take(5).forEach { holder ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = holder.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatPercent(holder.pctHeld),
                            style = MaterialTheme.typography.bodySmall,
                            color = VettrAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Insider Data
        fundamentals.insiderData?.let { insider ->
            SectionHeader(title = "Insider Activity")
            Column(
                modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FundamentalRow("Insider Ownership", formatPercent(insider.insiderOwnership))
                FundamentalRow("Net Transactions (3M)", "${insider.netTransactions3m}")
                FundamentalRow("Buy Transactions", "${insider.buyTransactions}")
                FundamentalRow("Sell Transactions", "${insider.sellTransactions}")
            }
        }

        // Dividend
        fundamentals.dividend?.let { div ->
            SectionHeader(title = "Dividend")
            Column(
                modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FundamentalRow("Yield", formatPercent(div.yield))
                FundamentalRow("Annual Dividend", "$${formatDouble(div.annualDividend)}")
                FundamentalRow("Payout Ratio", formatPercent(div.payoutRatio))
                div.exDividendDate?.let { FundamentalRow("Ex-Dividend Date", it.take(10)) }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@Composable
private fun FundamentalRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RatingBox(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun formatDouble(value: Double?): String {
    if (value == null) return "N/A"
    return String.format("%.2f", value)
}

private fun formatPercent(value: Double?): String {
    if (value == null) return "N/A"
    return "${String.format("%.1f", value * 100)}%"
}

private fun formatLargeNumber(value: Double?): String {
    if (value == null) return "N/A"
    val abs = kotlin.math.abs(value)
    val prefix = if (value < 0) "-" else ""
    return when {
        abs >= 1_000_000_000_000 -> "${prefix}$${String.format("%.1f", abs / 1_000_000_000_000)}T"
        abs >= 1_000_000_000 -> "${prefix}$${String.format("%.1f", abs / 1_000_000_000)}B"
        abs >= 1_000_000 -> "${prefix}$${String.format("%.0f", abs / 1_000_000)}M"
        abs >= 1_000 -> "${prefix}$${String.format("%.0f", abs / 1_000)}K"
        else -> "${prefix}$${String.format("%.0f", abs)}"
    }
}
