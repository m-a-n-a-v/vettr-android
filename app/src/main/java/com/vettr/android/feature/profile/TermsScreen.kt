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
fun TermsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms of Service",
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

            TermsSection(
                title = "1. Use of the Platform",
                content = "VETTR provides a research and analytics platform for evaluating small-cap and venture stocks listed on the TSX Venture Exchange (TSX-V), Canadian Securities Exchange (CSE), and related markets. By using VETTR, you agree to use the platform solely for informational and research purposes."
            )

            TermsSection(
                title = "2. Not Financial Advice",
                content = "The VETTR Score, analytics, alerts, and all information provided on this platform are for informational purposes only and do not constitute financial advice, investment recommendations, or solicitations to buy or sell securities. Always consult a qualified financial advisor before making investment decisions."
            )

            TermsSection(
                title = "3. Data Sources",
                content = "VETTR aggregates data from publicly available sources including SEDAR+, TSX-V, CSE filings, and other regulatory databases. While we strive for accuracy, we do not guarantee the completeness, timeliness, or accuracy of any data presented on the platform."
            )

            TermsSection(
                title = "4. User Responsibility",
                content = "You are solely responsible for your investment decisions. VETTR is a research tool and the information provided should be one of many factors in your investment analysis. Past performance indicators and scores do not guarantee future results."
            )

            TermsSection(
                title = "5. Intellectual Property",
                content = "The VETTR Score methodology, algorithms, user interface, and all original content are the intellectual property of VETTR. You may not reproduce, distribute, or create derivative works from any proprietary content without prior written permission."
            )

            TermsSection(
                title = "6. Limitation of Liability",
                content = "VETTR shall not be liable for any losses, damages, or expenses arising from the use of this platform or reliance on the information provided. This includes but is not limited to investment losses, data inaccuracies, or service interruptions."
            )

            TermsSection(
                title = "7. Account Terms",
                content = "You are responsible for maintaining the security of your account credentials. VETTR reserves the right to suspend or terminate accounts that violate these terms, engage in abusive behavior, or attempt to circumvent platform limitations."
            )

            TermsSection(
                title = "8. Governing Law",
                content = "These terms are governed by and construed in accordance with the laws of Canada. Any disputes arising from the use of this platform shall be subject to the exclusive jurisdiction of the courts of Canada."
            )

            TermsSection(
                title = "9. Contact",
                content = "For questions about these terms, please contact us at support@vettr.ca."
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun TermsSection(
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
private fun TermsScreenPreview() {
    VettrTheme {
        TermsScreen()
    }
}
