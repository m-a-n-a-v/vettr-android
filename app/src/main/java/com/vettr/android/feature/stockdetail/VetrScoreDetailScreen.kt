package com.vettr.android.feature.stockdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.data.VetrScoreResult
import com.vettr.android.core.data.repository.PeerComparison
import com.vettr.android.core.data.repository.PeerScore
import com.vettr.android.designsystem.component.getScoreColor
import com.vettr.android.designsystem.component.getScoreLabel
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow

/**
 * VETR Score Detail Screen - BottomSheet displaying detailed score breakdown.
 * Shows large score badge, trend sparkline, component scores, methodology, peer comparison,
 * improvement insights, and disclaimers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetrScoreDetailBottomSheet(
    ticker: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StockDetailViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val vetrScoreResult by viewModel.vetrScoreResult.collectAsStateWithLifecycle()
    val scoreHistory by viewModel.scoreHistory.collectAsStateWithLifecycle()
    val scoreTrend by viewModel.scoreTrend.collectAsStateWithLifecycle()
    val peerComparison by viewModel.peerComparison.collectAsStateWithLifecycle()

    // Load VETR score details when sheet is opened
    androidx.compose.runtime.LaunchedEffect(ticker) {
        viewModel.loadVetrScoreDetails()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        VetrScoreDetailContent(
            ticker = ticker,
            vetrScoreResult = vetrScoreResult,
            scoreHistory = scoreHistory,
            scoreTrend = scoreTrend,
            peerComparison = peerComparison
        )
    }
}

/**
 * Content for VETR Score Detail BottomSheet.
 */
