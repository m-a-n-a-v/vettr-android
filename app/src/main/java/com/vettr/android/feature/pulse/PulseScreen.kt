package com.vettr.android.feature.pulse

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.model.HealthBucket
import com.vettr.android.core.model.WatchlistHealth
import com.vettr.android.designsystem.component.EmptyStateView
import com.vettr.android.designsystem.component.ErrorView
import com.vettr.android.designsystem.component.FilingTypeBadge
import com.vettr.android.designsystem.component.LastUpdatedText
import com.vettr.android.designsystem.component.RedFlagCategoryCard
import com.vettr.android.designsystem.component.SectionHeader
import com.vettr.android.designsystem.component.SectorExposureCell
import com.vettr.android.designsystem.component.SkeletonEventCard
import com.vettr.android.designsystem.component.SkeletonMetricCard
import com.vettr.android.designsystem.component.SkeletonStockRow
import com.vettr.android.designsystem.component.VettrScoreView
import com.vettr.android.designsystem.component.cardStyle
import com.vettr.android.designsystem.component.vettrPadding
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrAmber
import com.vettr.android.designsystem.theme.VettrEmerald
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrOrange
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTeal
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrWarning
import com.vettr.android.designsystem.theme.VettrYellow
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pulse screen - displays market overview dashboard with watchlist health,
 * sector exposure, red flag summary, filings, movers, and top VETTR scores.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val pulseSummary by viewModel.pulseSummary.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val lastUpdatedAt by viewModel.lastUpdatedAt.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // ── Derived data ──

    // Watchlist health: prefer API, fallback to client-side (5-tier)
    val watchlistHealth by remember(stocks, pulseSummary) {
        derivedStateOf {
            pulseSummary?.watchlistHealth ?: run {
                if (stocks.isEmpty()) WatchlistHealth(
                    elite = HealthBucket(0, 0),
                    contender = HealthBucket(0, 0),
                    watchlist = HealthBucket(0, 0),
                    speculative = HealthBucket(0, 0),
                    toxic = HealthBucket(0, 0)
                ) else {
                    val total = stocks.size
                    val elite = stocks.count { it.vetrScore >= 90 }
                    val contender = stocks.count { it.vetrScore in 75..89 }
                    val watchlistCount = stocks.count { it.vetrScore in 50..74 }
                    val speculative = stocks.count { it.vetrScore in 30..49 }
                    val toxic = stocks.count { it.vetrScore < 30 }
                    WatchlistHealth(
                        elite = HealthBucket(elite, if (total > 0) (elite * 100f / total).roundToInt() else 0),
                        contender = HealthBucket(contender, if (total > 0) (contender * 100f / total).roundToInt() else 0),
                        watchlist = HealthBucket(watchlistCount, if (total > 0) (watchlistCount * 100f / total).roundToInt() else 0),
                        speculative = HealthBucket(speculative, if (total > 0) (speculative * 100f / total).roundToInt() else 0),
                        toxic = HealthBucket(toxic, if (total > 0) (toxic * 100f / total).roundToInt() else 0)
                    )
                }
            }
        }
    }

    // Sector exposure: prefer API, fallback to client-side
    val sectorExposure by remember(stocks, pulseSummary) {
        derivedStateOf {
            pulseSummary?.sectorExposure ?: run {
                if (stocks.isEmpty()) emptyList()
                else {
                    val total = stocks.size
                    stocks.groupBy { it.sector.ifEmpty { "Other" } }
                        .map { (sector, items) ->
                            com.vettr.android.core.model.SectorExposureItem(
                                sector = sector,
                                exchange = items.firstOrNull()?.exchange ?: "",
                                count = items.size,
                                pct = (items.size * 100f / total).roundToInt()
                            )
                        }
                        .sortedByDescending { it.count }
                }
            }
        }
    }

    val redFlagCategories by remember(pulseSummary) {
        derivedStateOf { pulseSummary?.redFlagCategories }
    }

    // Top 2 gainers + top 2 losers
    val topGainers by remember(stocks) {
        derivedStateOf { stocks.sortedByDescending { it.priceChange }.take(2) }
    }
    val topLosers by remember(stocks) {
        derivedStateOf { stocks.sortedBy { it.priceChange }.take(2).filter { it.priceChange < 0 } }
    }

    // Recent filings (up to 4)
    val recentFilings by remember(filings) {
        derivedStateOf { filings.sortedByDescending { it.date }.take(4) }
    }

    // Top VETTR Scores (top 4)
    val topVetrScores by remember(stocks) {
        derivedStateOf { stocks.sortedByDescending { it.vetrScore }.take(4) }
    }

    // Top Movers (top 4)
    val topMovers by remember(stocks) {
        derivedStateOf { stocks.sortedByDescending { abs(it.priceChange) }.take(4) }
    }

    // Stock lookup for filings
    val stockLookup by remember(stocks) {
        derivedStateOf { stocks.associateBy { it.id } }
    }

    // Offline snackbar
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
        if (errorMessage != null) {
            ErrorView(
                message = errorMessage ?: "An error occurred",
                onRetry = { viewModel.refresh() },
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

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

        PullToRefreshBox(
            isRefreshing = isLoading && stocks.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(
                targetState = isLoading && stocks.isEmpty(),
                label = "pulseContentCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { showSkeleton ->
                if (showSkeleton) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        SectionHeader(title = "Market Overview")
                        repeat(3) { SkeletonMetricCard(modifier = Modifier.fillMaxWidth()) }
                        SectionHeader(title = "Red Flag Summary")
                        SkeletonMetricCard(modifier = Modifier.fillMaxWidth())
                        SectionHeader(title = "Smart Filings (SEDAR+)")
                        repeat(3) { SkeletonEventCard() }
                        SectionHeader(title = "Watchlist Movers")
                        repeat(3) { SkeletonStockRow() }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        LastUpdatedText(lastUpdatedAt = lastUpdatedAt, modifier = Modifier.fillMaxWidth())

                        // ═══════ ROW 1: Market Overview ═══════
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            SectionHeader(title = "Market Overview")

                            // Watchlist Health
                            WatchlistHealthCard(health = watchlistHealth)

                            // Sector Exposure
                            if (sectorExposure.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding()
                                ) {
                                    Text(
                                        text = "SECTOR EXPOSURE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(bottom = Spacing.sm)
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                        maxItemsInEachRow = 2
                                    ) {
                                        sectorExposure.take(6).forEach { item ->
                                            SectorExposureCell(
                                                sector = item.sector,
                                                exchange = item.exchange,
                                                count = item.count,
                                                pct = item.pct,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (sectorExposure.take(6).size % 2 != 0) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            // Gainers & Losers
                            Column(modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding()) {
                                Text(
                                    text = "GAINERS & LOSERS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = Spacing.sm)
                                )
                                topGainers.forEach { stock ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { onStockClick(stock.id) }.padding(vertical = Spacing.xs),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = stock.ticker, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                            Text(text = stock.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text(text = "\u25B2 ${String.format("%.2f", abs(stock.priceChange))}%", style = MaterialTheme.typography.bodyMedium, color = VettrGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)).padding(vertical = Spacing.xs))
                                topLosers.forEach { stock ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { onStockClick(stock.id) }.padding(vertical = Spacing.xs),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = stock.ticker, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                            Text(text = stock.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text(text = "\u25BC ${String.format("%.2f", abs(stock.priceChange))}%", style = MaterialTheme.typography.bodyMedium, color = VettrRed, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (topLosers.isEmpty()) {
                                    Text(text = "No losers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = Spacing.xs))
                                }
                            }
                        }

                        // ═══════ ROW 2: Red Flag Summary ═══════
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            SectionHeader(title = "Red Flag Summary")
                            Column(
                                modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(),
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                // Badge pills
                                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(VettrRed.copy(alpha = 0.1f)).padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
                                        Text(text = "Critical Flags (${redFlagCategories?.criticalCount ?: 0})", style = MaterialTheme.typography.labelMedium, color = VettrRed, fontWeight = FontWeight.Medium)
                                    }
                                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(VettrWarning.copy(alpha = 0.1f)).padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
                                        Text(text = "Warnings (${redFlagCategories?.warningCount ?: 0})", style = MaterialTheme.typography.labelMedium, color = VettrWarning, fontWeight = FontWeight.Medium)
                                    }
                                }

                                // Category cards
                                val categories = redFlagCategories?.categories ?: emptyList()
                                val allCategoryNames = listOf("Financial Risk", "Governance", "Momentum")
                                val existingCategoryNames = categories.map { it.category }.toSet()

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    categories.forEach { cat ->
                                        RedFlagCategoryCard(category = cat.category, label = cat.label, stockCount = cat.stockCount, severity = cat.severity, modifier = Modifier.weight(1f))
                                    }
                                    allCategoryNames.filter { it !in existingCategoryNames }.take(3 - categories.size).forEach { catName ->
                                        AllClearCard(category = catName, modifier = Modifier.weight(1f))
                                    }
                                }

                                // Latest alert banner
                                redFlagCategories?.latestAlert?.let { alert ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(VettrRed.copy(alpha = 0.05f)).clickable { onStockClick(alert.ticker) }.padding(Spacing.sm),
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (alert.isNew) {
                                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(VettrRed).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                Text(text = "NEW", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(text = alert.ticker, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                                Text(text = "— ${alert.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Text(text = "View", style = MaterialTheme.typography.labelSmall, color = VettrAccent, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // ═══════ ROW 3: Filings, Movers, Top Scores ═══════

                        // Smart Filings (SEDAR+)
                        if (recentFilings.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                SectionHeader(title = "Smart Filings (SEDAR+)", onSeeAllClick = onSeeAllFilings)
                                Column(modifier = Modifier.fillMaxWidth().cardStyle().vettrPadding(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    recentFilings.forEach { filing ->
                                        val stock = stockLookup[filing.stockId]
                                        val tickerLabel = stock?.ticker ?: ""
                                        Column(modifier = Modifier.fillMaxWidth().clickable { stock?.let { onStockClick(it.id) } }.padding(vertical = Spacing.xs)) {
                                            FilingTypeBadge(filingType = filing.type, modifier = Modifier.padding(bottom = 4.dp))
                                            Text(text = filing.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.padding(top = 2.dp)) {
                                                if (tickerLabel.isNotEmpty()) {
                                                    Text(text = tickerLabel, style = MaterialTheme.typography.labelSmall, color = VettrAccent, fontWeight = FontWeight.SemiBold)
                                                }
                                                Text(text = formatFilingDate(filing.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Watchlist Movers
                        if (topMovers.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                SectionHeader(title = "Watchlist Movers", onSeeAllClick = onSeeAllMovers)
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    topMovers.forEach { stock ->
                                        val changeColor = if (stock.priceChange >= 0) VettrGreen else VettrRed
                                        val directionIcon = if (stock.priceChange >= 0) "\u25B2" else "\u25BC"
                                        Row(
                                            modifier = Modifier.fillMaxWidth().cardStyle().clickable { onStockClick(stock.id) }.vettrPadding(),
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = stock.ticker, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                    VettrScoreView(score = stock.vetrScore, size = 24.dp)
                                                }
                                                Text(text = stock.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Text(text = "$directionIcon ${String.format("%.2f", abs(stock.priceChange))}%", style = MaterialTheme.typography.bodyMedium, color = changeColor, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // Top VETTR Scores (vertical ranked list)
                        if (topVetrScores.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                SectionHeader(title = "Top VETTR Scores", onSeeAllClick = onSeeAllTopScores)
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    topVetrScores.forEachIndexed { index, stock ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().cardStyle().clickable { onStockClick(stock.id) }.vettrPadding(),
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "${index + 1}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = stock.ticker, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                Text(text = stock.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(text = String.format("%.1f", stock.vetrScore / 10.0), style = MaterialTheme.typography.headlineMedium, color = VettrAccent, fontWeight = FontWeight.Bold)
                                                Text(text = "/ 10.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// Supporting Composables
// ════════════════════════════════════════════════════════════════════

@Composable
private fun WatchlistHealthCard(health: WatchlistHealth, modifier: Modifier = Modifier) {
    val total = health.elite.count + health.contender.count + health.watchlist.count + health.speculative.count + health.toxic.count
    Column(
        modifier = modifier.fillMaxWidth().cardStyle().vettrPadding(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(text = "WATCHLIST HEALTH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = Spacing.xs))
        if (total > 0) {
            Row(modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(6.dp)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (health.elite.count > 0) Box(modifier = Modifier.weight(health.elite.count.toFloat()).height(24.dp).clip(RoundedCornerShape(6.dp)).background(VettrEmerald))
                if (health.contender.count > 0) Box(modifier = Modifier.weight(health.contender.count.toFloat()).height(24.dp).clip(RoundedCornerShape(6.dp)).background(VettrTeal))
                if (health.watchlist.count > 0) Box(modifier = Modifier.weight(health.watchlist.count.toFloat()).height(24.dp).clip(RoundedCornerShape(6.dp)).background(VettrAmber))
                if (health.speculative.count > 0) Box(modifier = Modifier.weight(health.speculative.count.toFloat()).height(24.dp).clip(RoundedCornerShape(6.dp)).background(VettrOrange))
                if (health.toxic.count > 0) Box(modifier = Modifier.weight(health.toxic.count.toFloat()).height(24.dp).clip(RoundedCornerShape(6.dp)).background(VettrRed))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            HealthLegendItem(color = VettrEmerald, label = "Elite (Strong Buy)", count = health.elite.count, pct = health.elite.pct)
            HealthLegendItem(color = VettrTeal, label = "Contender (Accumulate)", count = health.contender.count, pct = health.contender.pct)
            HealthLegendItem(color = VettrAmber, label = "Watchlist (Hold)", count = health.watchlist.count, pct = health.watchlist.pct)
            HealthLegendItem(color = VettrOrange, label = "Speculative (Avoid)", count = health.speculative.count, pct = health.speculative.pct)
            HealthLegendItem(color = VettrRed, label = "Toxic (Strong Sell)", count = health.toxic.count, pct = health.toxic.pct)
        }
    }
}

@Composable
private fun HealthLegendItem(color: Color, label: String, count: Int, pct: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = "$count ($pct%)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AllClearCard(category: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)).padding(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VettrGreen))
            Text(text = category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Text(text = "All Clear", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Text(text = "No flags", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

private fun formatFilingDate(dateMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - dateMillis
    return when {
        diff < 0 -> "just now"
        diff < 60_000 -> "just now"
        diff < 3600_000 -> { val m = diff / 60_000; "$m min${if (m == 1L) "" else "s"} ago" }
        diff < 86400_000 -> { val h = diff / 3600_000; "$h hour${if (h == 1L) "" else "s"} ago" }
        diff < 604800_000 -> { val d = diff / 86400_000; "$d day${if (d == 1L) "" else "s"} ago" }
        else -> { val w = diff / 604800_000; "$w week${if (w == 1L) "" else "s"} ago" }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun PulseScreenPreview() {
    VettrTheme {
        PulseScreen(windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp)))
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun PulseScreenTabletPreview() {
    VettrTheme {
        PulseScreen(windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(840.dp, 1200.dp)))
    }
}
