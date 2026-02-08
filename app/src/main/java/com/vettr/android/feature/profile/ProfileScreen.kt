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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
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
 * - User header with avatar, name, email, and tier badge
 * - Account settings menu items
 * - Upgrade to Pro card for free users
 * - Logout with confirmation dialog
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val tier by viewModel.tier.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }

    ProfileScreenContent(
        user = user,
        tier = tier,
        isLoading = isLoading,
        showLogoutDialog = showLogoutDialog,
        onShowLogoutDialog = { showLogoutDialog = it },
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
    showLogoutDialog: Boolean,
    onShowLogoutDialog: (Boolean) -> Unit,
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
            // Header section
            ProfileHeader(user = user, tier = tier)

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Menu items
            MenuRow(
                icon = Icons.Default.AccountCircle,
                title = "My Account & Settings",
                onClick = { /* TODO: Navigate to account settings */ }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            MenuRow(
                icon = Icons.Default.Notifications,
                title = "Notification Settings",
                onClick = { /* TODO: Navigate to notification settings */ }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            MenuRow(
                icon = Icons.Default.School,
                title = "Educational Hub",
                onClick = { /* TODO: Navigate to educational hub */ }
            )

            // Upgrade to Pro card (only show for free tier)
            if (tier == VettrTier.FREE) {
                Spacer(modifier = Modifier.height(Spacing.lg))
                UpgradeToProCard()
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Help & Support
            MenuRow(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "Help & Support",
                onClick = { /* TODO: Navigate to help & support */ }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Log Out
            MenuRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                onClick = { onShowLogoutDialog(true) }
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
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
 * Profile header showing avatar, name, email, and tier badge.
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
            // Avatar
            if (user?.avatarUrl != null) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "Profile avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            } else {
                // Default avatar icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(VettrAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default avatar",
                        tint = VettrAccent,
                        modifier = Modifier.size(48.dp)
                    )
                }
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
                contentDescription = null,
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
        onClick = { /* TODO: Navigate to upgrade flow */ },
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
                    text = "$10.99/month",
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    VettrTheme {
        ProfileScreenContent(
            user = User(
                id = "1",
                email = "investor@example.com",
                displayName = "Jane Investor",
                avatarUrl = null,
                tier = "FREE",
                createdAt = System.currentTimeMillis()
            ),
            tier = VettrTier.FREE,
            isLoading = false,
            showLogoutDialog = false,
            onShowLogoutDialog = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenProPreview() {
    VettrTheme {
        ProfileScreenContent(
            user = User(
                id = "1",
                email = "pro@example.com",
                displayName = "Pro Investor",
                avatarUrl = null,
                tier = "PRO",
                createdAt = System.currentTimeMillis()
            ),
            tier = VettrTier.PRO,
            isLoading = false,
            showLogoutDialog = false,
            onShowLogoutDialog = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LogoutDialogPreview() {
    VettrTheme {
        LogoutConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
