package com.vettr.android.feature.stockdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.data.DetectedFlag
import com.vettr.android.core.data.RedFlagScore
import com.vettr.android.core.data.RedFlagSeverity
import com.vettr.android.core.data.RedFlagType
import com.vettr.android.core.model.RedFlagHistory
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Red Flag screen showing detected red flags with scores, history, and methodology.
 * Connected to ViewModel for state management.
 */
@Composable
fun RedFlagScreen(
    ticker: String,
    viewModel: RedFlagViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RedFlagScreenContent(
        uiState = uiState,
        onAcknowledgeAll = viewModel::acknowledgeAllFlags,
        onAcknowledgeFlag = viewModel::acknowledgeFlag,
        onToggleMethodology = viewModel::toggleMethodologySheet,
        onToggleFlagExpanded = viewModel::toggleFlagExpanded,
        modifier = modifier
    )
}

/**
 * Stateless Red Flag screen content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedFlagScreenContent(
    uiState: RedFlagUiState,
    onAcknowledgeAll: () -> Unit = {},
    onAcknowledgeFlag: (String) -> Unit = {},
    onToggleMethodology: () -> Unit = {},
    onToggleFlagExpanded: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Analyzing red flags...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VettrTextSecondary
                    )
                }
            }
        } else if (uiState.compositeScore == null || uiState.compositeScore.totalScore < 30) {
            // Empty state for low/no red flags
            EmptyRedFlagState(
                score = uiState.compositeScore?.totalScore ?: 0.0,
                onShowMethodology = onToggleMethodology
            )
        } else {
            // Main content with red flags
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Overall score badge
                item {
                    OverallScoreBadge(score = uiState.compositeScore)
                }

                // Individual flag breakdown
                item {
                    Text(
                        text = "Detected Issues",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }

                items(uiState.currentFlags) { flag ->
                    FlagRow(
                        flag = flag,
                        isExpanded = uiState.expandedFlags.contains(flag.type.name),
                        onToggleExpanded = { onToggleFlagExpanded(flag.type.name) }
                    )
                }

                // Acknowledge All button
                if (uiState.currentFlags.isNotEmpty()) {
                    item {
                        Button(
                            onClick = onAcknowledgeAll,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.sm),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Acknowledge All",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text("Acknowledge All")
                        }
                    }
                }

                // Flag History timeline
                if (uiState.flagHistory.isNotEmpty()) {
                    item {
                        Text(
                            text = "Flag History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = Spacing.md)
                        )
                    }

                    items(uiState.flagHistory) { historyItem ->
                        FlagHistoryItem(
                            historyItem = historyItem,
                            onAcknowledge = { onAcknowledgeFlag(historyItem.id) }
                        )
                    }
                }

                // Methodology info button
                item {
                    Button(
                        onClick = onToggleMethodology,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.md),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text("How Red Flags Are Detected")
                    }
                }
            }
        }
    }

    // Methodology BottomSheet
    if (uiState.showMethodologySheet) {
        ModalBottomSheet(
            onDismissRequest = onToggleMethodology,
            sheetState = sheetState
        ) {
            MethodologyContent()
        }
    }
}

/**
 * Overall score badge with circular progress indicator.
 */
