package com.vettr.android.feature.discovery

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.EmptyStateView
import com.vettr.android.designsystem.component.ErrorView
import com.vettr.android.designsystem.component.LastUpdatedText
import com.vettr.android.designsystem.component.SearchBarView
import com.vettr.android.designsystem.component.SectionHeader
import com.vettr.android.designsystem.component.SectorChip
import com.vettr.android.designsystem.component.SkeletonEventCard
import com.vettr.android.designsystem.component.SkeletonMetricCard
import com.vettr.android.designsystem.component.VettrScoreView
import com.vettr.android.designsystem.component.cardStyle
import com.vettr.android.designsystem.component.vettrPadding
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock

/**
 * Discovery screen - displays stock discovery with search, sector filters,
 * featured stocks, and recent filings from live API data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    onStockClick: (String) -> Unit = {},
    viewModel: DiscoveryViewModel = hiltViewModel()
) {
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val filings by viewModel.filings.collectAsStateWithLifecycle()
    val sectors by viewModel.sectors.collectAsStateWithLifecycle()
    val selectedSector by viewModel.selectedSector.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val lastUpdatedAt by viewModel.lastUpdatedAt.collectAsStateWithLifecycle()

    // Build stock lookup for filing display
    val stockLookup by remember(stocks) {
        derivedStateOf { stocks.associateBy { it.id } }
    }

    // All stocks for lookup (need unfiltered for filing ticker resolution)
    // We use the filtered stocks for display, but we need the full stock map for filings.
    // The viewModel exposes filtered data, so stockLookup from filtered is fine for what's shown.

    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        // Handle error state
        if (errorMessage != null && stocks.isEmpty()) {
            ErrorView(
                message = errorMessage ?: "An error occurred",
                onRetry = { viewModel.refresh() },
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = isLoading && stocks.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Use Crossfade to transition between skeleton and content
            Crossfade(
                targetState = isLoading && stocks.isEmpty() && sectors.isEmpty(),
                label = "discoveryContentCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { showSkeleton ->
                if (showSkeleton) {
                    // Skeleton loading state
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Search bar skeleton
                        item {
                            SkeletonMetricCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            )
                        }

                        // Sector chips skeleton
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                repeat(4) {
                                    SkeletonMetricCard(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(36.dp)
                                    )
                                }
                            }
                        }

                        // Featured Stocks skeleton
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                SectionHeader(title = "Featured Stocks")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                                ) {
                                    items(4, key = { it }) {
                                        SkeletonMetricCard(
                                            modifier = Modifier
                                                .width(160.dp)
                                                .height(200.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Recent Filings skeleton
                        item {
                            SectionHeader(title = "Recent Filings")
                        }
                        items(5, key = { it }) {
                            SkeletonEventCard()
                        }
                    }
                } else {
                    // Actual content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Last updated timestamp
                        item {
                            LastUpdatedText(
                                lastUpdatedAt = lastUpdatedAt,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // ── Search Bar ──
                        item {
                            SearchBarView(
                                query = searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // ── Sector Filter Chips ──
                        if (sectors.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    sectors.forEach { sector ->
                                        FilterChip(
                                            selected = selectedSector == sector,
                                            onClick = { viewModel.selectSector(sector) },
                                            label = {
                                                Text(
                                                    text = sector,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ── Featured Stocks Section (horizontal scroll, up to 8) ──
                        if (stocks.isNotEmpty()) {
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                                ) {
                                    SectionHeader(title = "Featured Stocks")

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                        contentPadding = PaddingValues(horizontal = 0.dp)
                                    ) {
                                        items(
                                            stocks.take(8),
                                            key = { it.id }
                                        ) { stock ->
                                            FeaturedStockCard(
                                                stock = stock,
                                                onClick = { onStockClick(stock.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Recent Filings Section (up to 10) ──
                        if (filings.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Recent Filings")
                            }

                            items(
                                filings,
                                key = { it.id }
                            ) { filing ->
                                FilingRow(
                                    filing = filing,
                                    stockTicker = stockLookup[filing.stockId]?.ticker,
                                    onClick = {
                                        stockLookup[filing.stockId]?.let { onStockClick(it.id) }
                                    }
                                )
                            }
                        }

                        // Empty state when filters produce no results
                        if (stocks.isEmpty() && filings.isEmpty() && !isLoading) {
                            item {
                                EmptyStateView(
                                    icon = Icons.Default.Inbox,
                                    title = "No Results",
                                    subtitle = "Try adjusting your search or filters."
                                )
                            }
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(Spacing.md))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Featured stock card for horizontal scroll section.
 * Compact layout: ticker + initials row, company name, price + change row, VETTR score.
 */
@Composable
private fun FeaturedStockCard(
    stock: Stock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .cardStyle()
            .clickable(onClick = onClick)
            .vettrPadding(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top row: initials avatar + ticker
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            val initials = stock.ticker.take(2)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(VettrAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stock.ticker,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        // Company name
        Text(
            text = stock.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Price + change in one row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = String.format("$%.2f", stock.price),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            val changeColor = if (stock.priceChange >= 0) VettrGreen else VettrRed
            val changePrefix = if (stock.priceChange >= 0) "+" else ""
            Text(
                text = "${changePrefix}${String.format("%.1f", stock.priceChange)}%",
                style = MaterialTheme.typography.labelSmall,
                color = changeColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        // VETTR Score - small badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = "VETTR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            VettrScoreView(
                score = stock.vetrScore,
                size = 28.dp
            )
        }
    }
}

/**
 * Filing row with type icon, title, stock ticker, date, and material flag.
 */
@Composable
private fun FilingRow(
    filing: Filing,
    stockTicker: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .cardStyle()
            .clickable(onClick = onClick)
            .vettrPadding(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon with colored background
        val (iconVector, iconBgColor) = when {
            filing.type.contains("MD&A", ignoreCase = true) -> Icons.Default.Description to VettrYellow
            filing.type.contains("Press", ignoreCase = true) -> Icons.Default.Newspaper to VettrGreen
            filing.type.contains("Financial", ignoreCase = true) -> Icons.Default.Article to VettrAccent
            else -> Icons.Default.Description to VettrYellow
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = filing.type,
                tint = iconBgColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Title, ticker, date
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = filing.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (stockTicker != null) {
                    Text(
                        text = stockTicker,
                        style = MaterialTheme.typography.labelMedium,
                        color = VettrAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = formatFilingDate(filing.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Material flag
        if (filing.isMaterial) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(VettrRed.copy(alpha = 0.2f))
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
            ) {
                Text(
                    text = "Material",
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
fun DiscoveryScreenPreview() {
    VettrTheme {
        DiscoveryScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun DiscoveryScreenTabletPreview() {
    VettrTheme {
        DiscoveryScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(840.dp, 1200.dp))
        )
    }
}