@Composable
private fun VetrScoreDetailContent(
    ticker: String,
    vetrScoreResult: VetrScoreResult?,
    scoreHistory: List<Int>,
    scoreTrend: String,
    peerComparison: PeerComparison?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md)
            .padding(bottom = Spacing.xl)
    ) {
        // Title
        Text(
            text = "VETR Score Breakdown",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        // Large Score Badge
        if (vetrScoreResult != null) {
            LargeScoreBadge(
                score = vetrScoreResult.overallScore,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // 30-day Trend Sparkline
        TrendSparkline(
            scoreHistory = scoreHistory,
            trend = scoreTrend
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Component Scores Section
        Text(
            text = "Score Components",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        if (vetrScoreResult != null) {
            ComponentScoresSection(components = vetrScoreResult.components)
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Scoring Methodology
        ScoringMethodologySection()

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Peer Comparison
        if (peerComparison != null) {
            PeerComparisonSection(peerComparison = peerComparison)

            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        // What Could Improve Score
        if (vetrScoreResult != null) {
            ImprovementInsightsSection(
                components = vetrScoreResult.components,
                overallScore = vetrScoreResult.overallScore
            )

            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        // Disclaimers
        DisclaimersSection()
    }
}

/**
 * Large Score Badge using Canvas for detailed rendering.
 */
@Composable
private fun LargeScoreBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val normalizedScore = score.coerceIn(0, 100)
    val scoreColor = getScoreColor(score)
    val scoreLabel = getScoreLabel(score)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(150.dp)) {
                val canvasSize = this.size.minDimension
                val strokeWidth = canvasSize * 0.1f
                val radius = (canvasSize - strokeWidth) / 2
                val centerX = this.size.width / 2
                val centerY = this.size.height / 2

                // Background circle
                drawCircle(
                    color = Color(0xFF3A4A5A),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = strokeWidth)
                )

                // Progress arc
                val sweepAngle = (normalizedScore / 100f) * 360f
                drawArc(
                    color = scoreColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Text(
                text = normalizedScore.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = scoreLabel,
            style = MaterialTheme.typography.titleMedium,
            color = scoreColor,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "out of 100",
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary
        )
    }
}

/**
 * 30-day Trend Sparkline showing score history.
 */
@Composable
private fun TrendSparkline(
    scoreHistory: List<Int>,
    trend: String,
    modifier: Modifier = Modifier
) {
    val trendColor = when {
        trend.contains("up", ignoreCase = true) -> VettrGreen
        trend.contains("down", ignoreCase = true) -> VettrRed
        else -> VettrYellow
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "30-Day Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = trend,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = trendColor
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Sparkline Chart
        if (scoreHistory.isNotEmpty()) {
            SparklineChart(
                data = scoreHistory,
                color = trendColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No historical data available",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
            }
        }
    }
}

/**
 * Sparkline chart for score trend visualization.
 */
@Composable
private fun SparklineChart(
    data: List<Int>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val maxValue = data.maxOrNull() ?: 100
        val minValue = data.minOrNull() ?: 0
        val range = maxValue - minValue
        val normalizedRange = if (range == 0) 1 else range

        val stepX = size.width / (data.size - 1)
        val path = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedValue = ((value - minValue).toFloat() / normalizedRange)
            val y = size.height - (normalizedValue * size.height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw dots at each data point
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedValue = ((value - minValue).toFloat() / normalizedRange)
            val y = size.height - (normalizedValue * size.height)

            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Component Scores Section with circular progress indicators.
 */
@Composable
private fun ComponentScoresSection(
    components: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    var selectedComponent by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        ComponentScoreRow(
            name = "Pedigree",
            score = components["pedigree"] ?: 0,
            weight = "25%",
            description = "Executive team quality and experience",
            isSelected = selectedComponent == "pedigree",
            onClick = { selectedComponent = if (selectedComponent == "pedigree") null else "pedigree" }
        )

        ComponentScoreRow(
            name = "Filing Velocity",
            score = components["filingVelocity"] ?: 0,
            weight = "20%",
            description = "Timeliness and frequency of regulatory filings",
            isSelected = selectedComponent == "filingVelocity",
            onClick = { selectedComponent = if (selectedComponent == "filingVelocity") null else "filingVelocity" }
        )

        ComponentScoreRow(
            name = "Red Flag Score",
            score = components["redFlag"] ?: 0,
            weight = "25%",
            description = "Inverse of red flag severity (lower flags = higher score)",
            isSelected = selectedComponent == "redFlag",
            onClick = { selectedComponent = if (selectedComponent == "redFlag") null else "redFlag" }
        )

        ComponentScoreRow(
            name = "Growth",
            score = components["growth"] ?: 0,
            weight = "15%",
            description = "Market cap and price momentum indicators",
            isSelected = selectedComponent == "growth",
            onClick = { selectedComponent = if (selectedComponent == "growth") null else "growth" }
        )

        ComponentScoreRow(
            name = "Governance",
            score = components["governance"] ?: 0,
            weight = "15%",
            description = "Corporate governance quality and board structure",
            isSelected = selectedComponent == "governance",
            onClick = { selectedComponent = if (selectedComponent == "governance") null else "governance" }
        )
    }
}

/**
 * Individual component score row with tap-to-expand detail.
 */
@Composable
private fun ComponentScoreRow(
    name: String,
    score: Int,
    weight: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreColor = getScoreColor(score)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Circular Progress Indicator
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier.size(40.dp),
                        color = scoreColor,
                        strokeWidth = 4.dp,
                        trackColor = Color(0xFF3A4A5A)
                    )
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Weight: $weight",
                        style = MaterialTheme.typography.labelSmall,
                        color = VettrTextSecondary
                    )
                }
            }

            Icon(
                imageVector = if (isSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isSelected) "Collapse" else "Expand",
                tint = VettrTextSecondary
            )
        }

        // Expandable description
        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = Spacing.sm)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
            }
        }
    }
}

/**
 * Scoring Methodology expandable section.
 */
@Composable
private fun ScoringMethodologySection(
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .padding(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scoring Methodology",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = VettrTextSecondary
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(top = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = "The VETR Score is calculated from five key components:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VettrTextSecondary
                )

                MethodologyItem(
                    title = "Pedigree (25%)",
                    description = "Evaluates executive team quality based on tenure, experience, and industry expertise."
                )

                MethodologyItem(
                    title = "Filing Velocity (20%)",
                    description = "Measures timeliness and frequency of regulatory filings, with penalties for overdue submissions."
                )

                MethodologyItem(
                    title = "Red Flag Score (25%)",
                    description = "Inverse score based on red flag detection. Fewer red flags result in higher scores."
                )

                MethodologyItem(
                    title = "Growth (15%)",
                    description = "Analyzes market capitalization and price momentum to assess growth potential."
                )

                MethodologyItem(
                    title = "Governance (15%)",
                    description = "Evaluates corporate governance quality, board structure, and transparency indicators."
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "Adjustments:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "• Bonus +5: Audited financials and strong industry expertise\n• Penalty -10: Overdue filings or regulatory issues",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
            }
        }
    }
}

/**
 * Individual methodology item.
 */
@Composable
private fun MethodologyItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
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

/**
 * Peer Comparison section with percentile bar.
 */