@Composable
private fun OverallScoreBadge(
    score: RedFlagScore?,
    modifier: Modifier = Modifier
) {
    if (score == null) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Circular score badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            // Draw circular progress
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Background circle
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Foreground arc based on score
                val sweepAngle = (score.totalScore / 100.0 * 360.0).toFloat()
                val color = when (score.severity) {
                    RedFlagSeverity.LOW -> Color(0xFF00C853) // VettrGreen
                    RedFlagSeverity.MODERATE -> Color(0xFFFFC107) // VettrYellow
                    RedFlagSeverity.HIGH -> Color(0xFFFF6B00) // Orange
                    RedFlagSeverity.CRITICAL -> Color(0xFFE53935) // VettrRed
                }

                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Score text in center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = score.totalScore.toInt().toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/ 100",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
            }
        }

        // Severity label
        SuggestionChip(
            onClick = {},
            label = {
                Text(
                    text = score.severity.name,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = when (score.severity) {
                    RedFlagSeverity.LOW -> VettrGreen
                    RedFlagSeverity.MODERATE -> VettrYellow
                    RedFlagSeverity.HIGH -> Color(0xFFFF6B00)
                    RedFlagSeverity.CRITICAL -> VettrRed
                },
                labelColor = Color.Black
            )
        )

        Text(
            text = "${score.flags.size} red flag${if (score.flags.size != 1) "s" else ""} detected",
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary
        )
    }
}

/**
 * Individual flag row with expandable detail.
 */
@Composable
private fun FlagRow(
    flag: DetectedFlag,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon and flag name
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = getFlagColor(flag.score),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = formatFlagName(flag.type),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Severity chip
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = getSeverityLabel(flag.score),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = getFlagColor(flag.score),
                        labelColor = Color.Black
                    ),
                    modifier = Modifier.height(28.dp)
                )

                // Expand/collapse icon
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Score breakdown bar
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                LinearProgressIndicator(
                    progress = { (flag.score / 100.0).toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = getFlagColor(flag.score),
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
                Text(
                    text = "${flag.score.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Expandable description
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = flag.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VettrTextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Flag history timeline item.
 */
@Composable
private fun FlagHistoryItem(
    historyItem: RedFlagHistory,
    onAcknowledge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(historyItem.detectedAt))

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (historyItem.isAcknowledged) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatFlagName(RedFlagType.valueOf(historyItem.flagType)),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (historyItem.isAcknowledged) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Acknowledged",
                            tint = VettrGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
                Text(
                    text = "Score: ${historyItem.score.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
            }

            if (!historyItem.isAcknowledged) {
                Button(
                    onClick = onAcknowledge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VettrGreen
                    )
                ) {
                    Text("Acknowledge", color = Color.Black)
                }
            }
        }
    }
}

/**
 * Empty state when red flag score is low (< 30).
 */
@Composable
private fun EmptyRedFlagState(
    score: Double,
    onShowMethodology: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(VettrGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = VettrGreen,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = "All Clear!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = if (score == 0.0) {
                "No red flags detected for this stock."
            } else {
                "Only minor concerns detected (score: ${score.toInt()}/100)."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = VettrTextSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "This stock shows no significant red flags in our analysis. Continue monitoring for any changes.",
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Button(
            onClick = onShowMethodology,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("Learn About Red Flags")
        }
    }
}

/**
 * Methodology bottom sheet content explaining how red flags are detected.
 */
@Composable
private fun MethodologyContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = "Red Flag Methodology",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "VETTR analyzes 5 key indicators to detect potential red flags:",
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        MethodologyItem(
            title = "Consolidation Velocity (30%)",
            description = "Tracks frequency of share consolidations. Frequent consolidations may indicate dilution concerns."
        )

        MethodologyItem(
            title = "Financing Velocity (25%)",
            description = "Monitors equity financing frequency. Multiple financings may signal cash burn or operational challenges."
        )

        MethodologyItem(
            title = "Executive Churn (20%)",
            description = "Analyzes C-suite turnover rates. High turnover may indicate leadership instability."
        )

        MethodologyItem(
            title = "Disclosure Gaps (15%)",
            description = "Identifies delays in required filings. Gaps may suggest disclosure issues or operational problems."
        )

        MethodologyItem(
            title = "Debt Trend (10%)",
            description = "Examines debt mentions in recent filings. Increasing debt references may indicate financial leverage concerns."
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Severity Levels",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SeverityChip("LOW", VettrGreen, "< 30")
            SeverityChip("MODERATE", VettrYellow, "30-60")
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SeverityChip("HIGH", Color(0xFFFF6B00), "60-85")
            SeverityChip("CRITICAL", VettrRed, "> 85")
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
    }
}

