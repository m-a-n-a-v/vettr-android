package com.vettr.android.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Determines the color for a VETR score based on 5-tier score ranges.
 * Exposed for testing.
 */
fun getScoreColor(score: Int): Color {
    val s = score.coerceIn(0, 100)
    return when {
        s >= 90 -> Color(0xFF198754)  // Dark Green - Exceptional
        s >= 75 -> Color(0xFF84CC16)  // Lime Green - Healthy
        s >= 50 -> Color(0xFFFBBF24)  // Yellow - Neutral
        s >= 30 -> Color(0xFFF97316)  // Orange - High Risk
        else -> Color(0xFFDC2626)     // Deep Red - Toxic
    }
}

/**
 * Determines the label for a VETR score based on 5-tier score ranges.
 * Exposed for testing.
 */
fun getScoreLabel(score: Int): String {
    val s = score.coerceIn(0, 100)
    return when {
        s >= 90 -> "Exceptional"
        s >= 75 -> "Healthy"
        s >= 50 -> "Neutral"
        s >= 30 -> "High Risk"
        else -> "Toxic"
    }
}

/**
 * A circular VETR score badge that displays a score from 0-100 with color coding
 * and a descriptive label.
 *
 * @param score The VETR score value (0-100)
 * @param modifier Modifier to apply to the composable
 * @param size The diameter of the circular badge
 */
@Composable
fun VettrScoreView(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val normalizedScore = score.coerceIn(0, 100)

    // Determine color based on score ranges
    val scoreColor = getScoreColor(score)

    // Determine label based on score ranges
    val scoreLabel = getScoreLabel(score)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular score badge using Canvas
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val canvasSize = this.size.minDimension
                val strokeWidth = canvasSize * 0.08f
                val radius = (canvasSize - strokeWidth) / 2
                val centerX = this.size.width / 2
                val centerY = this.size.height / 2

                // Draw background circle (gray track)
                drawCircle(
                    color = Color(0xFF3A4A5A),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = strokeWidth)
                )

                // Draw progress arc based on score
                val sweepAngle = (normalizedScore / 100f) * 360f
                drawArc(
                    color = scoreColor,
                    startAngle = -90f, // Start from top
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Score text in center — font scales with circle size
            val fontSize = with(LocalDensity.current) { (size * 0.35f).toSp() }
            Text(
                text = normalizedScore.toString(),
                fontSize = fontSize,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                lineHeight = fontSize
            )
        }

        // Score label below the circle (hidden for small sizes)
        if (size >= 48.dp) {
            Text(
                text = scoreLabel,
                style = MaterialTheme.typography.bodySmall,
                color = scoreColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_Exceptional() {
    VettrTheme {
        VettrScoreView(score = 95)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_Healthy() {
    VettrTheme {
        VettrScoreView(score = 80)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_Neutral() {
    VettrTheme {
        VettrScoreView(score = 55)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_HighRisk() {
    VettrTheme {
        VettrScoreView(score = 35)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_Toxic() {
    VettrTheme {
        VettrScoreView(score = 15)
    }
}
