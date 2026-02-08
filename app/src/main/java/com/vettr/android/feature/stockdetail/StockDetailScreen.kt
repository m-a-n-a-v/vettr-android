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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import com.vettr.android.designsystem.component.MetricCard
import com.vettr.android.designsystem.component.VettrScoreView
import com.vettr.android.designsystem.component.getScoreLabel
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shares stock information via the Android share sheet.
 *
 * @param context Android context for launching the share intent.
 * @param stock The stock to share.
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
 * Stock Detail screen wrapper that connects to ViewModel.
 * Use this composable when navigating to stock details with ViewModel integration.
 */
@Composable
fun StockDetailRoute(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit = {},
    viewModel: StockDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val stock by viewModel.stock.collectAsStateWithLifecycle()
    val filings by viewModel.filings.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()

    StockDetailScreen(
        stock = stock,
        filings = filings,
        selectedTimeRange = selectedTimeRange,
        selectedTab = when (selectedTab) {
            StockDetailTab.OVERVIEW -> 0
            StockDetailTab.PEDIGREE -> 1
            StockDetailTab.RED_FLAGS -> 2
        },
        onBackClick = onBackClick,
        onShareClick = {
            stock?.let { stockData ->
                shareStock(context, stockData)
            }
            onShareClick()
        },
        onFavoriteClick = { viewModel.toggleFavorite(view) },
        onTimeRangeSelected = { viewModel.selectTimeRange(it) },
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
 * This is the stateless UI component. For ViewModel integration, use StockDetailRoute.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: Stock?,
    filings: List<Filing> = emptyList(),
    selectedTimeRange: TimeRange = TimeRange.ONE_DAY,
    selectedTab: Int = 0,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onTimeRangeSelected: (TimeRange) -> Unit = {},
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
                            tint = MaterialTheme.colorScheme.onSurface
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Spacing.md)
            ) {
                // Header section with ticker, exchange badge, company name, and VETR score
                StockDetailHeader(stock = stock)

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Price display section
                PriceSection(stock = stock)

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Mini chart placeholder
                ChartPlaceholder()

                Spacer(modifier = Modifier.height(Spacing.md))

                // Time range selector
                TimeRangeSelector(
                    selectedTimeRange = selectedTimeRange,
                    onTimeRangeSelected = onTimeRangeSelected
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Tab navigation
                StockDetailTabs(
                    selectedTabIndex = selectedTab,
                    onTabSelected = onTabSelected
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                // Tab content
                when (selectedTab) {
                    0 -> OverviewTab(
                        stock = stock,
                        onShowVetrScoreHelp = { showVetrScoreHelp = true }
                    )
                    1 -> PedigreeTabWrapper(
                        onShowPedigreeHelp = { showPedigreeHelp = true }
                    )
                    2 -> RedFlagsTab(
                        stock = stock,
                        onShowRedFlagHelp = { showRedFlagHelp = true }
                    )
                }
            }
        } else {
            // Loading or error state
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

/**
 * Header section for StockDetailScreen.
 * Displays ticker, exchange badge, company name, VETR score badge, and rating label.
 */
@Composable
private fun StockDetailHeader(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left side: Ticker, exchange, company name
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            // Ticker and Exchange badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.ticker,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Exchange badge
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stock.exchange,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = VettrTextSecondary
                    )
                }
            }

            // Company name
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodyLarge,
                color = VettrTextSecondary
            )
        }

        // Right side: VETR Score badge with label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            VettrScoreView(
                score = stock.vetrScore,
                size = 100.dp
            )
        }
    }
}

/**
 * Price section displaying current price, change amount, and change percentage.
 */
@Composable
private fun PriceSection(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    val isPositive = stock.priceChange >= 0
    val changeColor = if (isPositive) VettrGreen else VettrRed
    val changeSign = if (isPositive) "+" else ""

    // Calculate percentage change
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
        // Current price
        Text(
            text = currencyFormat.format(stock.price),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Change amount and percentage
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$changeSign${currencyFormat.format(stock.priceChange)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = changeColor
            )
            Text(
                text = "$changeSign${"%.2f".format(percentageChange)}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = changeColor
            )
        }
    }
}

/**
 * Placeholder box for the mini line chart.
 */
@Composable
private fun ChartPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = Color.Gray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Chart Placeholder",
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary
        )
    }
}

/**
 * Time range selector using FilterChip composables.
 */
@Composable
private fun TimeRangeSelector(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        TimeRangeChip(
            label = "1D",
            selected = selectedTimeRange == TimeRange.ONE_DAY,
            onClick = { onTimeRangeSelected(TimeRange.ONE_DAY) }
        )
        TimeRangeChip(
            label = "1W",
            selected = selectedTimeRange == TimeRange.ONE_WEEK,
            onClick = { onTimeRangeSelected(TimeRange.ONE_WEEK) }
        )
        TimeRangeChip(
            label = "1M",
            selected = selectedTimeRange == TimeRange.ONE_MONTH,
            onClick = { onTimeRangeSelected(TimeRange.ONE_MONTH) }
        )
        TimeRangeChip(
            label = "1Y",
            selected = selectedTimeRange == TimeRange.ONE_YEAR,
            onClick = { onTimeRangeSelected(TimeRange.ONE_YEAR) }
        )
    }
}

