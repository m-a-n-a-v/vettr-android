package com.vettr.android.feature.pulse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.data.remote.RedFlagTrendPointDto
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrSurfaceVariant
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrWarning
import com.vettr.android.designsystem.theme.VettrYellow

/**
 * Red Flag Global Trends screen showing market-wide red flag patterns.
 * Accessible from Pulse red flag summary section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedFlagTrendsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RedFlagTrendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Red Flag Trends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Time period selector
            item {
                Spacer(modifier = Modifier.height(Spacing.sm))
                TimePeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { viewModel.selectPeriod(it) }
                )
            }

            // Loading state
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = VettrAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Error state
            uiState.error?.let { error ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(VettrRed.copy(alpha = 0.1f))
                            .padding(Spacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VettrRed
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                text = "Tap to retry",
                                style = MaterialTheme.typography.labelMedium,
                                color = VettrAccent,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { viewModel.loadTrends() }
                            )
                        }
                    }
                }
            }

            // Trend data
            val trendResponse = uiState.trendResponse
            if (trendResponse != null && !uiState.isLoading) {
                // Summary breakdown by severity
                item {
                    SeverityBreakdownSection(trendPoints = trendResponse.trendPoints)
                }

                // Flag category breakdown
                item {
                    FlagCategoryBreakdown(trendPoints = trendResponse.trendPoints)
                }

                // Trend data points list
                item {
                    Text(
                        text = "Trend Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(trendResponse.trendPoints) { point ->
                    TrendPointRow(point = point)
                }
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

/**
 * Time period selector with chips.
 */
@Composable
private fun TimePeriodSelector(
    selectedPeriod: TrendPeriod,
    onPeriodSelected: (TrendPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        TrendPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) VettrAccent else VettrSurfaceVariant)
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = Spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.surface else VettrTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Summary section showing total breakdown by severity across all trend points.
 */
@Composable
private fun SeverityBreakdownSection(
    trendPoints: List<RedFlagTrendPointDto>,
    modifier: Modifier = Modifier
) {
    val totalCritical = trendPoints.sumOf { it.severityBreakdown?.critical ?: 0 }
    val totalWarning = trendPoints.sumOf { it.severityBreakdown?.warning ?: 0 }
    val totalInfo = trendPoints.sumOf { it.severityBreakdown?.info ?: 0 }
    val totalCount = trendPoints.sumOf { it.count }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VettrSurfaceVariant)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = "Severity Overview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SeverityCard(
                label = "Critical",
                count = totalCritical,
                color = VettrRed,
                modifier = Modifier.weight(1f)
            )
            SeverityCard(
                label = "Warning",
                count = totalWarning,
                color = VettrWarning,
                modifier = Modifier.weight(1f)
            )
            SeverityCard(
                label = "Info",
                count = totalInfo,
                color = VettrYellow,
                modifier = Modifier.weight(1f)
            )
        }

        // Total count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Flags",
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary
            )
            Text(
                text = "$totalCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Individual severity count card.
 */
@Composable
private fun SeverityCard(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

/**
 * Breakdown of flag types by category (5 red flag categories).
 */
@Composable
private fun FlagCategoryBreakdown(
    trendPoints: List<RedFlagTrendPointDto>,
    modifier: Modifier = Modifier
) {
    // Calculate average counts per data point to show distribution
    val avgCritical = if (trendPoints.isNotEmpty()) {
        trendPoints.sumOf { it.severityBreakdown?.critical ?: 0 }.toFloat() / trendPoints.size
    } else 0f
    val avgWarning = if (trendPoints.isNotEmpty()) {
        trendPoints.sumOf { it.severityBreakdown?.warning ?: 0 }.toFloat() / trendPoints.size
    } else 0f
    val avgInfo = if (trendPoints.isNotEmpty()) {
        trendPoints.sumOf { it.severityBreakdown?.info ?: 0 }.toFloat() / trendPoints.size
    } else 0f
    val avgTotal = if (trendPoints.isNotEmpty()) {
        trendPoints.sumOf { it.count }.toFloat() / trendPoints.size
    } else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VettrSurfaceVariant)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = "Average per Period",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        CategoryProgressRow(
            label = "Critical",
            value = avgCritical,
            maxValue = avgTotal.coerceAtLeast(1f),
            color = VettrRed
        )
        CategoryProgressRow(
            label = "Warning",
            value = avgWarning,
            maxValue = avgTotal.coerceAtLeast(1f),
            color = VettrWarning
        )
        CategoryProgressRow(
            label = "Info",
            value = avgInfo,
            maxValue = avgTotal.coerceAtLeast(1f),
            color = VettrYellow
        )
    }
}

/**
 * Progress row showing a category's proportion.
 */
@Composable
private fun CategoryProgressRow(
    label: String,
    value: Float,
    maxValue: Float,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary
            )
            Text(
                text = String.format("%.1f avg", value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(
            progress = { (value / maxValue).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Individual trend data point row.
 */
@Composable
private fun TrendPointRow(
    point: RedFlagTrendPointDto,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(VettrSurfaceVariant)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = point.date.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            point.severityBreakdown?.let { breakdown ->
                Text(
                    text = "C: ${breakdown.critical}  W: ${breakdown.warning}  I: ${breakdown.info}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrTextSecondary
                )
            }
        }

        // Count badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        point.count > 10 -> VettrRed.copy(alpha = 0.15f)
                        point.count > 5 -> VettrWarning.copy(alpha = 0.15f)
                        else -> VettrYellow.copy(alpha = 0.15f)
                    }
                )
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${point.count}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    point.count > 10 -> VettrRed
                    point.count > 5 -> VettrWarning
                    else -> VettrYellow
                }
            )
        }
    }
}
