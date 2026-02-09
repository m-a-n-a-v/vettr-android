package com.vettr.android.feature.stockdetail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import com.vettr.android.designsystem.component.MetricCard
import com.vettr.android.designsystem.component.VettrScoreView
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Shares stock information via the Android share sheet.
 */
private fun shareStock(context: android.content.Context, stock: Stock) {
    val shareText = buildString {
        append("${stock.ticker} - ${stock.name}\n")
        append("VETR Score: ${stock.vetrScore}/100\n")
        append("\nView more on VETTR: https://vettr.com/stocks/${stock.ticker}")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

/**
 * Format a date as relative time (e.g., "2h ago", "3d ago", "Jan 15").
 */
private fun formatRelativeDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

/**
 * Format market cap in compact form (e.g., $4.8B, $215M).
 */
private fun formatMarketCap(value: Double): String {
    return when {
        value >= 1_000_000_000_000 -> "$${String.format("%.1f", value / 1_000_000_000_000)}T"
        value >= 1_000_000_000 -> "$${String.format("%.1f", value / 1_000_000_000)}B"
        value >= 1_000_000 -> "$${String.format("%.0f", value / 1_000_000)}M"
        value >= 1_000 -> "$${String.format("%.0f", value / 1_000)}K"
        else -> "$${String.format("%.0f", value)}"
    }
}

/**
 * Get filing type color based on filing type string.
 */
private fun getFilingTypeColor(type: String): Color {
    return when (type.uppercase()) {
        "10-K" -> Color(0xFF4CAF50)    // Green - Annual
        "10-Q" -> Color(0xFF2196F3)    // Blue - Quarterly
        "8-K" -> Color(0xFFFF9800)     // Orange - Current event
        "DEF 14A" -> Color(0xFF9C27B0) // Purple - Proxy
        else -> Color(0xFF607D8B)       // Gray - Other
    }
}

/**
 * Stock Detail screen wrapper that connects to ViewModel.
 */
@Composable
fun StockDetailRoute(
    onBackClick: () -> Unit,
    windowSizeClass: WindowSizeClass,
    onShareClick: () -> Unit = {},
    viewModel: StockDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val stock by viewModel.stock.collectAsStateWithLifecycle()
    val filings by viewModel.filings.collectAsStateWithLifecycle()
    val executives by viewModel.executives.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    StockDetailScreen(
        stock = stock,
        filings = filings,
        executives = executives,
        selectedTab = when (selectedTab) {
            StockDetailTab.OVERVIEW -> 0
            StockDetailTab.PEDIGREE -> 1
            StockDetailTab.RED_FLAGS -> 2
        },
        isRefreshing = isRefreshing,
        windowSizeClass = windowSizeClass,
        onBackClick = onBackClick,
        onShareClick = {
            stock?.let { stockData ->
                shareStock(context, stockData)
            }
            onShareClick()
        },
        onFavoriteClick = { viewModel.toggleFavorite(view) },
        onRefresh = { viewModel.refresh() },
        onTabSelected = { tabIndex ->
            viewModel.selectTab(
                when (tabIndex) {
                    0 -> StockDetailTab.OVERVIEW
                    1 -> StockDetailTab.PEDIGREE
                    2 -> StockDetailTab.RED_FLAGS
                    else -> StockDetailTab.OVERVIEW
                }
            )
        },
        modifier = modifier
    )
}

/**
 * Stock Detail screen - displays detailed information about a specific stock.
 * Full 3-tab layout matching iOS: Overview, Pedigree, Red Flags.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: Stock?,
    filings: List<Filing> = emptyList(),
    executives: List<Executive> = emptyList(),
    selectedTab: Int = 0,
    isRefreshing: Boolean = false,
    windowSizeClass: WindowSizeClass,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showVetrScoreHelp by remember { mutableStateOf(false) }
    var showPedigreeHelp by remember { mutableStateOf(false) }
    var showRedFlagHelp by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (stock?.isFavorite == true) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = if (stock?.isFavorite == true) {
                                "Remove from favorites"
                            } else {
                                "Add to favorites"
                            },
                            tint = if (stock?.isFavorite == true) VettrYellow else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (stock != null) {
            val pullToRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // ---- Price Header ----
                    item {
                        PriceHeader(stock = stock)
                    }

                    // ---- Tab Bar ----
                    item {
                        StockDetailTabs(
                            selectedTabIndex = selectedTab,
                            onTabSelected = onTabSelected
                        )
                    }

                    // ---- Tab Content ----
                    when (selectedTab) {
                        0 -> {
                            // OVERVIEW TAB
                            item {
                                OverviewVetrScoreSection(
                                    stock = stock,
                                    onShowHelp = { showVetrScoreHelp = true }
                                )
                            }

                            item {
                                OverviewKeyMetrics(
                                    stock = stock,
                                    filingCount = filings.size
                                )
                            }

                            if (filings.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Recent Filings",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = Spacing.sm)
                                    )
                                }

                                items(filings, key = { it.id }) { filing ->
                                    FilingRow(filing = filing)
                                }
                            } else {
                                item {
                                    EmptyFilingsState()
                                }
                            }
                        }

                        1 -> {
                            // PEDIGREE TAB
                            item {
                                PedigreeHeader(
                                    executiveCount = executives.size,
                                    onShowHelp = { showPedigreeHelp = true }
                                )
                            }

                            if (executives.isNotEmpty()) {
                                items(executives, key = { it.id }) { executive ->
                                    ExecutiveRow(executive = executive)
                                }
                            } else {
                                item {
                                    EmptyExecutivesState()
                                }
                            }
                        }

                        2 -> {
                            // RED FLAGS TAB
                            item {
                                RedFlagsTabContent(
                                    stock = stock,
                                    onShowHelp = { showRedFlagHelp = true }
                                )
                            }
                        }
                    }

                    // Bottom spacer for scroll clearance
                    item {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                    }
                }
            }
        } else {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading stock details...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // Help bottom sheets
    if (showVetrScoreHelp) {
        VetrScoreHelpBottomSheet(onDismiss = { showVetrScoreHelp = false })
    }
    if (showPedigreeHelp) {
        PedigreeHelpBottomSheet(onDismiss = { showPedigreeHelp = false })
    }
    if (showRedFlagHelp) {
        RedFlagHelpBottomSheet(onDismiss = { showRedFlagHelp = false })
    }
}

// =============================================================================
// PRICE HEADER - Company name, price, change, sector + exchange chips
// =============================================================================

/**
 * Price header section: company name, current price, price change with arrow, sector + exchange chips.
 */
@Composable
private fun PriceHeader(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    val isPositive = stock.priceChange >= 0
    val changeColor = if (isPositive) VettrGreen else VettrRed
    val changeArrow = if (isPositive) "\u2191" else "\u2193"
    val changeSign = if (isPositive) "+" else ""

    val percentageChange = if (stock.price > 0) {
        (stock.priceChange / (stock.price - stock.priceChange)) * 100
    } else {
        0.0
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Company name
        Text(
            text = stock.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Price
        Text(
            text = currencyFormat.format(stock.price),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Price change with arrow
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$changeArrow $changeSign${currencyFormat.format(stock.priceChange)} ($changeSign${"%.2f".format(percentageChange)}%)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = changeColor
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Sector + Exchange chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            ChipBadge(text = stock.sector, color = VettrAccent.copy(alpha = 0.15f), textColor = VettrAccent)
            ChipBadge(text = stock.exchange, color = MaterialTheme.colorScheme.surfaceVariant, textColor = VettrTextSecondary)
        }
    }
}

/**
 * Small chip badge for sector and exchange.
 */
@Composable
private fun ChipBadge(
    text: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

// =============================================================================
// TAB BAR
// =============================================================================

/**
 * Custom TabRow for Stock Detail screen tabs with accent underline.
 */
@Composable
private fun StockDetailTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Overview", "Pedigree", "Red Flags")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = VettrAccent
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = VettrTextSecondary
            )
        }
    }
}

