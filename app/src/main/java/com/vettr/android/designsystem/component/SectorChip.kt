package com.vettr.android.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Dimensions
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * SectorChip - Pill-shaped tag displaying sector name
 *
 * @param sector The name of the sector (e.g., "Mining", "Technology", "Energy")
 * @param modifier Optional modifier for customization
 */
@Composable
fun SectorChip(
    sector: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(
                width = 1.dp,
                color = VettrAccent,
                shape = RoundedCornerShape(Dimensions.chipRadius)
            ),
        color = VettrCardBackground,
        shape = RoundedCornerShape(Dimensions.chipRadius)
    ) {
        Text(
            text = sector,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                horizontal = Spacing.md,
                vertical = Spacing.sm
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SectorChipMiningPreview() {
    VettrTheme {
        SectorChip(sector = "Mining")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SectorChipTechnologyPreview() {
    VettrTheme {
        SectorChip(sector = "Technology")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SectorChipEnergyPreview() {
    VettrTheme {
        SectorChip(sector = "Energy")
    }
}
