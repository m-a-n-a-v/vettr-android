package com.vettr.android.feature.stockdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.EmptyStateView
import com.vettr.android.designsystem.component.LoadingView
import com.vettr.android.designsystem.component.SearchBarView
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBarView(
                query = searchQuery,
                onQueryChange = { viewModel.searchStocks(it) },
                modifier = Modifier.padding(Spacing.md)
            )

            // Content based on loading/empty/data states
            when {
                isLoading -> {
                    LoadingView(message = "Loading stocks...")
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
                                logoUrl = "https://logo.clearbit.com/${stock.ticker.lowercase()}.com",
                                onClick = { onStockClick(stock.ticker) }
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

@Preview(showBackground = true)
@Composable
fun StocksScreenPreview() {
    VettrTheme {
        StocksScreen()
    }
}
