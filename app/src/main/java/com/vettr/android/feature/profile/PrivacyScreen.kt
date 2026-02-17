package com.vettr.android.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrTextPrimary
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VettrTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VettrTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VettrNavy
                )
            )
        },
        containerColor = VettrNavy,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Text(
                text = "Last updated: February 2026",
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            PrivacySection(
                title = "1. Information We Collect",
                content = "We collect information you provide when creating an account, including your email address and display name. We also collect usage data such as your watchlist preferences, alert configurations, and interaction patterns with the platform."
            )

            PrivacySection(
                title = "2. How We Use Your Information",
                content = "Your information is used to personalize your experience, deliver alerts and notifications, improve platform features, and provide customer support. We analyze aggregated usage patterns to enhance our scoring algorithms and user experience."
            )

            PrivacySection(
                title = "3. Data Storage & Security",
                content = "Your data is stored securely using encrypted databases hosted on trusted cloud infrastructure. We implement industry-standard security measures including encryption at rest and in transit, access controls, and regular security audits."
            )

            PrivacySection(
                title = "4. Third-Party Sharing",
                content = "We do not sell, trade, or share your personal information with third parties for marketing purposes. We may share anonymized, aggregated data for analytics. We may disclose information when required by law or to protect our rights."
            )

            PrivacySection(
                title = "5. Your Rights",
                content = "You have the right to access, correct, or delete your personal data at any time. You can update your profile information through the app settings. To request a full data export or account deletion, contact us at support@vettr.ca."
            )

            PrivacySection(
                title = "6. Cookies & Analytics",
                content = "We use essential data storage to maintain your session and preferences. We may use privacy-respecting analytics to understand platform usage. You can manage data sharing preferences in the app settings."
            )

            PrivacySection(
                title = "7. Changes to This Policy",
                content = "We may update this privacy policy from time to time. We will notify you of significant changes via email or in-app notification. Continued use of the platform after changes constitutes acceptance of the updated policy."
            )

            PrivacySection(
                title = "8. Contact",
                content = "For privacy-related inquiries, please contact us at support@vettr.ca."
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(bottom = Spacing.lg)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = VettrTextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Preview
@Composable
private fun PrivacyScreenPreview() {
    VettrTheme {
        PrivacyScreen()
    }
}
