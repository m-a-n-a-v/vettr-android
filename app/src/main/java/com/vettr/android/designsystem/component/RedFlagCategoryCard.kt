package com.vettr.android.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrWarning

/**
 * RedFlagCategoryCard displays a red flag category with severity indicator.
 * Shows the category name, label, stock count, and a severity-colored indicator.
 *
 * @param category The category name (e.g., "Financial Risk")
 * @param label The specific label (e.g., "High Debt")
 * @param stockCount Number of stocks affected
 * @param severity Severity level: "critical" (red) or "warning" (orange)
 * @param modifier Optional modifier for customization
 */
@Composable
fun RedFlagCategoryCard(
    category: String,
    label: String,
    stockCount: Int,
    severity: String,
    modifier: Modifier = Modifier
) {
    val severityColor = if (severity == "critical") VettrRed else VettrWarning

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(severityColor.copy(alpha = 0.1f))
            .padding(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(severityColor)
            )
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = severityColor,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "$stockCount stock${if (stockCount != 1) "s" else ""}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun RedFlagCategoryCardCriticalPreview() {
    VettrTheme {
        RedFlagCategoryCard(
            category = "Financial Risk",
            label = "High Debt",
            stockCount = 2,
            severity = "critical"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun RedFlagCategoryCardWarningPreview() {
    VettrTheme {
        RedFlagCategoryCard(
            category = "Governance",
            label = "Board Changes",
            stockCount = 3,
            severity = "warning"
        )
    }
}
