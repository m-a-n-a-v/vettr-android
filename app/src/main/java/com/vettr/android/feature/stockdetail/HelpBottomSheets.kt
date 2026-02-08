package com.vettr.android.feature.stockdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Bottom sheet explaining what VETR Score is and how it's calculated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetrScoreHelpBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Title
            Text(
                text = "What is VETR Score?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Description
            Text(
                text = "The VETR Score is a proprietary 0-100 rating that evaluates a company's overall health and investment quality. Higher scores indicate stronger company fundamentals and lower investment risk.",
                style = MaterialTheme.typography.bodyLarge,
                color = VettrTextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // How it's calculated
            Text(
                text = "How is it calculated?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HelpBulletPoint(
                title = "Financial Health (30%)",
                description = "Revenue growth, profit margins, debt ratios, and cash flow strength"
            )

            HelpBulletPoint(
                title = "Management Pedigree (25%)",
                description = "Executive experience, track record, insider ownership, and board composition"
            )

            HelpBulletPoint(
                title = "Filing Consistency (20%)",
                description = "Timely regulatory filings, disclosure quality, and compliance history"
            )

            HelpBulletPoint(
                title = "Market Performance (15%)",
                description = "Price stability, trading volume, market sentiment, and peer comparison"
            )

            HelpBulletPoint(
                title = "Risk Indicators (10%)",
                description = "Red flags, regulatory issues, and other warning signs"
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Score ranges
            Text(
                text = "Score Ranges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            ScoreRangeBullet("80-100", "Excellent - Strong fundamentals and low risk")
            ScoreRangeBullet("60-79", "Good - Solid company with some areas for improvement")
            ScoreRangeBullet("40-59", "Fair - Moderate risk with mixed indicators")
            ScoreRangeBullet("20-39", "Poor - Significant concerns or red flags")
            ScoreRangeBullet("0-19", "Critical - High risk investment")
        }
    }
}

/**
 * Bottom sheet explaining what Red Flags are and how they're detected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedFlagHelpBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Title
            Text(
                text = "What are Red Flags?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Description
            Text(
                text = "Red flags are warning indicators that highlight potential risks or concerns with a company. Our system automatically detects these patterns to help you make informed investment decisions.",
                style = MaterialTheme.typography.bodyLarge,
                color = VettrTextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Types of red flags
            Text(
                text = "Types of Red Flags",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HelpBulletPoint(
                title = "Executive Turnover",
                description = "Frequent changes in C-suite or board members, especially CFO or CEO departures"
            )

            HelpBulletPoint(
                title = "Filing Delays",
                description = "Late or missing regulatory filings, which may indicate internal problems"
            )

            HelpBulletPoint(
                title = "Unusual Trading",
                description = "Suspicious insider trading patterns or abnormal volume spikes"
            )

            HelpBulletPoint(
                title = "Financial Concerns",
                description = "Declining revenue, increasing debt, negative cash flow, or going concern warnings"
            )

            HelpBulletPoint(
                title = "Regulatory Issues",
                description = "Cease trade orders, compliance violations, or investigations"
            )

            HelpBulletPoint(
                title = "Governance Problems",
                description = "Related party transactions, conflicts of interest, or weak board independence"
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Severity levels
            Text(
                text = "Severity Levels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            ScoreRangeBullet("Critical", "Immediate attention required - significant risk to investment")
            ScoreRangeBullet("High", "Serious concern that should be investigated")
            ScoreRangeBullet("Medium", "Notable issue worth monitoring")
            ScoreRangeBullet("Low", "Minor concern or pattern to watch")
        }
    }
}

/**
 * Bottom sheet explaining what Pedigree is and how it's evaluated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedigreeHelpBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Title
            Text(
                text = "What is Pedigree?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Description
            Text(
                text = "Pedigree evaluates the quality and track record of a company's management team and board of directors. Strong leadership is a key indicator of a company's potential for success.",
                style = MaterialTheme.typography.bodyLarge,
                color = VettrTextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // What we analyze
            Text(
                text = "What We Analyze",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HelpBulletPoint(
                title = "Industry Experience",
                description = "Years of relevant experience in the sector and similar roles"
            )

            HelpBulletPoint(
                title = "Track Record",
                description = "Past successes, exits, IPOs, and value creation at previous companies"
            )

            HelpBulletPoint(
                title = "Education & Credentials",
                description = "Relevant degrees, professional designations, and industry certifications"
            )

            HelpBulletPoint(
                title = "Board Composition",
                description = "Independence, diversity, expertise balance, and committee structure"
            )

            HelpBulletPoint(
                title = "Insider Ownership",
                description = "Management's financial stake and alignment with shareholder interests"
            )

            HelpBulletPoint(
                title = "Network & Reputation",
                description = "Industry connections, advisory roles, and professional standing"
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Key positions
            Text(
                text = "Key Executive Roles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            ScoreRangeBullet("CEO", "Chief Executive Officer - overall strategy and operations")
            ScoreRangeBullet("CFO", "Chief Financial Officer - financial management and reporting")
            ScoreRangeBullet("COO", "Chief Operating Officer - day-to-day operations")
            ScoreRangeBullet("Board", "Directors providing oversight and governance")

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "Tap on any executive to view their detailed background, insider trading history, and other companies they're involved with.",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Helper composable for bullet points with title and description.
 */
@Composable
private fun HelpBulletPoint(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "• $title",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = VettrAccent
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

/**
 * Helper composable for score range bullets.
 */
@Composable
private fun ScoreRangeBullet(
    range: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "• $range",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VetrScoreHelpBottomSheetPreview() {
    VettrTheme {
        VetrScoreHelpBottomSheet(onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
fun RedFlagHelpBottomSheetPreview() {
    VettrTheme {
        RedFlagHelpBottomSheet(onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PedigreeHelpBottomSheetPreview() {
    VettrTheme {
        PedigreeHelpBottomSheet(onDismiss = {})
    }
}
