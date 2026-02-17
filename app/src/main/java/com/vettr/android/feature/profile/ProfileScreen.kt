package com.vettr.android.feature.profile

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.model.User
import com.vettr.android.core.model.VettrTier
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrTextPrimary
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Profile screen - displays user profile information and account settings menu.
 * Features:
 * - User header with initials avatar, name, email, and tier badge
 * - Subscription section (plan, watchlist limit, stocks tracked)
 * - Data Sync section (last sync, sync now button)
 * - Data & Storage (favorites count, clear cache)
 * - Help & Learning (Glossary, FAQ)
 * - About (version, terms, privacy)
 * - Logout with confirmation dialog
 *
 * All data is sourced from live repositories.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToGlossary: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val tier by viewModel.tier.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val stockCount by viewModel.stockCount.collectAsStateWithLifecycle()
    val favoritesCount by viewModel.favoritesCount.collectAsStateWithLifecycle()
    val cacheCleared by viewModel.cacheCleared.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }

    ProfileScreenContent(
        user = user,
        tier = tier,
        isLoading = isLoading,
        lastSyncTime = lastSyncTime,
        isSyncing = isSyncing,
        nextSyncEta = viewModel.getNextSyncEta(),
        appVersion = viewModel.getAppVersion(),
        buildType = viewModel.getBuildType(),
        stockCount = stockCount,
        favoritesCount = favoritesCount,
        cacheCleared = cacheCleared,
        showLogoutDialog = showLogoutDialog,
        onShowLogoutDialog = { showLogoutDialog = it },
        onManualSync = { viewModel.triggerManualSync() },
        onClearCache = { viewModel.clearCache() },
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToGlossary = onNavigateToGlossary,
        onNavigateToFaq = onNavigateToFaq,
        onNavigateToTerms = onNavigateToTerms,
        onNavigateToPrivacy = onNavigateToPrivacy,
        onNavigateToContact = onNavigateToContact,
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        modifier = modifier
    )
}

