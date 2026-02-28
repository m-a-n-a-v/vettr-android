package com.vettr.android.feature.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.data.remote.SamplePortfolioStockDto
import com.vettr.android.designsystem.component.VettrScoreView
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrSurfaceVariant
import com.vettr.android.designsystem.theme.VettrTextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SamplePortfolioDashboardScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onStockClick: (String) -> Unit = {},
    viewModel: SamplePortfolioDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.portfolio?.name ?: "Sample Portfolio") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VettrNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VettrAccent)
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "An error occurred",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            uiState.portfolio != null -> {
                val portfolio = uiState.portfolio!!
                val stocks = portfolio.stocks
                val avgScore = if (stocks.isNotEmpty()) stocks.map { it.vetrScore }.average() else 0.0
                val totalValue = stocks.sumOf { it.price }
                val sectors = stocks.groupBy { it.sector }.mapValues { it.value.size }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // Description
                    item {
                        Text(
                            text = portfolio.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VettrTextSecondary
                        )
                    }

                    // Summary stats in 2x2 grid
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            maxItemsInEachRow = 2
                        ) {
                            SampleStatCard(
                                title = "Stocks",
                                value = "${stocks.size}",
                                modifier = Modifier.weight(1f)
                            )
                            SampleStatCard(
                                title = "Avg VETTR Score",
                                value = String.format("%.1f", avgScore),
                                modifier = Modifier.weight(1f)
                            )
                            SampleStatCard(
                                title = "Sectors",
                                value = "${sectors.size}",
                                modifier = Modifier.weight(1f)
                            )
                            SampleStatCard(
                                title = "Total Price",
                                value = "$${String.format("%.2f", totalValue)}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sector breakdown
                    if (sectors.isNotEmpty()) {
                        item {
                            Text(
                                text = "Sector Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        item {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                sectors.forEach { (sector, count) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(VettrAccent.copy(alpha = 0.1f))
                                            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                                    ) {
                                        Text(
                                            text = "$sector ($count)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = VettrAccent,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Holdings
                    item {
                        Text(
                            text = "Holdings",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(stocks) { stock ->
                        SampleStockRow(
                            stock = stock,
                            onClick = { onStockClick(stock.ticker) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(Spacing.xl)) }
                }
            }
        }
    }
}

@Composable
private fun SampleStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VettrSurfaceVariant)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SampleStockRow(
    stock: SamplePortfolioStockDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VettrSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.ticker,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stock.exchange,
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrTextSecondary
                )
            }
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary
            )
            Text(
                text = stock.sector,
                style = MaterialTheme.typography.labelSmall,
                color = VettrAccent
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            VettrScoreView(score = stock.vetrScore, size = 32.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format("%.2f", stock.price)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
