package com.vettr.android.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * StockRowView displays a single stock item in a list format.
 *
 * @param ticker The stock ticker symbol
 * @param companyName The full company name
 * @param price The current stock price
 * @param priceChange The price change amount (e.g., 2.50 for +$2.50)
 * @param onClick Callback when the row is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun StockRowView(
    ticker: String,
    companyName: String,
    price: Double,
    priceChange: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .vettrPadding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ticker initials avatar
        val initials = ticker.take(2)
        val avatarColors = listOf(
            VettrAccent, VettrGreen, Color(0xFF6366F1), Color(0xFFEC4899),
            Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFF14B8A6)
        )
        val avatarColor = avatarColors[ticker.hashCode().and(0x7FFFFFFF) % avatarColors.size]
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                color = avatarColor,
                fontWeight = FontWeight.Bold
            )
        }

        // Ticker and company name
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = ticker,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = companyName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Price and change
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = String.format("$%.2f", price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            val isPositive = priceChange >= 0
            val changeColor = if (isPositive) VettrGreen else VettrRed
            val changePrefix = if (isPositive) "+" else ""

            Text(
                text = "$changePrefix${String.format("$%.2f", priceChange)}",
                style = MaterialTheme.typography.bodyMedium,
                color = changeColor
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun StockRowViewPositivePreview() {
    VettrTheme {
        StockRowView(
            ticker = "SHOP",
            companyName = "Shopify Inc.",
            price = 75.50,
            priceChange = 2.35,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun StockRowViewNegativePreview() {
    VettrTheme {
        StockRowView(
            ticker = "RY",
            companyName = "Royal Bank of Canada",
            price = 128.75,
            priceChange = -1.45,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun StockRowViewZeroChangePreview() {
    VettrTheme {
        StockRowView(
            ticker = "ENB",
            companyName = "Enbridge Inc.",
            price = 48.00,
            priceChange = 0.0,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun StockRowViewNoLogoPreview() {
    VettrTheme {
        StockRowView(
            ticker = "TD",
            companyName = "Toronto-Dominion Bank",
            price = 82.15,
            priceChange = 0.85,
            onClick = {}
        )
    }
}
