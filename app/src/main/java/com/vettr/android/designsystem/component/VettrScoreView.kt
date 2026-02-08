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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow

/**
 * Determines the color for a VETR score based on score ranges.
 * Exposed for testing.
 */
fun getScoreColor(score: Int): Color {
    val normalizedScore = score.coerceIn(0, 100)
    return when {
        normalizedScore > 80 -> VettrGreen  // Strong Buy
        normalizedScore >= 60 -> VettrYellow // Buy
        normalizedScore >= 40 -> Color(0xFFFF9800) // Orange - Hold
        else -> VettrRed // Caution
    }
}

/**
 * Determines the label for a VETR score based on score ranges.
 * Exposed for testing.
 */
fun getScoreLabel(score: Int): String {
    val normalizedScore = score.coerceIn(0, 100)
    return when {
        normalizedScore > 80 -> "Strong Buy"
        normalizedScore >= 60 -> "Buy"
        normalizedScore >= 40 -> "Hold"
        else -> "Caution"
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

            // Score text in center
            Text(
                text = normalizedScore.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Score label below the circle
        Text(
            text = scoreLabel,
            style = MaterialTheme.typography.bodySmall,
            color = scoreColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_StrongBuy() {
    VettrTheme {
        VettrScoreView(score = 85)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_Buy() {
    VettrTheme {
        VettrScoreView(score = 70)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_Hold() {
    VettrTheme {
        VettrScoreView(score = 50)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrScoreViewPreview_Caution() {
    VettrTheme {
        VettrScoreView(score = 30)
    }
}
