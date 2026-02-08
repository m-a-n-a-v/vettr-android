package com.vettr.android.feature.pulse

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.EmptyStateView
import com.vettr.android.designsystem.component.ErrorView
import com.vettr.android.designsystem.component.EventCard
import com.vettr.android.designsystem.component.LoadingView
import com.vettr.android.designsystem.component.MetricCard
import com.vettr.android.designsystem.component.SearchBarView
import com.vettr.android.designsystem.component.SectionHeader
import com.vettr.android.designsystem.component.StockRowView
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow

/**
 * Pulse screen - displays market overview and strategic events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseScreen(
    modifier: Modifier = Modifier,
    onStockClick: (String) -> Unit = {},
    viewModel: PulseViewModel = hiltViewModel()
) {
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val filings by viewModel.filings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier
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

        // Handle loading state
        if (isLoading && stocks.isEmpty()) {
            LoadingView(
                message = "Loading market data...",
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

        // Handle empty state
        if (!isLoading && stocks.isEmpty()) {
            Box(modifier = Modifier.padding(paddingValues)) {
                EmptyStateView(
                    icon = Icons.Default.Inbox,
                    title = "No Market Data",
                    subtitle = "Unable to load market data. Pull down to refresh."
                )
            }
            return@Scaffold
        }

        // Main content with pull-to-refresh
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
            // Search bar with notification bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBarView(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { /* TODO: Navigate to notifications */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Market Overview Section
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                SectionHeader(title = "Market Overview")

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    item {
                        MetricCard(
                            title = "TSX",
                            value = "21,543.25",
                            change = 1.25,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    item {
                        MetricCard(
                            title = "S&P 500",
                            value = "4,783.45",
                            change = 0.85,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    item {
                        MetricCard(
                            title = "NASDAQ",
                            value = "15,095.14",
                            change = -0.45,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }

            // Strategic Events Section
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                SectionHeader(title = "Strategic Events")

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    EventCard(
                        title = "Discovery Drill Hit",
                        subtitle = "BBB.V - Significant gold discovery announced",
                        date = "2 hours ago",
                        indicatorColor = VettrGreen,
                        onClick = {}
                    )

                    EventCard(
                        title = "Red Flag Alert",
                        subtitle = "XYZ.TO - Unusual insider selling detected",
                        date = "4 hours ago",
                        indicatorColor = VettrRed,
                        onClick = {}
                    )

                    EventCard(
                        title = "New Financing",
                        subtitle = "ABC.V - $10M private placement completed",
                        date = "1 day ago",
                        indicatorColor = VettrYellow,
                        onClick = {}
                    )
                }
            }

            // Trending Stocks Section
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                SectionHeader(
                    title = "Trending Stocks",
                    onSeeAllClick = { /* TODO: Navigate to full stock list */ }
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(stocks.take(6)) { stock ->
                        StockRowView(
                            ticker = stock.ticker,
                            companyName = stock.name,
                            price = stock.price,
                            priceChange = stock.priceChange,
                            logoUrl = null, // TODO: Add logo URL when available
                            onClick = { onStockClick(stock.id) },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }

                // Placeholder for future sections
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun PulseScreenPreview() {
    VettrTheme {
        PulseScreen()
    }
}
