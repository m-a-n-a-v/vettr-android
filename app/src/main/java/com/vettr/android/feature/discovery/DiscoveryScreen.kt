package com.vettr.android.feature.discovery

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.EventCard
import com.vettr.android.designsystem.component.LastUpdatedText
import com.vettr.android.designsystem.component.SectionHeader
import com.vettr.android.designsystem.component.SkeletonEventCard
import com.vettr.android.designsystem.component.SkeletonMetricCard
import com.vettr.android.designsystem.component.cardStyle
import com.vettr.android.designsystem.component.vettrPadding
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow

/**
 * Discovery screen - displays stock discovery and search features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    onStockClick: (String) -> Unit = {},
    viewModel: DiscoveryViewModel = hiltViewModel()
) {
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val sectors by viewModel.sectors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val lastUpdatedAt by viewModel.lastUpdatedAt.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Discovery",
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
            isRefreshing = isLoading && sectors.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Use Crossfade to transition between skeleton and content
            Crossfade(
                targetState = isLoading && sectors.isEmpty(),
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
                        // Filter chips skeleton
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                repeat(2) {
                                    SkeletonMetricCard(
                                        modifier = Modifier
                                            .fillMaxWidth(0.35f)
                                            .height(40.dp)
                                    )
                                }
                            }
                        }

                        // Top Sectors skeleton
                        item {
                            val isExpanded by remember { derivedStateOf { windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded } }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SectionHeader(title = "Top Sectors")

                                LazyVerticalGrid(
                                    columns = if (isExpanded) GridCells.Fixed(3) else GridCells.Adaptive(minSize = 140.dp),
                                    modifier = Modifier.height(200.dp),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    items(4, key = { it }) {
                                        SkeletonMetricCard()
                                    }
                                }
                            }
                        }

                        // Recent Updates skeleton
                        item {
                            SectionHeader(title = "Recent Updates")
                        }

                        items(8, key = { it }) {
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

                        // Filter Chips Section
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = selectedFilter == DiscoveryFilter.WATCHLIST,
                                    onClick = { viewModel.toggleFilter() },
                                    label = {
                                        Text(
                                            text = "My Watchlist",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )

                                FilterChip(
                                    selected = selectedFilter == DiscoveryFilter.ALERTS,
                                    onClick = { viewModel.toggleFilter() },
                                    label = {
                                        Text(
                                            text = "Alerts Only",
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

                        // Top Sectors Section
                        item {
                            val isExpanded by remember { derivedStateOf { windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded } }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SectionHeader(title = "Top Sectors")

                                LazyVerticalGrid(
                                    columns = if (isExpanded) GridCells.Fixed(3) else GridCells.Adaptive(minSize = 140.dp),
                                    modifier = Modifier.height(200.dp),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    items(sectors, key = { it }) { sector ->
                                        SectorCard(
                                            name = sector,
                                            percentage = when (sector) {
                                                "Critical Minerals" -> "+12.5%"
                                                "AI Technology" -> "+8.3%"
                                                "Energy Juniors" -> "-3.2%"
                                                "Clean Tech" -> "+5.7%"
                                                else -> "+0.0%"
                                            },
                                            onClick = { /* TODO: Navigate to sector detail */ }
                                        )
                                    }
                                }
                            }
                        }

                        // Recent Updates Section
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SectionHeader(title = "Recent Updates")
                            }
                        }

                        // Recent Updates List
                        items(5) { index ->
                            val events = listOf(
                                Triple("New Drill Results", "CRE.V - High-grade lithium discovery", VettrGreen),
                                Triple("Insider Selling Alert", "ABC.TO - CEO sold 50,000 shares", VettrRed),
                                Triple("Earnings Beat", "DEF.TO - Q4 revenue up 45%", VettrGreen),
                                Triple("Price Target Change", "GHI.V - Downgraded to Hold", VettrYellow),
                                Triple("M&A Rumor", "JKL.TO - Acquisition talks confirmed", VettrGreen)
                            )
                            val event = events[index % events.size]
                            EventCard(
                                title = event.first,
                                subtitle = event.second,
                                date = "${index + 1} hour${if (index == 0) "" else "s"} ago",
                                indicatorColor = event.third,
                                onClick = { /* TODO: Navigate to event/stock detail based on event type */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sector card composable displaying sector name and performance percentage.
 */
@Composable
fun SectorCard(
    name: String,
    percentage: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardStyle()
            .clickable(onClick = onClick)
            .vettrPadding(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = percentage,
            style = MaterialTheme.typography.bodyLarge,
            color = if (percentage.startsWith("+")) VettrGreen else VettrRed
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SectorCardPreview() {
    VettrTheme {
        SectorCard(
            name = "Critical Minerals",
            percentage = "+12.5%",
            onClick = {}
        )
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
