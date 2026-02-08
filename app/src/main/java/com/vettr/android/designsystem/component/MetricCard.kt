package com.vettr.android.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Determines the color for a change indicator based on whether the change is positive or negative.
 * Exposed for testing.
 */
fun getChangeColor(change: Double): Color {
    return if (change >= 0) VettrGreen else VettrRed
}

/**
 * MetricCard displays a key metric in a consistent card format.
 *
 * @param title The metric title/label
 * @param value The metric value to display
 * @param change Optional percentage change value (e.g., 5.25 for +5.25%)
 * @param modifier Optional modifier for customization
 */
@Composable
fun MetricCard(
    title: String,
    value: String,
    change: Double? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .cardStyle()
            .vettrPadding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Value
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Change indicator (if provided)
        change?.let {
            val isPositive = it >= 0
            val changeColor = getChangeColor(it)
            val changePrefix = if (isPositive) "+" else ""

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$changePrefix${String.format("%.2f", it)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = changeColor
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun MetricCardPositivePreview() {
    VettrTheme {
        MetricCard(
            title = "Market Cap",
            value = "$1.2B",
            change = 5.25
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun MetricCardNegativePreview() {
    VettrTheme {
        MetricCard(
            title = "Price",
            value = "$48.50",
            change = -2.15
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun MetricCardNoChangePreview() {
    VettrTheme {
        MetricCard(
            title = "Volume",
            value = "1.2M"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun MetricCardZeroChangePreview() {
    VettrTheme {
        MetricCard(
            title = "P/E Ratio",
            value = "18.5",
            change = 0.0
        )
    }
}
