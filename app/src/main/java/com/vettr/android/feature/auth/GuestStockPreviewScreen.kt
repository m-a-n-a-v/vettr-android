package com.vettr.android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.data.remote.PillarBreakdownDto
import com.vettr.android.core.data.remote.PillarScoreDto
import com.vettr.android.core.data.remote.StockPreviewDto
import com.vettr.android.designsystem.component.PrimaryButton
import com.vettr.android.designsystem.component.VettrScoreView
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Guest Stock Preview screen showing limited stock data with blurred locked content.
 *
 * Displays:
 * - Stock header with ticker, name, exchange, and current price
 * - VETTR Score badge
 * - 4-pillar breakdown progress bars
 * - Blurred placeholder sections for full analysis content
 * - CTA overlay card prompting the user to create an account
 *
 * @param onBackClick Callback when user taps the back arrow
 * @param onCreateAccountClick Callback when user taps "Create Account"
 * @param onSignInClick Callback when user taps "Sign In"
 * @param viewModel GuestStockPreviewViewModel providing stock preview state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestStockPreviewScreen(
    onBackClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    viewModel: GuestStockPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top app bar
        TopAppBar(
            title = {
                Text(
                    text = uiState.stockPreview?.ticker ?: "Stock Preview",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: "Error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        TextButton(onClick = { viewModel.retry() }) {
                            Text("Retry", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            uiState.stockPreview != null -> {
                StockPreviewContent(
                    preview = uiState.stockPreview!!,
                    onCreateAccountClick = onCreateAccountClick,
                    onSignInClick = onSignInClick
                )
            }
        }
    }
}

@Composable
private fun StockPreviewContent(
    preview: StockPreviewDto,
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
        ) {
            // Stock header
            StockHeaderSection(preview)

            Spacer(modifier = Modifier.height(Spacing.lg))

            // VETTR Score
            if (preview.vetrScore != null) {
                VettrScoreSection(preview.vetrScore)
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            // 4-Pillar Breakdown
            if (preview.pillars != null) {
                PillarBreakdownSection(preview.pillars)
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            // Blurred locked content sections
            BlurredLockedSection(title = "Full Analysis Report")
            Spacer(modifier = Modifier.height(Spacing.md))
            BlurredLockedSection(title = "Red Flag Detection")
            Spacer(modifier = Modifier.height(Spacing.md))
            BlurredLockedSection(title = "Executive Pedigree")

            // Bottom spacing for CTA overlay
            Spacer(modifier = Modifier.height(200.dp))
        }

        // CTA overlay at the bottom
        ConversionCtaOverlay(
            onCreateAccountClick = onCreateAccountClick,
            onSignInClick = onSignInClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun StockHeaderSection(preview: StockPreviewDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = preview.ticker,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    if (preview.companyName != null) {
                        Text(
                            text = preview.companyName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (preview.exchange != null) {
                        Text(
                            text = preview.exchange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Price
                if (preview.currentPrice != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${String.format("%.2f", preview.currentPrice)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        if (preview.priceChangePercent != null) {
                            val changeColor = if (preview.priceChangePercent >= 0) VettrGreen else VettrRed
                            val changePrefix = if (preview.priceChangePercent >= 0) "+" else ""
                            Text(
                                text = "$changePrefix${String.format("%.2f", preview.priceChangePercent)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = changeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Market cap
            if (preview.marketCap != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "Market Cap: ${formatMarketCap(preview.marketCap)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VettrScoreSection(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VETR Score",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            VettrScoreView(score = score, size = 100.dp)
        }
    }
}

@Composable
private fun PillarBreakdownSection(pillars: PillarBreakdownDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = "Pillar Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            pillars.financialSurvival?.let {
                PillarBar(name = "Financial Survival", pillar = it)
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
            pillars.operationalEfficiency?.let {
                PillarBar(name = "Operational Efficiency", pillar = it)
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
            pillars.shareholderStructure?.let {
                PillarBar(name = "Shareholder Structure", pillar = it)
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
            pillars.marketSentiment?.let {
                PillarBar(name = "Market Sentiment", pillar = it)
            }
        }
    }
}

@Composable
private fun PillarBar(name: String, pillar: PillarScoreDto) {
    val score = pillar.score ?: 0.0
    val normalizedProgress = (score / 100.0).coerceIn(0.0, 1.0).toFloat()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${score.toInt()}/100",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { normalizedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

/**
 * Blurred placeholder section representing locked content.
 * Shows a blurred box with a lock icon overlay to indicate premium content.
 */
@Composable
private fun BlurredLockedSection(title: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Blurred content placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .blur(8.dp),
            colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                // Fake content lines
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = if (it == 3) 0.6f else 0.9f)
                            .height(12.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            }
        }

        // Lock overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked content",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(Spacing.md)
            )
        }
    }
}

/**
 * CTA overlay card at the bottom of the screen prompting the user to sign up.
 */
@Composable
private fun ConversionCtaOverlay(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            Text(
                text = "Unlock Full Due Diligence Report",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Get complete analysis, red flag detection, executive pedigree, and AI-powered insights.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            PrimaryButton(
                text = "Create Free Account",
                onClick = onCreateAccountClick,
                fullWidth = true,
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            TextButton(onClick = onSignInClick) {
                Text(
                    text = "Already have an account? Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Format market cap value into human-readable string.
 */
private fun formatMarketCap(value: Double): String {
    return when {
        value >= 1_000_000_000 -> "$${String.format("%.1f", value / 1_000_000_000)}B"
        value >= 1_000_000 -> "$${String.format("%.1f", value / 1_000_000)}M"
        value >= 1_000 -> "$${String.format("%.1f", value / 1_000)}K"
        else -> "$${String.format("%.0f", value)}"
    }
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun GuestStockPreviewScreenContentPreview() {
    VettrTheme {
        StockPreviewContent(
            preview = StockPreviewDto(
                ticker = "NXE.TO",
                companyName = "NexGen Energy Ltd.",
                exchange = "TSX",
                sector = "Mining",
                marketCap = 5_200_000_000.0,
                currentPrice = 11.45,
                priceChangePercent = 2.35,
                vetrScore = 78,
                pillars = PillarBreakdownDto(
                    financialSurvival = PillarScoreDto(score = 82.0, weight = 0.30),
                    operationalEfficiency = PillarScoreDto(score = 71.0, weight = 0.25),
                    shareholderStructure = PillarScoreDto(score = 65.0, weight = 0.25),
                    marketSentiment = PillarScoreDto(score = 88.0, weight = 0.20)
                )
            ),
            onCreateAccountClick = {},
            onSignInClick = {}
        )
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun GuestStockPreviewScreenTabletPreview() {
    VettrTheme {
        StockPreviewContent(
            preview = StockPreviewDto(
                ticker = "AAPL",
                companyName = "Apple Inc.",
                exchange = "NASDAQ",
                sector = "Technology",
                marketCap = 3_000_000_000_000.0,
                currentPrice = 178.72,
                priceChangePercent = -0.85,
                vetrScore = 91,
                pillars = null
            ),
            onCreateAccountClick = {},
            onSignInClick = {}
        )
    }
}
