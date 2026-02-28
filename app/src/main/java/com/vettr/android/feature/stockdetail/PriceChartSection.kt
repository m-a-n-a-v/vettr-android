package com.vettr.android.feature.stockdetail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrSurfaceVariant
import com.vettr.android.designsystem.theme.VettrTextSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Price chart section for the Stock Detail Overview tab.
 * Displays a line chart with time range selector (1D, 1W, 1M, 1Y).
 * Uses Canvas for lightweight rendering without external chart libraries.
 */
@Composable
fun PriceChartSection(
    currentPrice: Double,
    priceChange: Double,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate price data points based on current price and change
    val priceData = remember(currentPrice, priceChange, selectedTimeRange) {
        generatePriceData(currentPrice, priceChange, selectedTimeRange)
    }

    val isPositive = priceChange >= 0
    val chartColor = if (isPositive) VettrGreen else VettrRed

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VettrSurfaceVariant)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Chart title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Min/Max labels
            if (priceData.isNotEmpty()) {
                val min = priceData.minOrNull() ?: 0.0
                val max = priceData.maxOrNull() ?: 0.0
                Text(
                    text = "L: $${String.format("%.2f", min)}  H: $${String.format("%.2f", max)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrTextSecondary
                )
            }
        }

        // Chart Canvas
        if (priceData.size >= 2) {
            PriceLineChart(
                data = priceData,
                lineColor = chartColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chart data unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
            }
        }

        // Time range selector
        TimeRangeSelector(
            selectedRange = selectedTimeRange,
            onRangeSelected = onTimeRangeSelected
        )
    }
}

/**
 * Canvas-based line chart for price visualization.
 */
@Composable
private fun PriceLineChart(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val maxValue = data.maxOrNull() ?: return@Canvas
        val minValue = data.minOrNull() ?: return@Canvas
        val range = maxValue - minValue
        val effectiveRange = if (range < 0.001) 1.0 else range

        val paddingTop = 8.dp.toPx()
        val paddingBottom = 8.dp.toPx()
        val chartHeight = size.height - paddingTop - paddingBottom
        val stepX = size.width / (data.size - 1)

        // Build line path
        val linePath = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedValue = ((value - minValue) / effectiveRange).toFloat()
            val y = paddingTop + chartHeight - (normalizedValue * chartHeight)

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Complete fill path
        fillPath.lineTo(size.width, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()

        // Draw gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.2f),
                    lineColor.copy(alpha = 0.02f)
                )
            )
        )

        // Draw line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw end dot
        val lastIndex = data.size - 1
        val lastX = lastIndex * stepX
        val lastNormalized = ((data[lastIndex] - minValue) / effectiveRange).toFloat()
        val lastY = paddingTop + chartHeight - (lastNormalized * chartHeight)

        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}

/**
 * Time range selector with chip-style buttons.
 */
@Composable
private fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            val label = when (range) {
                TimeRange.ONE_DAY -> "1D"
                TimeRange.ONE_WEEK -> "1W"
                TimeRange.ONE_MONTH -> "1M"
                TimeRange.ONE_YEAR -> "1Y"
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) VettrAccent else Color.Transparent)
                    .clickable { onRangeSelected(range) }
                    .padding(vertical = Spacing.xs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else VettrTextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Generate simulated price data points based on current price, change, and time range.
 * Creates a realistic-looking price curve using the available price information.
 */
private fun generatePriceData(
    currentPrice: Double,
    priceChange: Double,
    timeRange: TimeRange
): List<Double> {
    if (currentPrice <= 0) return emptyList()

    val dataPoints = when (timeRange) {
        TimeRange.ONE_DAY -> 24     // hourly for 1 day
        TimeRange.ONE_WEEK -> 35    // 5 points per day for 7 days
        TimeRange.ONE_MONTH -> 30   // daily for 30 days
        TimeRange.ONE_YEAR -> 52    // weekly for 52 weeks
    }

    // Calculate starting price from current price and change percentage
    val changeDecimal = priceChange / 100.0
    val startPrice = currentPrice / (1 + changeDecimal)

    // Scale volatility based on time range
    val volatility = when (timeRange) {
        TimeRange.ONE_DAY -> 0.005    // 0.5% intraday
        TimeRange.ONE_WEEK -> 0.01    // 1% weekly
        TimeRange.ONE_MONTH -> 0.015  // 1.5% monthly
        TimeRange.ONE_YEAR -> 0.025   // 2.5% yearly
    }

    val data = mutableListOf<Double>()
    val priceStep = (currentPrice - startPrice) / dataPoints

    // Use deterministic "random" based on price to get consistent chart
    val seed = (currentPrice * 1000).toLong()

    for (i in 0 until dataPoints) {
        val trendComponent = startPrice + priceStep * i
        // Pseudo-random noise using sine for deterministic results
        val noise = sin(seed.toDouble() * (i + 1) * 0.7) * volatility * currentPrice +
            cos(seed.toDouble() * (i + 1) * 0.3) * volatility * currentPrice * 0.5
        val price = (trendComponent + noise).coerceAtLeast(0.01)
        data.add(price)
    }

    // Ensure the last data point matches current price
    data.add(currentPrice)

    return data
}
