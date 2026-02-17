package com.vettr.android.feature.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vettr.android.core.model.VettrTier
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.designsystem.theme.VettrYellow

/**
 * Tier information for display in the upgrade dialog.
 */
private data class TierDisplayInfo(
    val tier: VettrTier,
    val name: String,
    val price: String,
    val period: String,
    val watchlistLabel: String,
    val pulseLabel: String,
    val features: List<String>,
    val isCurrent: Boolean,
    val isRecommended: Boolean
)

private val TIER_DISPLAY_DATA = mapOf(
    VettrTier.FREE to TierDisplayInfo(
        tier = VettrTier.FREE,
        name = "Free",
        price = "$0",
        period = "forever",
        watchlistLabel = "5 stocks",
        pulseLabel = "12-hour delay",
        features = listOf(
            "Up to 5 watchlist stocks",
            "VETTR Score access",
            "Basic pulse dashboard",
            "Stock discovery collections"
        ),
        isCurrent = false,
        isRecommended = false
    ),
    VettrTier.PRO to TierDisplayInfo(
        tier = VettrTier.PRO,
        name = "Pro",
        price = "$9.99",
        period = "/month",
        watchlistLabel = "25 stocks",
        pulseLabel = "4-hour delay",
        features = listOf(
            "Up to 25 watchlist stocks",
            "VETTR Score with full breakdown",
            "Faster pulse updates (4hr)",
            "Priority stock data sync",
            "CSV export"
        ),
        isCurrent = false,
        isRecommended = false
    ),
    VettrTier.PREMIUM to TierDisplayInfo(
        tier = VettrTier.PREMIUM,
        name = "Premium",
        price = "$24.99",
        period = "/month",
        watchlistLabel = "Unlimited",
        pulseLabel = "Real-time",
        features = listOf(
            "Unlimited watchlist stocks",
            "Full VETTR Score analytics",
            "Real-time pulse updates",
            "Priority data sync (4hr)",
            "CSV export",
            "Early access to new features"
        ),
        isCurrent = false,
        isRecommended = false
    )
)

/**
 * Full-screen upgrade dialog showing available tier options.
 *
 * @param isVisible Whether the dialog is currently showing
 * @param onDismiss Callback when the dialog is dismissed
 * @param currentTier The user's current subscription tier
 * @param currentCount Current number of watchlist stocks
 * @param currentLimit Maximum watchlist stocks for the current tier
 */
@Composable
fun UpgradeDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    currentTier: VettrTier,
    currentCount: Int,
    currentLimit: Int
) {
    if (!isVisible || currentTier == VettrTier.PREMIUM) return

    val tierOrder = listOf(VettrTier.FREE, VettrTier.PRO, VettrTier.PREMIUM)
    val currentIndex = tierOrder.indexOf(currentTier)
    val nextIndex = currentIndex + 1

    val visibleTiers = tierOrder.subList(currentIndex, tierOrder.size).map { tier ->
        val baseInfo = TIER_DISPLAY_DATA[tier]!!
        baseInfo.copy(
            isCurrent = tier == currentTier,
            isRecommended = tierOrder.indexOf(tier) == nextIndex
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upgrade Your Plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VettrTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Limit reached banner
                if (currentLimit != Int.MAX_VALUE) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = VettrYellow.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = VettrYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Watchlist limit reached",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VettrYellow
                                )
                                Text(
                                    text = "$currentCount/$currentLimit stocks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VettrTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                // Tier cards
                visibleTiers.forEach { tierInfo ->
                    TierCard(
                        tierInfo = tierInfo,
                        onUpgradeClick = {
                            // In production, this would open Google Play billing flow
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                // Footer
                Text(
                    text = "Upgrade and downgrade anytime. Plans billed monthly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm)
                )
            }
        }
    }
}

@Composable
private fun TierCard(
    tierInfo: TierDisplayInfo,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        tierInfo.isRecommended -> VettrAccent.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (tierInfo.isRecommended) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = VettrAccent.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (tierInfo.isCurrent) {
                VettrCardBackground.copy(alpha = 0.5f)
            } else if (tierInfo.isRecommended) {
                VettrAccent.copy(alpha = 0.05f)
            } else {
                VettrCardBackground
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tierInfo.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (tierInfo.isRecommended) VettrAccent else MaterialTheme.colorScheme.onSurface
                )

                if (tierInfo.isCurrent) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = VettrTextSecondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "CURRENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VettrTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                } else if (tierInfo.isRecommended) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = VettrAccent.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "RECOMMENDED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VettrAccent,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Price
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = tierInfo.price,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tierInfo.period,
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Key stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = VettrYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = tierInfo.watchlistLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = VettrAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = tierInfo.pulseLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Features
            tierInfo.features.forEach { feature ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = VettrAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // CTA button
            if (tierInfo.isCurrent) {
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Current Plan",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (tierInfo.isRecommended) {
                        ButtonDefaults.buttonColors(
                            containerColor = VettrAccent,
                            contentColor = Color(0xFF0D1B2A) // VettrNavy
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Upgrade to ${tierInfo.name}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun UpgradeDialogPreview() {
    VettrTheme {
        UpgradeDialog(
            isVisible = true,
            onDismiss = {},
            currentTier = VettrTier.FREE,
            currentCount = 5,
            currentLimit = 5
        )
    }
}
