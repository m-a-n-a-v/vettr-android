package com.vettr.android.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Dimensions
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Base skeleton shimmer animation using InfiniteTransition.
 * Creates an animated gradient that moves across the skeleton element.
 *
 * @param modifier Modifier to be applied to the skeleton box
 */
@Composable
fun SkeletonView(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    // Shimmer colors - VettrCardBackground with varying gray overlay
    val shimmerColor = Color.Gray.copy(alpha = shimmerAlpha)
    val baseColor = VettrCardBackground

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        shimmerColor,
                        baseColor
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    )
}

/**
 * Skeleton placeholder for StockRowView.
 * Displays a skeleton with logo circle, ticker/name lines, and price/change lines.
 */
@Composable
fun SkeletonStockRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .vettrPadding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Company logo skeleton (circular)
        SkeletonView(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        // Ticker and company name skeleton
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            SkeletonView(
                modifier = Modifier
                    .width(60.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(Dimensions.cardRadius))
            )
            SkeletonView(
                modifier = Modifier
                    .width(140.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(Dimensions.cardRadius))
            )
        }

        // Price and change skeleton
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            SkeletonView(
                modifier = Modifier
                    .width(70.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(Dimensions.cardRadius))
            )
            SkeletonView(
                modifier = Modifier
                    .width(60.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(Dimensions.cardRadius))
            )
        }
    }
}

/**
 * Skeleton placeholder for MetricCard.
 * Displays a skeleton card with title, value, and optional change lines.
 */
@Composable
fun SkeletonMetricCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .cardStyle()
            .vettrPadding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Title skeleton
        SkeletonView(
            modifier = Modifier
                .width(80.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(Dimensions.cardRadius))
        )

        // Value skeleton
        SkeletonView(
            modifier = Modifier
                .width(100.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(Dimensions.cardRadius))
        )

        // Change skeleton
        SkeletonView(
            modifier = Modifier
                .width(60.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(Dimensions.cardRadius))
        )
    }
}

/**
 * Skeleton placeholder for EventCard.
 * Displays a skeleton card with indicator dot and title/subtitle/date lines.
 */
@Composable
fun SkeletonEventCard(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .cardStyle()
            .vettrPadding(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored indicator dot skeleton
        SkeletonView(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
        )

        // Title, subtitle, and date skeleton column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            SkeletonView(
                modifier = Modifier
                    .width(180.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(Dimensions.cardRadius))
            )
            SkeletonView(
                modifier = Modifier
                    .width(140.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(Dimensions.cardRadius))
            )
            SkeletonView(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(Dimensions.cardRadius))
            )
        }
    }
}

// Previews

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SkeletonViewPreview() {
    VettrTheme {
        SkeletonView(
            modifier = Modifier
                .width(200.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SkeletonStockRowPreview() {
    VettrTheme {
        SkeletonStockRow()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SkeletonMetricCardPreview() {
    VettrTheme {
        SkeletonMetricCard()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SkeletonEventCardPreview() {
    VettrTheme {
        SkeletonEventCard()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SkeletonRowsPreview() {
    VettrTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SkeletonStockRow()
            SkeletonStockRow()
            SkeletonStockRow()
            SkeletonStockRow()
            SkeletonStockRow()
        }
    }
}