@Composable
private fun PeerComparisonSection(
    peerComparison: PeerComparison,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(Spacing.md)
    ) {
        Text(
            text = "Peer Comparison",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Your Score",
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrTextSecondary
                )
                Text(
                    text = "${peerComparison.score}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(peerComparison.score)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Sector Average",
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrTextSecondary
                )
                Text(
                    text = "${peerComparison.sectorAverage}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Percentile Bar
        PercentileBar(percentile = peerComparison.percentile)

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "${peerComparison.percentile}th percentile in sector",
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary
        )
    }
}

/**
 * Percentile bar visualization.
 */
@Composable
private fun PercentileBar(
    percentile: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(
                color = Color(0xFF3A4A5A),
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percentile / 100f)
                .height(12.dp)
                .background(
                    color = VettrAccent,
                    shape = RoundedCornerShape(6.dp)
                )
        )
    }
}

/**
 * What Could Improve Score section with insights.
 */
@Composable
private fun ImprovementInsightsSection(
    components: Map<String, Int>,
    overallScore: Int,
    modifier: Modifier = Modifier
) {
    // Generate insights based on lowest scoring components
    val insights = generateInsights(components, overallScore)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(Spacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = VettrAccent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "What Could Improve Your Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        insights.forEach { insight ->
            InsightItem(insight = insight)
            Spacer(modifier = Modifier.height(Spacing.xs))
        }
    }
}

/**
 * Individual insight item.
 */
@Composable
private fun InsightItem(
    insight: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color = VettrAccent, shape = CircleShape)
                .align(Alignment.CenterVertically)
        )
        Text(
            text = insight,
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Generate improvement insights based on component scores.
 */
private fun generateInsights(components: Map<String, Int>, overallScore: Int): List<String> {
    val insights = mutableListOf<String>()

    // Find lowest scoring components
    val sortedComponents = components.entries.sortedBy { it.value }

    sortedComponents.take(3).forEach { (component, score) ->
        when (component) {
            "pedigree" -> {
                if (score < 60) {
                    insights.add("Strengthen executive team credentials and industry experience to boost Pedigree score")
                }
            }
            "filingVelocity" -> {
                if (score < 60) {
                    insights.add("Maintain consistent filing schedule and avoid delays to improve Filing Velocity")
                }
            }
            "redFlag" -> {
                if (score < 60) {
                    insights.add("Address identified red flags and improve disclosure quality to enhance this score")
                }
            }
            "growth" -> {
                if (score < 60) {
                    insights.add("Focus on sustainable growth strategies and positive price momentum")
                }
            }
            "governance" -> {
                if (score < 60) {
                    insights.add("Enhance corporate governance practices and board transparency")
                }
            }
        }
    }

    // Add general insights if score is moderate
    if (overallScore in 40..70 && insights.size < 3) {
        insights.add("Continue monitoring regulatory filings for timeliness and completeness")
    }

    // If score is already high
    if (overallScore > 80 && insights.isEmpty()) {
        insights.add("Maintain current strong performance across all metrics")
        insights.add("Continue transparent communication with stakeholders")
    }

    // Ensure we have at least 3 insights
    while (insights.size < 3) {
        insights.add("Regular review of company fundamentals and market positioning recommended")
    }

    return insights.take(5)
}

/**
 * Disclaimers section.
 */
@Composable
private fun DisclaimersSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(Spacing.md)
    ) {
        Text(
            text = "Disclaimers",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = VettrTextSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "The VETR Score is for informational purposes only and should not be considered financial advice. " +
                    "Past performance does not guarantee future results. Always conduct your own research and consult " +
                    "with a qualified financial advisor before making investment decisions. Scores are calculated based " +
                    "on publicly available data and may not reflect all material information about a company.",
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary,
            lineHeight = 16.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VetrScoreDetailContentPreview() {
    VettrTheme {
        VetrScoreDetailContent(
            ticker = "SHOP",
            vetrScoreResult = VetrScoreResult(
                overallScore = 85,
                components = mapOf(
                    "pedigree" to 80,
                    "filingVelocity" to 90,
                    "redFlag" to 85,
                    "growth" to 75,
                    "governance" to 88
                ),
                lastUpdated = System.currentTimeMillis()
            ),
            scoreHistory = listOf(78, 80, 82, 83, 85, 84, 85, 86, 85, 85),
            scoreTrend = "Trending up",
            peerComparison = PeerComparison(
                ticker = "SHOP",
                score = 85,
                sectorAverage = 72,
                percentile = 78,
                peerScores = listOf(
                    PeerScore("AMZN", "Amazon", 88),
                    PeerScore("EBAY", "eBay", 75),
                    PeerScore("ETSY", "Etsy", 70)
                )
            )
        )
    }
}
