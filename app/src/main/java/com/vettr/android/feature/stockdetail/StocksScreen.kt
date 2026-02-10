package com.vettr.android.feature.stockdetail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.EmptyStateView
import com.vettr.android.designsystem.component.LastUpdatedText
import com.vettr.android.designsystem.component.SearchBarView
import com.vettr.android.designsystem.component.SkeletonStockRow
import com.vettr.android.designsystem.component.StockRowView
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
    onStockClick: (String) -> Unit = {}
) {
    val filteredStocks by viewModel.filteredStocks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val lastUpdatedAt by viewModel.lastUpdatedAt.collectAsStateWithLifecycle()

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
