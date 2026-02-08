package com.vettr.android.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vettr.android.designsystem.theme.Spacing
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
 * @param logoUrl URL for the company logo image
 * @param onClick Callback when the row is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun StockRowView(
    ticker: String,
    companyName: String,
    price: Double,
    priceChange: Double,
    logoUrl: String?,
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
        // Company logo
        AsyncImage(
            model = logoUrl,
            contentDescription = "$companyName logo",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

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
            logoUrl = "https://logo.clearbit.com/shopify.com",
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
            logoUrl = "https://logo.clearbit.com/rbc.com",
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
            logoUrl = "https://logo.clearbit.com/enbridge.com",
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
            logoUrl = null,
            onClick = {}
        )
    }
}