// =============================================================================
// OVERVIEW TAB - VETR Score, Key Metrics, Recent Filings
// =============================================================================

/**
 * VETR Score section with large circular badge.
 */
@Composable
private fun OverviewVetrScoreSection(
    stock: Stock,
    onShowHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VETR Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onShowHelp) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "VETR Score Help",
                    tint = VettrAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        VettrScoreView(
            score = stock.vetrScore,
            size = 120.dp
        )
    }
}

/**
 * Key Metrics 2x2 grid: Market Cap, Exchange, Sector, Filing count.
 */
@Composable
private fun OverviewKeyMetrics(
    stock: Stock,
    filingCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = "Key Metrics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Row 1: Market Cap + Exchange
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            MetricCard(
                title = "Market Cap",
                value = formatMarketCap(stock.marketCap),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Exchange",
                value = stock.exchange,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Sector + Filing Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            MetricCard(
                title = "Sector",
                value = stock.sector,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Filings",
                value = filingCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Filing row with colored type icon, title, relative date, summary (2 lines).
 */
@Composable
private fun FilingRow(
    filing: Filing,
    modifier: Modifier = Modifier
) {
    val typeColor = getFilingTypeColor(filing.type)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = VettrCardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            // Filing type icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = filing.type,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                // Title row with type badge and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = filing.type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Text(
                        text = formatRelativeDate(filing.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = VettrTextSecondary
                    )
                }

                // Title
                Text(
                    text = filing.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Summary (2 lines)
                Text(
                    text = filing.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Empty state when no filings are available.
 */
@Composable
private fun EmptyFilingsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = VettrTextSecondary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "No recent filings available",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        }
    }
}

// =============================================================================
// PEDIGREE TAB - Executive list with tenure risk badges
// =============================================================================

/**
 * Pedigree tab header with executive count and info button.
 */
@Composable
private fun PedigreeHeader(
    executiveCount: Int,
    onShowHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Executive Team",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$executiveCount executive${if (executiveCount != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary
            )
        }
        IconButton(onClick = onShowHelp) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Pedigree Help",
                tint = VettrAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Determine tenure risk level and associated color.
 */
private enum class TenureRisk(val label: String, val color: Color) {
    LOW("Low", VettrGreen),
    MEDIUM("Medium", VettrYellow),
    HIGH("High", VettrRed)
}

private fun getTenureRisk(years: Double): TenureRisk {
    return when {
        years >= 5.0 -> TenureRisk.LOW
        years >= 1.0 -> TenureRisk.MEDIUM
        else -> TenureRisk.HIGH
    }
}

/**
 * Executive row: name, title, years at company, specialization, tenure risk badge.
 */
@Composable
private fun ExecutiveRow(
    executive: Executive,
    modifier: Modifier = Modifier
) {
    val tenureRisk = getTenureRisk(executive.yearsAtCompany)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = VettrCardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Name, title, specialization
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = executive.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = executive.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
                if (executive.specialization.isNotEmpty()) {
                    Text(
                        text = executive.specialization,
                        style = MaterialTheme.typography.labelSmall,
                        color = VettrAccent
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Right: Tenure + risk badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = "${String.format("%.1f", executive.yearsAtCompany)} yrs",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Tenure risk badge
                Box(
                    modifier = Modifier
                        .background(tenureRisk.color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = Spacing.sm, vertical = 2.dp)
                ) {
                    Text(
                        text = tenureRisk.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tenureRisk.color
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no executives are available.
 */
@Composable
private fun EmptyExecutivesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = VettrTextSecondary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No executives found",
                style = MaterialTheme.typography.titleSmall,
                color = VettrTextSecondary
            )
            Text(
                text = "Executive data will appear once available",
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary
            )
        }
    }
}

// =============================================================================
// RED FLAGS TAB - Overall score + empty state
// =============================================================================

/**
 * Red Flags tab content with score and empty state.
 */
@Composable
private fun RedFlagsTabContent(
    stock: Stock,
    onShowHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Header with info button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Risk Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onShowHelp) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Red Flag Help",
                    tint = VettrAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Red flag score derived from vetrScore (placeholder)
        val redFlagScore = maxOf(0, 100 - stock.vetrScore)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Overall red flag score badge
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(VettrGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "All Clear",
                    tint = VettrGreen,
                    modifier = Modifier.size(60.dp)
                )
            }

            Text(
                text = "No Red Flags Detected",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Risk Score: $redFlagScore/100",
                style = MaterialTheme.typography.bodyLarge,
                color = VettrTextSecondary
            )

            Text(
                text = "This stock shows no significant red flags in our analysis. Continue monitoring for any changes.",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary,
                modifier = Modifier.padding(horizontal = Spacing.md)
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Methodology section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = VettrCardBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "Methodology",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    MethodologyBullet(
                        text = "Consolidation Velocity - Tracks share consolidation frequency"
                    )
                    MethodologyBullet(
                        text = "Financing Velocity - Monitors equity financing frequency"
                    )
                    MethodologyBullet(
                        text = "Executive Churn - Analyzes C-suite turnover rates"
                    )
                    MethodologyBullet(
                        text = "Disclosure Gaps - Identifies filing delays"
                    )
                    MethodologyBullet(
                        text = "Debt Trend - Examines debt mentions in filings"
                    )
                }
            }
        }
    }
}

