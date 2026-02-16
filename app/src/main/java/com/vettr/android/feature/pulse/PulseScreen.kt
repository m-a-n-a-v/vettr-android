package com.vettr.android.feature.pulse

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.EmptyStateView
import com.vettr.android.designsystem.component.ErrorView
import com.vettr.android.designsystem.component.EventCard
import com.vettr.android.designsystem.component.LastUpdatedText
import com.vettr.android.designsystem.component.MetricCard
import com.vettr.android.designsystem.component.SectionHeader
import com.vettr.android.designsystem.component.SkeletonEventCard
import com.vettr.android.designsystem.component.SkeletonMetricCard
import com.vettr.android.designsystem.component.SkeletonStockRow
import com.vettr.android.designsystem.component.VettrScoreView
import com.vettr.android.designsystem.component.cardStyle
import com.vettr.android.designsystem.component.vettrPadding
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow
import com.vettr.android.core.model.Stock
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.IntrinsicSize

/**
 * Pulse screen - displays market overview and live data from stocks and filings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseScreen(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    onStockClick: (String) -> Unit = {},
    onNavigateToStocks: () -> Unit = {},
    onSeeAllFilings: () -> Unit = {},
    onSeeAllTopScores: () -> Unit = {},
    onSeeAllMovers: () -> Unit = {},
    viewModel: PulseViewModel = hiltViewModel()
) {
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val filings by viewModel.filings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val lastUpdatedAt by viewModel.lastUpdatedAt.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Compute market overview metrics from live stock data
    val stocksTracked by remember(stocks) {
        derivedStateOf { stocks.size }
    }
    val avgVetrScore by remember(stocks) {
        derivedStateOf {
            if (stocks.isEmpty()) 0.0
            else stocks.map { it.vetrScore }.average()
        }
    }
    val topGainer by remember(stocks) {
        derivedStateOf { stocks.maxByOrNull { it.priceChange } }
    }
    val topLoser by remember(stocks) {
        derivedStateOf { stocks.minByOrNull { it.priceChange } }
    }

    // Recent material filings (up to 5, sorted by date desc)
    val recentFilings by remember(filings) {
        derivedStateOf {
            filings.sortedByDescending { it.date }.take(5)
        }
    }

    // Top VETTR Scores (top 5 by vetrScore)
    val topVetrScores by remember(stocks) {
        derivedStateOf {
            stocks.sortedByDescending { it.vetrScore }.take(5)
        }
    }

    // Top Movers (top 5 by abs(priceChange))
    val topMovers by remember(stocks) {
        derivedStateOf {
            stocks.sortedByDescending { abs(it.priceChange) }.take(5)
        }
    }

    // Risk distribution based on VETR score thresholds
    val riskDistribution by remember(stocks) {
        derivedStateOf {
            if (stocks.isEmpty()) Triple(0, 0, 0)
            else {
                val low = stocks.count { it.vetrScore > 60 }
                val medium = stocks.count { it.vetrScore in 40..60 }
                val high = stocks.count { it.vetrScore < 40 }
                Triple(low, medium, high)
            }
        }
    }

    // Build a stock lookup map for filings
    val stockLookup by remember(stocks) {
        derivedStateOf { stocks.associateBy { it.id } }
    }

    // Show offline message when network is lost
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            snackbarHostState.showSnackbar(
                message = "No internet connection",
                withDismissAction = true
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = VettrRed,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
        }
    ) { paddingValues ->
        // Handle error state
        if (errorMessage != null) {
            ErrorView(
                message = errorMessage ?: "An error occurred",
                onRetry = { viewModel.refresh() },
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

        // Handle empty state (only when not loading)
        if (!isLoading && stocks.isEmpty()) {
            Box(modifier = Modifier.padding(paddingValues)) {
                EmptyStateView(
                    icon = Icons.Default.Star,
                    title = "Your Watchlist is Empty",
                    subtitle = "Add stocks to your watchlist to see personalized insights",
                    actionLabel = "Browse Stocks",
                    onAction = onNavigateToStocks
                )
            }
            return@Scaffold
        }

        // Main content with pull-to-refresh
        PullToRefreshBox(
            isRefreshing = isLoading && stocks.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Use Crossfade to transition between skeleton and content
            Crossfade(
                targetState = isLoading && stocks.isEmpty(),
                label = "pulseContentCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { showSkeleton ->
                if (showSkeleton) {
                    // Skeleton loading state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Market Overview Skeleton
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            SectionHeader(title = "Market Overview")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SkeletonMetricCard(modifier = Modifier.weight(1f))
                                SkeletonMetricCard(modifier = Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SkeletonMetricCard(modifier = Modifier.weight(1f))
                                SkeletonMetricCard(modifier = Modifier.weight(1f))
                            }
                        }

                        // Recent Events Skeleton
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            SectionHeader(title = "Recent Events")
                            repeat(3) {
                                SkeletonEventCard()
                            }
                        }

                        // Top VETTR Scores Skeleton
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            SectionHeader(title = "Top VETTR Scores")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                contentPadding = PaddingValues(horizontal = 0.dp)
                            ) {
                                items(5, key = { it }) {
                                    SkeletonMetricCard(modifier = Modifier.width(140.dp))
                                }
                            }
                        }

                        // Top Movers Skeleton
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            SectionHeader(title = "Top Movers")
                            repeat(3) {
                                SkeletonStockRow()
                            }
                        }
                    }
                } else {
                    // Actual content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Last updated timestamp
                        LastUpdatedText(
                            lastUpdatedAt = lastUpdatedAt,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // ── Market Overview Section ──
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            SectionHeader(title = "Market Overview")

                            // Risk Distribution Bar
                            RiskDistributionBar(
                                lowCount = riskDistribution.first,
                                mediumCount = riskDistribution.second,
                                highCount = riskDistribution.third,
                                total = stocksTracked
                            )

                            // Top Gainer + Top Loser
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                MetricCard(
                                    title = "Top Gainer",
                                    value = topGainer?.ticker ?: "--",
                                    change = topGainer?.priceChange,
                                    modifier = Modifier.weight(1f)
                                )

                                MetricCard(
                                    title = "Top Loser",
                                    value = topLoser?.ticker ?: "--",
                                    change = topLoser?.priceChange,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // ── Recent Events Section (up to 5 material filings) ──
                        if (recentFilings.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SectionHeader(
                                    title = "Recent Events",
                                    onSeeAllClick = onSeeAllFilings
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    recentFilings.forEach { filing ->
                                        val stock = stockLookup[filing.stockId]
                                        val tickerLabel = stock?.ticker ?: ""

                                        // Color by filing type
                                        val indicatorColor = when {
                                            filing.type.contains("MD&A", ignoreCase = true) -> VettrYellow
                                            filing.type.contains("Press", ignoreCase = true) -> VettrGreen
                                            filing.type.contains("Financial", ignoreCase = true) -> VettrAccent
                                            filing.isMaterial -> VettrRed
                                            else -> VettrYellow
                                        }

                                        // Format date as relative time
                                        val relativeDate = formatFilingDate(filing.date)

                                        EventCard(
                                            title = filing.title,
                                            subtitle = if (tickerLabel.isNotEmpty()) "$tickerLabel - ${filing.type}" else filing.type,
                                            date = relativeDate,
                                            indicatorColor = indicatorColor,
                                            onClick = { stock?.let { onStockClick(it.id) } }
                                        )
                                    }
                                }
                            }
                        }

                        // ── Top VETTR Scores Section (horizontal scroll, top 5) ──
                        if (topVetrScores.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SectionHeader(
                                    title = "Top VETTR Scores",
                                    onSeeAllClick = onSeeAllTopScores
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                    contentPadding = PaddingValues(horizontal = 0.dp)
                                ) {
                                    items(topVetrScores, key = { it.id }) { stock ->
                                        VetrScoreStockCard(
                                            stock = stock,
                                            onClick = { onStockClick(stock.id) }
                                        )
                                    }
                                }
                            }
                        }

                        // ── Top Movers Section (top 5 by abs(priceChange)) ──
                        if (topMovers.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SectionHeader(
                                    title = "Top Movers",
                                    onSeeAllClick = onSeeAllMovers
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    topMovers.forEach { stock ->
                                        val directionIcon = if (stock.priceChange >= 0) "\u25B2" else "\u25BC"
                                        val changeColor = if (stock.priceChange >= 0) VettrGreen else VettrRed

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .cardStyle()
                                                .clickable { onStockClick(stock.id) }
                                                .vettrPadding(),
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Avatar circle with 2-letter abbreviation
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(changeColor.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = directionIcon,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = changeColor
                                                )
                                            }

                                            // Ticker and company name
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                                            ) {
                                                Text(
                                                    text = stock.ticker,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = stock.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Price change percentage
                                            Column(
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = String.format("$%.2f", stock.price),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${if (stock.priceChange >= 0) "+" else ""}${String.format("%.2f", stock.priceChange)}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = changeColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom spacing
                        Spacer(modifier = Modifier.height(Spacing.md))
                    }
                }
            }
        }
    }
}

/**
 * Risk distribution bar showing Low / Medium / High risk breakdown.
 */
