package com.vettr.android.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Filing type color and display label mapping.
 */
private data class FilingTypeStyle(
    val displayLabel: String,
    val backgroundColor: Color,
    val textColor: Color
)

/**
 * Maps filing type strings to display styles.
 */
private fun getFilingTypeStyle(filingType: String): FilingTypeStyle {
    return when {
        filingType.contains("Press", ignoreCase = true) -> FilingTypeStyle(
            displayLabel = "News Release",
            backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
            textColor = Color(0xFF3B82F6)
        )
        filingType.contains("MD&A", ignoreCase = true) -> FilingTypeStyle(
            displayLabel = "MD&A",
            backgroundColor = Color(0xFF8B5CF6).copy(alpha = 0.2f),
            textColor = Color(0xFF8B5CF6)
        )
        filingType.contains("Financial", ignoreCase = true) -> FilingTypeStyle(
            displayLabel = "Financials",
            backgroundColor = Color(0xFF10B981).copy(alpha = 0.2f),
            textColor = Color(0xFF10B981)
        )
        filingType.contains("Material", ignoreCase = true) -> FilingTypeStyle(
            displayLabel = "Material Change",
            backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
            textColor = Color(0xFFF59E0B)
        )
        else -> FilingTypeStyle(
            displayLabel = "Filing",
            backgroundColor = Color(0xFF64748B).copy(alpha = 0.2f),
            textColor = Color(0xFF64748B)
        )
    }
}

/**
 * FilingTypeBadge displays a colored pill badge for filing types.
 * Maps filing type strings to human-readable labels with color coding.
 *
 * @param filingType The raw filing type string from the API
 * @param modifier Optional modifier for customization
 */
@Composable
fun FilingTypeBadge(
    filingType: String,
    modifier: Modifier = Modifier
) {
    val style = getFilingTypeStyle(filingType)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(style.backgroundColor)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    ) {
        Text(
            text = style.displayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = style.textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun FilingTypeBadgePressPreview() {
    VettrTheme {
        FilingTypeBadge(filingType = "Press Release")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun FilingTypeBadgeMdaPreview() {
    VettrTheme {
        FilingTypeBadge(filingType = "MD&A")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun FilingTypeBadgeFinancialPreview() {
    VettrTheme {
        FilingTypeBadge(filingType = "Financial Statements")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun FilingTypeBadgeMaterialPreview() {
    VettrTheme {
        FilingTypeBadge(filingType = "Material Change")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun FilingTypeBadgeDefaultPreview() {
    VettrTheme {
        FilingTypeBadge(filingType = "Annual Report")
    }
}
