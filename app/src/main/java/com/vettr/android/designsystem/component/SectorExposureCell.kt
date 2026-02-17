package com.vettr.android.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Sector color mapping for the exposure grid.
 */
object SectorColors {
    val Mining = Color(0xFFF59E0B)
    val Energy = Color(0xFF10B981)
    val Technology = Color(0xFF3B82F6)
    val Cannabis = Color(0xFF8B5CF6)
    val Healthcare = Color(0xFFEF4444)
    val RealEstate = Color(0xFFF97316)
    val Financial = Color(0xFF14B8A6)
    val Other = Color(0xFF64748B)

    fun forSector(sector: String): Color {
        return when {
            sector.contains("Mining", ignoreCase = true) -> Mining
            sector.contains("Energy", ignoreCase = true) -> Energy
            sector.contains("Technology", ignoreCase = true) ||
                sector.contains("Tech", ignoreCase = true) -> Technology
            sector.contains("Cannabis", ignoreCase = true) -> Cannabis
            sector.contains("Healthcare", ignoreCase = true) ||
                sector.contains("Health", ignoreCase = true) -> Healthcare
            sector.contains("Real Estate", ignoreCase = true) -> RealEstate
            sector.contains("Financial", ignoreCase = true) ||
                sector.contains("Finance", ignoreCase = true) -> Financial
            else -> Other
        }
    }
}

/**
 * SectorExposureCell displays a colored sector block in the sector exposure grid.
 * Shows sector name, exchange, stock count and percentage.
 *
 * @param sector The sector name (e.g., "Mining", "Energy")
 * @param exchange The exchange (e.g., "TSX-V", "CSE")
 * @param count Number of stocks in this sector
 * @param pct Percentage of watchlist in this sector
 * @param modifier Optional modifier for customization
 */
@Composable
fun SectorExposureCell(
    sector: String,
    exchange: String,
    count: Int,
    pct: Int,
    modifier: Modifier = Modifier
) {
    val sectorColor = SectorColors.forSector(sector)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(sectorColor.copy(alpha = 0.15f))
            .padding(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Color indicator dot + sector name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(sectorColor)
            )
            Text(
                text = sector,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
        // Exchange
        Text(
            text = exchange,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Count and percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$count stocks",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.labelMedium,
                color = sectorColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SectorExposureCellMiningPreview() {
    VettrTheme {
        SectorExposureCell(
            sector = "Mining",
            exchange = "TSX-V",
            count = 4,
            pct = 35
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun SectorExposureCellTechPreview() {
    VettrTheme {
        SectorExposureCell(
            sector = "Technology",
            exchange = "CSE",
            count = 2,
            pct = 15
        )
    }
}