@Composable
private fun MethodologyBullet(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = "\u2022",
            style = MaterialTheme.typography.bodySmall,
            color = VettrAccent
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Phone - Overview", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun StockDetailScreenPreview() {
    VettrTheme {
        StockDetailScreen(
            stock = Stock(
                id = "1",
                ticker = "SHOP",
                name = "Shopify Inc.",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 85000000000.0,
                price = 120.50,
                priceChange = 2.35,
                vetrScore = 85,
                isFavorite = true
            ),
            filings = listOf(
                Filing(
                    id = "1",
                    stockId = "1",
                    type = "8-K",
                    title = "Quarterly Earnings Report",
                    date = System.currentTimeMillis() - 3600000L,
                    summary = "Company reports strong Q4 earnings with revenue growth of 15% year-over-year."
                ),
                Filing(
                    id = "2",
                    stockId = "1",
                    type = "10-K",
                    title = "Annual Report Filing",
                    date = System.currentTimeMillis() - 86400000L * 3,
                    summary = "Annual financial statements showing improved profitability and market expansion."
                )
            ),
            executives = listOf(
                Executive(
                    id = "1",
                    stockId = "1",
                    name = "Tobi Lutke",
                    title = "Chief Executive Officer",
                    yearsAtCompany = 18.0,
                    previousCompanies = "[]",
                    education = "Koblenzer Schule",
                    specialization = "Technology & Commerce"
                ),
                Executive(
                    id = "2",
                    stockId = "1",
                    name = "Jeff Hoffmeister",
                    title = "Chief Financial Officer",
                    yearsAtCompany = 2.5,
                    previousCompanies = "[]",
                    education = "MBA",
                    specialization = "Finance"
                ),
                Executive(
                    id = "3",
                    stockId = "1",
                    name = "New Executive",
                    title = "VP Engineering",
                    yearsAtCompany = 0.5,
                    previousCompanies = "[]",
                    education = "CS Degree",
                    specialization = "Engineering"
                )
            ),
            selectedTab = 0,
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Phone - Pedigree", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun StockDetailScreenPedigreePreview() {
    VettrTheme {
        StockDetailScreen(
            stock = Stock(
                id = "1",
                ticker = "SHOP",
                name = "Shopify Inc.",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 85000000000.0,
                price = 120.50,
                priceChange = 2.35,
                vetrScore = 85,
                isFavorite = false
            ),
            executives = listOf(
                Executive(
                    id = "1",
                    stockId = "1",
                    name = "Tobi Lutke",
                    title = "Chief Executive Officer",
                    yearsAtCompany = 18.0,
                    previousCompanies = "[]",
                    education = "Koblenzer Schule",
                    specialization = "Technology & Commerce"
                ),
                Executive(
                    id = "2",
                    stockId = "1",
                    name = "Jeff Hoffmeister",
                    title = "Chief Financial Officer",
                    yearsAtCompany = 2.5,
                    previousCompanies = "[]",
                    education = "MBA",
                    specialization = "Finance"
                )
            ),
            selectedTab = 1,
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Phone - Red Flags", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun StockDetailScreenRedFlagsPreview() {
    VettrTheme {
        StockDetailScreen(
            stock = Stock(
                id = "1",
                ticker = "SHOP",
                name = "Shopify Inc.",
                exchange = "TSX",
                sector = "Technology",
                marketCap = 85000000000.0,
                price = 120.50,
                priceChange = -1.20,
                vetrScore = 85,
                isFavorite = false
            ),
            selectedTab = 2,
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        )
    }
}