/**
 * Individual FilterChip for time range selection.
 */
@Composable
private fun TimeRangeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = VettrGreen,
            selectedLabelColor = Color.Black
        )
    )
}

/**
 * Custom TabRow for Stock Detail screen tabs.
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

/**
 * Overview tab content showing company description and key metrics.
 */
@Composable
private fun OverviewTab(
    stock: Stock,
    onShowVetrScoreHelp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)

    // Format market cap in compact form (e.g., $1.2B)
    fun formatMarketCap(value: Double): String {
        return when {
            value >= 1_000_000_000 -> "$${String.format("%.1f", value / 1_000_000_000)}B"
            value >= 1_000_000 -> "$${String.format("%.1f", value / 1_000_000)}M"
            value >= 1_000 -> "$${String.format("%.1f", value / 1_000)}K"
            else -> currencyFormat.format(value)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Company description
        Text(
            text = "Company Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${stock.name} operates in the ${stock.sector} sector and is listed on the ${stock.exchange} exchange. " +
                    "The company has demonstrated consistent growth and innovation in its market segment.",
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Key metrics section with VETR Score info button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Key Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onShowVetrScoreHelp) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "VETR Score Help",
                    tint = VettrAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Metrics grid
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
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
                    title = "Sector",
                    value = stock.sector,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                MetricCard(
                    title = "Exchange",
                    value = stock.exchange,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "VETR Score",
                    value = "${stock.vetrScore}/100",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Analysis tab content showing VETR analysis.
 */
@Composable
private fun AnalysisTab(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // VETR Analysis header
        Text(
            text = "VETR Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Analysis bullet points
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            AnalysisBulletPoint(
                text = "Strong fundamentals with consistent revenue growth and healthy profit margins."
            )
            AnalysisBulletPoint(
                text = "Management team has demonstrated effective capital allocation and strategic vision."
            )
            AnalysisBulletPoint(
                text = "Market position is strengthening with increasing competitive advantages."
            )
            AnalysisBulletPoint(
                text = "Risk factors include sector volatility and regulatory changes that may impact operations."
            )
            AnalysisBulletPoint(
                text = "VETR Score of ${stock.vetrScore}/100 reflects ${getScoreLabel(stock.vetrScore).lowercase()} investment quality."
            )
        }
    }
}

/**
 * Analysis bullet point composable.
 */
@Composable
private fun AnalysisBulletPoint(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = VettrAccent
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * News tab content showing recent filings.
 */
@Composable
private fun NewsTab(
    filings: List<Filing>,
    modifier: Modifier = Modifier
) {
    if (filings.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recent filings available",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(filings) { filing ->
                FilingCard(filing = filing)
            }
        }
    }
}

/**
 * Filing card composable for displaying individual filing information.
 */
@Composable
private fun FilingCard(
    filing: Filing,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.CANADA)
    val dateString = dateFormat.format(Date(filing.date))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Date
        Text(
            text = dateString,
            style = MaterialTheme.typography.labelSmall,
            color = VettrTextSecondary
        )

        // Title
        Text(
            text = filing.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Summary
        Text(
            text = filing.summary,
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary
        )
    }
}

/**
 * Pedigree tab wrapper with help button.
 */
@Composable
private fun PedigreeTabWrapper(
    onShowPedigreeHelp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header with info button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Executive Team",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onShowPedigreeHelp) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Pedigree Help",
                    tint = VettrAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Pedigree content
        PedigreeScreen()
    }
}

/**
 * Red Flags tab content showing red flag analysis.
 */
@Composable
private fun RedFlagsTab(
    stock: Stock,
    onShowRedFlagHelp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
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

            IconButton(onClick = onShowRedFlagHelp) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Red Flag Help",
                    tint = VettrAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Red flag content
        RedFlagScreen(
            ticker = stock.ticker,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
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
                    date = System.currentTimeMillis(),
                    summary = "Company reports strong Q4 earnings with revenue growth of 15% year-over-year."
                ),
                Filing(
                    id = "2",
                    stockId = "1",
                    type = "10-K",
                    title = "Annual Report Filing",
                    date = System.currentTimeMillis() - 86400000L,
                    summary = "Annual financial statements showing improved profitability and market expansion."
                )
            ),
            selectedTab = 0
        )
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun StockDetailScreenTabletPreview() {
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
                    date = System.currentTimeMillis(),
                    summary = "Company reports strong Q4 earnings with revenue growth of 15% year-over-year."
                ),
                Filing(
                    id = "2",
                    stockId = "1",
                    type = "10-K",
                    title = "Annual Report Filing",
                    date = System.currentTimeMillis() - 86400000L,
                    summary = "Annual financial statements showing improved profitability and market expansion."
                )
            ),
            selectedTab = 0
        )
    }
}