@Composable
private fun ProfileScreenContent(
    user: User?,
    tier: VettrTier,
    isLoading: Boolean,
    lastSyncTime: Long?,
    isSyncing: Boolean,
    nextSyncEta: Long?,
    appVersion: String,
    buildType: String,
    stockCount: Int,
    favoritesCount: Int,
    cacheCleared: Boolean,
    showLogoutDialog: Boolean,
    onShowLogoutDialog: (Boolean) -> Unit,
    onManualSync: () -> Unit,
    onClearCache: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToGlossary: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToContact: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = VettrNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md)
        ) {
            // Profile Header section with initials avatar
            ProfileHeader(user = user, tier = tier)

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Subscription section
            SubscriptionSection(
                tier = tier,
                stockCount = stockCount
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Sync section
            SyncSection(
                tier = tier,
                lastSyncTime = lastSyncTime,
                isSyncing = isSyncing,
                nextSyncEta = nextSyncEta,
                onManualSync = onManualSync
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Settings
            MenuRow(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Data & Storage section
            DataStorageSection(
                favoritesCount = favoritesCount,
                cacheCleared = cacheCleared,
                onClearCache = onClearCache
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Help & Learning section
            Text(
                text = "Help & Learning",
                style = MaterialTheme.typography.titleMedium,
                color = VettrAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            MenuRow(
                icon = Icons.Default.School,
                title = "Glossary",
                onClick = onNavigateToGlossary
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            MenuRow(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "FAQ",
                onClick = onNavigateToFaq
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Legal & Support section
            Text(
                text = "Legal & Support",
                style = MaterialTheme.typography.titleMedium,
                color = VettrAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            MenuRow(
                icon = Icons.Default.Description,
                title = "Terms of Service",
                onClick = onNavigateToTerms
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            MenuRow(
                icon = Icons.Default.Shield,
                title = "Privacy Policy",
                onClick = onNavigateToPrivacy
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            MenuRow(
                icon = Icons.Default.Email,
                title = "Contact Support",
                onClick = onNavigateToContact
            )

            // Upgrade to Pro card (only show for free tier)
            if (tier == VettrTier.FREE) {
                Spacer(modifier = Modifier.height(Spacing.lg))
                UpgradeToProCard()
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // About section
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = VettrAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            MenuRow(
                icon = Icons.Default.Info,
                title = "About VETTR",
                onClick = onNavigateToAbout
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Log Out
            MenuRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                onClick = { onShowLogoutDialog(true) }
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Version info at the bottom
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VETTR v$appVersion",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Build: $buildType",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                onShowLogoutDialog(false)
                onLogout()
            },
            onDismiss = { onShowLogoutDialog(false) }
        )
    }
}

/**
 * Profile header showing initials avatar, name, email, and tier badge.
 * Uses the first letters of the display name as the avatar initials (matching iOS).
 */
@Composable
private fun ProfileHeader(
    user: User?,
    tier: VettrTier,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VettrCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Initials-based avatar (matching iOS)
            val initials = getInitials(user?.displayName)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(VettrAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    color = VettrAccent,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Name
            Text(
                text = user?.displayName ?: "Guest User",
                style = MaterialTheme.typography.titleLarge,
                color = VettrTextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Email
            Text(
                text = user?.email ?: "guest@vettr.com",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Tier badge
            TierBadge(tier = tier)
        }
    }
}

/**
 * Extract initials from display name.
 * Takes the first letter of each word (max 2 letters).
 */
private fun getInitials(displayName: String?): String {
    if (displayName.isNullOrBlank()) return "G"
    val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        parts.size == 1 -> parts[0].first().uppercase()
        else -> "G"
    }
}

/**
 * Subscription section showing current plan, watchlist limit, and stocks tracked.
 * All counts come from live repository data.
 */
@Composable
private fun SubscriptionSection(
    tier: VettrTier,
    stockCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VettrCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Subscription",
                    tint = VettrAccent,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                Text(
                    text = "Subscription",
                    style = MaterialTheme.typography.titleMedium,
                    color = VettrTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Current plan
            InfoRow(
                label = "Current Plan",
                value = when (tier) {
                    VettrTier.FREE -> "Free"
                    VettrTier.PRO -> "Pro"
                    VettrTier.PREMIUM -> "Premium"
                }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Watchlist limit
            val watchlistLimitText = if (tier.watchlistLimit == Int.MAX_VALUE) {
                "Unlimited"
            } else {
                "${tier.watchlistLimit}"
            }
            InfoRow(
                label = "Watchlist Limit",
                value = watchlistLimitText
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Stocks tracked (live from Room)
            InfoRow(
                label = "Stocks Tracked",
                value = "$stockCount"
            )
        }
    }
}

/**
 * Data & Storage section showing favorites count and clear cache button.
 */
@Composable
private fun DataStorageSection(
    favoritesCount: Int,
    cacheCleared: Boolean,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VettrCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Data & Storage",
                    tint = VettrAccent,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                Text(
                    text = "Data & Storage",
                    style = MaterialTheme.typography.titleMedium,
                    color = VettrTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Favorites count (live from Room)
            InfoRow(
                label = "Favorites",
                value = "$favoritesCount stock${if (favoritesCount != 1) "s" else ""}"
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            HorizontalDivider(
                color = VettrTextSecondary.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Clear cache button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Cache",
                        tint = VettrTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "Clear Cache",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VettrTextPrimary
                    )
                }

                if (cacheCleared) {
                    Text(
                        text = "Cleared",
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    TextButton(onClick = onClearCache) {
                        Text(
                            text = "Clear",
                            color = VettrAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable info row for label-value pairs.
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextPrimary
        )
    }
}

/**
 * Sync section showing sync status and manual sync button.
 */
@Composable
private fun SyncSection(
    tier: VettrTier,
    lastSyncTime: Long?,
    isSyncing: Boolean,
    nextSyncEta: Long?,
    onManualSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VettrCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync",
                    tint = VettrAccent,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                Text(
                    text = "Data Sync",
                    style = MaterialTheme.typography.titleMedium,
                    color = VettrTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Last sync time
            InfoRow(
                label = "Last sync",
                value = lastSyncTime?.let { formatRelativeTime(it) } ?: "Never"
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Next sync ETA
            InfoRow(
                label = "Next sync",
                value = nextSyncEta?.let { formatNextSync(it) } ?: "Unknown"
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Sync frequency based on tier
            InfoRow(
                label = "Sync frequency",
                value = getSyncFrequencyText(tier)
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Manual sync button
            Button(
                onClick = onManualSync,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = VettrNavy,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "Syncing...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync Now",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "Sync Now",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Format timestamp as relative time (e.g., "2 minutes ago").
 */
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
        days < 7 -> "$days day${if (days != 1L) "s" else ""} ago"
        else -> "${days / 7} week${if (days / 7 != 1L) "s" else ""} ago"
    }
}

/**
 * Format next sync ETA.
 */
private fun formatNextSync(eta: Long): String {
    val now = System.currentTimeMillis()
    val diff = eta - now

    if (diff <= 0) {
        return "Soon"
    }

    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 60 -> "In $minutes minute${if (minutes != 1L) "s" else ""}"
        hours < 24 -> "In $hours hour${if (hours != 1L) "s" else ""}"
        else -> "In $days day${if (days != 1L) "s" else ""}"
    }
}

/**
 * Get sync frequency text based on tier.
 */
private fun getSyncFrequencyText(tier: VettrTier): String {
    return when (tier) {
        VettrTier.FREE -> "Daily (24h)"
        VettrTier.PRO -> "Every 12 hours"
        VettrTier.PREMIUM -> "Every 4 hours"
    }
}

/**
 * Tier badge showing the user's subscription level.
 */
@Composable
private fun TierBadge(
    tier: VettrTier,
    modifier: Modifier = Modifier
) {
    val tierText = when (tier) {
        VettrTier.FREE -> "Free"
        VettrTier.PRO -> "Pro"
        VettrTier.PREMIUM -> "Premium"
    }

    val tierColor = when (tier) {
        VettrTier.FREE -> VettrTextSecondary
        VettrTier.PRO -> VettrAccent
        VettrTier.PREMIUM -> VettrAccent
    }

    Box(
        modifier = modifier
            .background(
                color = tierColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = tierText.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = tierColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Menu row with icon, title, and chevron.
 */
@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VettrCardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = VettrAccent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = VettrTextPrimary,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = VettrTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Upgrade to Pro card promoting the premium subscription.
 */
@Composable
private fun UpgradeToProCard(
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { /* Upgrade flow handled via in-app dialog */ },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VettrAccent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Text(
                text = "Upgrade to Pro",
                style = MaterialTheme.typography.titleMedium,
                color = VettrNavy,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Unlock unlimited watchlists, faster updates, and more",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrNavy.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Learn More",
                    style = MaterialTheme.typography.titleMedium,
                    color = VettrNavy,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Upgrade",
                    tint = VettrNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Logout confirmation dialog.
 */
@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VettrCardBackground,
        title = {
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.titleMedium,
                color = VettrTextPrimary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out?",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Log Out",
                    color = VettrAccent
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = VettrTextSecondary
                )
            }
        }
    )
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ProfileScreenPreview() {
    VettrTheme {
        ProfileScreenContent(
            user = User(
                id = "1",
                email = "demo@vettr.com",
                displayName = "Demo User",
                avatarUrl = null,
                tier = "FREE",
                createdAt = System.currentTimeMillis()
            ),
            tier = VettrTier.FREE,
            isLoading = false,
            lastSyncTime = System.currentTimeMillis() - (2 * 60 * 1000), // 2 minutes ago
            isSyncing = false,
            nextSyncEta = System.currentTimeMillis() + (23 * 60 * 60 * 1000), // 23 hours from now
            appVersion = "1.0.0 (1)",
            buildType = "Debug",
            stockCount = 25,
            favoritesCount = 3,
            cacheCleared = false,
            showLogoutDialog = false,
            onShowLogoutDialog = {},
            onManualSync = {},
            onClearCache = {},
            onNavigateToSettings = {},
            onNavigateToAbout = {},
            onNavigateToGlossary = {},
            onNavigateToFaq = {},
            onNavigateToTerms = {},
            onNavigateToPrivacy = {},
            onNavigateToContact = {},
            onLogout = {}
        )
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun ProfileScreenTabletPreview() {
    VettrTheme {
        ProfileScreenContent(
            user = User(
                id = "1",
                email = "demo@vettr.com",
                displayName = "Demo User",
                avatarUrl = null,
                tier = "FREE",
                createdAt = System.currentTimeMillis()
            ),
            tier = VettrTier.FREE,
            isLoading = false,
            lastSyncTime = System.currentTimeMillis() - (2 * 60 * 1000), // 2 minutes ago
            isSyncing = false,
            nextSyncEta = System.currentTimeMillis() + (23 * 60 * 60 * 1000), // 23 hours from now
            appVersion = "1.0.0 (1)",
            buildType = "Debug",
            stockCount = 25,
            favoritesCount = 3,
            cacheCleared = false,
            showLogoutDialog = false,
            onShowLogoutDialog = {},
            onManualSync = {},
            onClearCache = {},
            onNavigateToSettings = {},
            onNavigateToAbout = {},
            onNavigateToGlossary = {},
            onNavigateToFaq = {},
            onNavigateToTerms = {},
            onNavigateToPrivacy = {},
            onNavigateToContact = {},
            onLogout = {}
        )
    }
}
