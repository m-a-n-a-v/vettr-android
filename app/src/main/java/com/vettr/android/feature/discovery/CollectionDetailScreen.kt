package com.vettr.android.feature.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.vettr.android.core.data.remote.CollectionStockDto
import com.vettr.android.core.data.remote.DiscoveryCollectionDto
import com.vettr.android.designsystem.component.SectorChip
import com.vettr.android.designsystem.component.VettrScoreView
import com.vettr.android.designsystem.component.cardStyle
import com.vettr.android.designsystem.component.vettrPadding
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Collection Detail screen - shows all stocks in a selected collection.
 *
 * @param collection The collection to display
 * @param onBackClick Callback when the back button is clicked
 * @param onStockClick Callback when a stock is clicked, receives stock ID
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collection: DiscoveryCollectionDto,
    onBackClick: () -> Unit,
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = collection.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Discovery"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Collection Header
            item {
                CollectionHeader(collection = collection)
            }

            // Stock List
            items(collection.stocks, key = { it.ticker }) { stock ->
                CollectionStockRow(
                    stock = stock,
                    onClick = { onStockClick(stock.ticker) }
                )
            }
        }
    }
}

/**
 * Collection header showing icon, name, tagline, and criteria summary.
 */
@Composable
private fun CollectionHeader(
    collection: DiscoveryCollectionDto,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardStyle()
            .vettrPadding(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(VettrAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconToEmoji(collection.icon),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Name and tagline
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = collection.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Criteria summary
        Text(
            text = collection.criteriaSummary,
            style = MaterialTheme.typography.bodySmall,
            color = VettrAccent,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Stock row displaying ticker, name, sector, price, change, and VETR score.
 */
@Composable
private fun CollectionStockRow(
    stock: CollectionStockDto,
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
        // Left section: Ticker, name, sector
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.ticker,
                    style = MaterialTheme.typography.titleMedium,
                    color = VettrAccent,
                    fontWeight = FontWeight.Bold
                )
                SectorChip(sector = stock.sector)
            }

            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right section: Price, change, score
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Price and change column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = stock.price?.let { String.format("$%.2f", it) } ?: "N/A",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                stock.priceChange?.let { change ->
                    val changeColor = if (change >= 0) VettrGreen else VettrRed
                    val changePrefix = if (change >= 0) "+" else ""
                    Text(
                        text = "${changePrefix}${String.format("%.1f", change)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = changeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // VETR score badge
            stock.vetrScore?.let { score ->
                VettrScoreView(
                    score = score,
                    size = 32.dp
                )
            }
        }
    }
}

@Preview(name = "Collection Detail Screen", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun CollectionDetailScreenPreview() {
    VettrTheme {
        CollectionDetailScreen(
            collection = DiscoveryCollectionDto(
                id = "clean_sheets",
                name = "The Clean Sheet Collection",
                tagline = "Score >75, Zero Red Flags. Safety first.",
                icon = "checkmark.shield",
                criteriaSummary = "Avg Vettr Score: 82  Stocks: 18",
                stocks = listOf(
                    CollectionStockDto(
                        ticker = "NXE",
                        name = "NexGen Energy Ltd.",
                        exchange = "TSX",
                        sector = "Uranium",
                        marketCap = 5200000000.0,
                        price = 12.50,
                        priceChange = 0.8,
                        vetrScore = 85
                    ),
                    CollectionStockDto(
                        ticker = "FCU",
                        name = "Fission Uranium Corp.",
                        exchange = "TSX",
                        sector = "Uranium",
                        marketCap = 950000000.0,
                        price = 1.45,
                        priceChange = -1.2,
                        vetrScore = 78
                    ),
                    CollectionStockDto(
                        ticker = "DML",
                        name = "Denison Mines Corp.",
                        exchange = "TSX",
                        sector = "Uranium",
                        marketCap = 1200000000.0,
                        price = 2.15,
                        priceChange = 2.5,
                        vetrScore = 82
                    )
                )
            ),
            onBackClick = {},
            onStockClick = {}
        )
    }
}