@Composable
private fun MethodologyItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
            }
        }
    }
}

@Composable
private fun RowScope.SeverityChip(
    label: String,
    color: Color,
    range: String
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color, RoundedCornerShape(8.dp))
                .padding(vertical = Spacing.sm),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        Text(
            text = range,
            style = MaterialTheme.typography.labelSmall,
            color = VettrTextSecondary
        )
    }
}

// Helper functions

private fun formatFlagName(type: RedFlagType): String {
    return when (type) {
        RedFlagType.CONSOLIDATION_VELOCITY -> "Consolidation Velocity"
        RedFlagType.FINANCING_VELOCITY -> "Financing Velocity"
        RedFlagType.EXECUTIVE_CHURN -> "Executive Churn"
        RedFlagType.DISCLOSURE_GAPS -> "Disclosure Gaps"
        RedFlagType.DEBT_TREND -> "Debt Trend"
    }
}

private fun getSeverityLabel(score: Double): String {
    return when {
        score < 10 -> "LOW"
        score < 20 -> "MODERATE"
        score < 30 -> "HIGH"
        else -> "CRITICAL"
    }
}

private fun getFlagColor(score: Double): Color {
    return when {
        score < 10 -> Color(0xFF00C853) // Green
        score < 20 -> Color(0xFFFFC107) // Yellow
        score < 30 -> Color(0xFFFF6B00) // Orange
        else -> Color(0xFFE53935) // Red
    }
}

// Previews

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun RedFlagScreenPreview_WithFlags() {
    VettrTheme {
        RedFlagScreenContent(
            uiState = RedFlagUiState(
                currentFlags = listOf(
                    DetectedFlag(
                        type = RedFlagType.CONSOLIDATION_VELOCITY,
                        ticker = "XYZ",
                        score = 30.0,
                        description = "Detected 3 share consolidations in the past year. Frequent consolidations may indicate ongoing dilution concerns.",
                        detectedAt = System.currentTimeMillis()
                    ),
                    DetectedFlag(
                        type = RedFlagType.FINANCING_VELOCITY,
                        ticker = "XYZ",
                        score = 25.0,
                        description = "Detected 4 equity financings in the past year. Frequent financings may indicate cash burn or operational challenges.",
                        detectedAt = System.currentTimeMillis()
                    )
                ),
                compositeScore = RedFlagScore(
                    totalScore = 55.0,
                    severity = RedFlagSeverity.MODERATE,
                    flags = emptyList()
                ),
                isLoading = false
            )
        )
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun RedFlagScreenTabletPreview() {
    VettrTheme {
        RedFlagScreenContent(
            uiState = RedFlagUiState(
                currentFlags = listOf(
                    DetectedFlag(
                        type = RedFlagType.CONSOLIDATION_VELOCITY,
                        ticker = "XYZ",
                        score = 30.0,
                        description = "Detected 3 share consolidations in the past year. Frequent consolidations may indicate ongoing dilution concerns.",
                        detectedAt = System.currentTimeMillis()
                    ),
                    DetectedFlag(
                        type = RedFlagType.FINANCING_VELOCITY,
                        ticker = "XYZ",
                        score = 25.0,
                        description = "Detected 4 equity financings in the past year. Frequent financings may indicate cash burn or operational challenges.",
                        detectedAt = System.currentTimeMillis()
                    )
                ),
                compositeScore = RedFlagScore(
                    totalScore = 55.0,
                    severity = RedFlagSeverity.MODERATE,
                    flags = emptyList()
                ),
                isLoading = false
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun RedFlagScreenPreview_Empty() {
    VettrTheme {
        RedFlagScreenContent(
            uiState = RedFlagUiState(
                currentFlags = emptyList(),
                compositeScore = null,
                isLoading = false
            )
        )
    }
}
