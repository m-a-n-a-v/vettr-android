package com.vettr.android.feature.stockdetail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.EmptyStateView
import com.vettr.android.designsystem.component.LastUpdatedText
import com.vettr.android.designsystem.component.SearchBarView
import com.vettr.android.designsystem.component.SkeletonStockRow
import com.vettr.android.designsystem.component.StockRowView
import com.vettr.android.designsystem.component.vettrPadding
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Stocks screen - displays stock lists with search functionality.
 * Allows users to browse and search all stocks and navigate to stock details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StocksScreen(
    modifier: Modifier = Modifier,
    viewModel: StocksViewModel = hiltViewModel(),
    onStockClick: (String) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {}
) {
    val filteredStocks by viewModel.filteredStocks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val lastUpdatedAt by viewModel.lastUpdatedAt.collectAsStateWithLifecycle()
    val favoriteStocks by viewModel.favoriteStocks.collectAsStateWithLifecycle()
    val favoriteCount by viewModel.favoriteCount.collectAsStateWithLifecycle()
    val watchlistLimit by viewModel.watchlistLimit.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Stocks",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading && filteredStocks.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Last updated timestamp
                LastUpdatedText(
                    lastUpdatedAt = lastUpdatedAt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm)
                )

                // Search bar
                SearchBarView(
                    query = searchQuery,
                    onQueryChange = { viewModel.searchStocks(it) },
                    modifier = Modifier.padding(Spacing.md)
                )

                // My Watchlist Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md)
                ) {
                    // Section header
                    Text(
                        text = "My Watchlist",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )

                    // Watchlist count
                    val limitText = if (watchlistLimit == Int.MAX_VALUE) "Unlimited" else "$watchlistLimit"
                    Text(
                        text = "$favoriteCount / $limitText stocks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )

                    // Watchlist stocks or empty message
                    if (favoriteStocks.isEmpty()) {
                        Text(
                            text = "No stocks in your watchlist yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Spacing.md)
                        )
                    } else {
                        favoriteStocks.forEach { stock ->
                            WatchlistStockRow(
                                stock = stock,
                                onStockClick = { onStockClick(stock.id) },
                                onRemoveClick = { onToggleFavorite(stock.id) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                            )
                        }
                    }

                    // Divider between sections
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = Spacing.md)
                    )

                    // All Stocks header
                    Text(
                        text = "All Stocks",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )
                }

                // Use Crossfade to transition between skeleton and content
                Crossfade(
                    targetState = isLoading && filteredStocks.isEmpty(),
                    label = "stocksContentCrossfade",
                    modifier = Modifier.fillMaxSize()
                ) { showSkeleton ->
                    when {
                        showSkeleton -> {
                            // Skeleton loading state
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                items(8, key = { it }) {
                                    SkeletonStockRow()
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                        filteredStocks.isEmpty() && searchQuery.isNotBlank() -> {
                            EmptyStateView(
                                icon = Icons.Default.SearchOff,
                                title = "No Results Found",
                                subtitle = "Try adjusting your search query"
                            )
                        }
                        filteredStocks.isEmpty() -> {
                            EmptyStateView(
                                icon = Icons.Default.SearchOff,
                                title = "No Stocks Available",
                                subtitle = "Stocks will appear here once data is loaded"
                            )
                        }
                        else -> {
                            // Actual content
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                items(
                                    items = filteredStocks,
                                    key = { stock -> stock.id }
                                ) { stock ->
                                    StockRowView(
                                        ticker = stock.ticker,
                                        companyName = stock.name,
                                        price = stock.price,
                                        priceChange = stock.priceChange,
                                        onClick = { onStockClick(stock.id) }
                                    )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Watchlist stock row with remove button.
 */
@Composable
private fun WatchlistStockRow(
    stock: com.vettr.android.core.model.Stock,
    onStockClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onStockClick)
            .vettrPadding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ticker initials avatar
        val initials = stock.ticker.take(2)
        val avatarColors = listOf(
            com.vettr.android.designsystem.theme.VettrAccent,
            com.vettr.android.designsystem.theme.VettrGreen,
            androidx.compose.ui.graphics.Color(0xFF6366F1),
            androidx.compose.ui.graphics.Color(0xFFEC4899),
            androidx.compose.ui.graphics.Color(0xFFF59E0B),
            androidx.compose.ui.graphics.Color(0xFF8B5CF6),
            androidx.compose.ui.graphics.Color(0xFF14B8A6)
        )
        val avatarColor = avatarColors[stock.ticker.hashCode().and(0x7FFFFFFF) % avatarColors.size]
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(40.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(avatarColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                color = avatarColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Price and change
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = String.format("$%.2f", stock.price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            val isPositive = stock.priceChange >= 0
            val changeColor = if (isPositive) com.vettr.android.designsystem.theme.VettrGreen else com.vettr.android.designsystem.theme.VettrRed
            val changePrefix = if (isPositive) "+" else ""

            Text(
                text = "$changePrefix${String.format("$%.2f", stock.priceChange)}",
                style = MaterialTheme.typography.bodyMedium,
                color = changeColor
            )
        }

        // Remove button (filled star icon)
        androidx.compose.material3.IconButton(
            onClick = onRemoveClick,
            modifier = Modifier.size(40.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Remove from watchlist",
                tint = com.vettr.android.designsystem.theme.VettrAccent
            )
        }
    }
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun StocksScreenPreview() {
    VettrTheme {
        StocksScreen()
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun StocksScreenTabletPreview() {
    VettrTheme {
        StocksScreen()
    }
}