@Composable
private fun RiskDistributionBar(
    lowCount: Int,
    mediumCount: Int,
    highCount: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val lowPct = if (total > 0) (lowCount.toFloat() / total * 100).roundToInt() else 0
    val medPct = if (total > 0) (mediumCount.toFloat() / total * 100).roundToInt() else 0
    val highPct = if (total > 0) (highCount.toFloat() / total * 100).roundToInt() else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardStyle()
            .vettrPadding(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Segmented bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (lowCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(lowCount.toFloat())
                        .height(28.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                        .background(VettrGreen)
                )
            }
            if (mediumCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(mediumCount.toFloat())
                        .height(28.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                        .background(VettrYellow)
                )
            }
            if (highCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(highCount.toFloat())
                        .height(28.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                        .background(VettrRed)
                )
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RiskLegendItem(color = VettrGreen, label = "Low Risk ($lowPct%)", count = lowCount)
            RiskLegendItem(color = VettrYellow, label = "Medium Risk ($medPct%)", count = mediumCount)
            RiskLegendItem(color = VettrRed, label = "High Risk ($highPct%)", count = highCount)
        }
    }
}

/**
 * Legend item for a risk category.
 */
@Composable
private fun RiskLegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    count: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$count stocks",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/**
 * Card displaying a stock with its VETTR score in horizontal scroll.
 */
@Composable
private fun VetrScoreStockCard(
    stock: Stock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .cardStyle()
            .clickable(onClick = onClick)
            .vettrPadding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        VettrScoreView(
            score = stock.vetrScore,
            size = 56.dp
        )
        Text(
            text = stock.ticker,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stock.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Format filing date (epoch millis) to relative time string.
 */
private fun formatFilingDate(dateMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - dateMillis

    return when {
        diff < 0 -> "just now"
        diff < 60_000 -> "just now"
        diff < 3600_000 -> {
            val minutes = diff / 60_000
            "$minutes min${if (minutes == 1L) "" else "s"} ago"
        }
        diff < 86400_000 -> {
            val hours = diff / 3600_000
            "$hours hour${if (hours == 1L) "" else "s"} ago"
        }
        diff < 604800_000 -> {
            val days = diff / 86400_000
            "$days day${if (days == 1L) "" else "s"} ago"
        }
        else -> {
            val weeks = diff / 604800_000
            "$weeks week${if (weeks == 1L) "" else "s"} ago"
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun PulseScreenPreview() {
    VettrTheme {
        PulseScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun PulseScreenTabletPreview() {
    VettrTheme {
        PulseScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(840.dp, 1200.dp))
        )
    }
}
