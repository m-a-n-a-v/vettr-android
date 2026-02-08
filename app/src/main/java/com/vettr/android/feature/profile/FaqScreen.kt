package com.vettr.android.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrTextPrimary
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Data class representing an FAQ item.
 */
@androidx.compose.runtime.Immutable
data class FaqItem(
    val question: String,
    val answer: String,
    val category: FaqCategory
)

/**
 * Categories for organizing FAQ items.
 */
enum class FaqCategory(val displayName: String) {
    GETTING_STARTED("Getting Started"),
    STOCKS("Stocks & Watchlists"),
    ALERTS("Alerts & Notifications"),
    SCORING("VETR Scoring"),
    ACCOUNT("Account & Billing")
}

/**
 * FAQ screen showing frequently asked questions with expandable answers.
 * Features 15+ questions organized in expandable sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val faqItems = remember { getAllFaqItems() }
    val groupedFaqs = remember { faqItems.groupBy { it.category } }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FAQ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
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
                    containerColor = VettrNavy,
                    titleContentColor = VettrTextPrimary
                )
            )
        },
        containerColor = VettrNavy
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Spacer(modifier = Modifier.height(Spacing.xs))
            }

            // Render each category section
            FaqCategory.entries.forEach { category ->
                val categoryItems = groupedFaqs[category] ?: emptyList()
                if (categoryItems.isNotEmpty()) {
                    item {
                        CategoryHeader(
                            category = category,
                            modifier = Modifier.padding(horizontal = Spacing.md)
                        )
                    }

                    items(categoryItems, key = { it.question }) { faqItem ->
                        ExpandableFaqCard(
                            faqItem = faqItem,
                            modifier = Modifier.padding(horizontal = Spacing.md)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }
    }
}

/**
 * Category header displaying the category name.
 */
@Composable
private fun CategoryHeader(
    category: FaqCategory,
    modifier: Modifier = Modifier
) {
    Text(
        text = category.displayName,
        style = MaterialTheme.typography.titleMedium,
        color = VettrAccent,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = Spacing.sm)
    )
}

/**
 * Expandable card for a single FAQ item.
 */
@Composable
private fun ExpandableFaqCard(
    faqItem: FaqItem,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

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
                .clickable { isExpanded = !isExpanded }
                .padding(Spacing.md)
        ) {
            // Question row with expand icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faqItem.question,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VettrTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = VettrAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Animated answer section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = faqItem.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VettrTextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Returns all FAQ items organized by category.
 */
private fun getAllFaqItems(): List<FaqItem> {
    return listOf(
        // Getting Started
        FaqItem(
            question = "What is VETTR?",
            answer = "VETTR is an investment intelligence platform designed for venture and micro-cap investors. We provide comprehensive data, analysis, and alerts for TSX-V and CSE listed companies to help you make informed investment decisions.",
            category = FaqCategory.GETTING_STARTED
        ),
        FaqItem(
            question = "How do I get started?",
            answer = "Simply sign up for an account, add stocks to your watchlist, and start exploring company filings, executive information, and our proprietary VETR Score. You can customize alerts to stay informed about the stocks you care about.",
            category = FaqCategory.GETTING_STARTED
        ),
        FaqItem(
            question = "What's the difference between Free, Pro, and Premium tiers?",
            answer = "Free tier includes basic features with daily sync and limited watchlists. Pro tier ($10.99/month) offers unlimited watchlists, 12-hour sync, and priority support. Premium tier provides 4-hour sync, advanced analytics, and exclusive research reports.",
            category = FaqCategory.GETTING_STARTED
        ),

        // Stocks & Watchlists
        FaqItem(
            question = "How do I add stocks to my watchlist?",
            answer = "Navigate to any stock detail page and tap the star icon to add it to your favorites/watchlist. You can also search for stocks in the Discovery tab and add them from there.",
            category = FaqCategory.STOCKS
        ),
        FaqItem(
            question = "What stock exchanges are covered?",
            answer = "VETTR currently covers the TSX Venture Exchange (TSX-V) and Canadian Securities Exchange (CSE), focusing on venture and micro-cap companies in Canada.",
            category = FaqCategory.STOCKS
        ),
        FaqItem(
            question = "How often is stock data updated?",
            answer = "Stock data update frequency depends on your tier: Free tier syncs daily (24 hours), Pro tier syncs every 12 hours, and Premium tier syncs every 4 hours. You can also trigger manual sync at any time.",
            category = FaqCategory.STOCKS
        ),
        FaqItem(
            question = "What information is available for each stock?",
            answer = "For each stock, you can view real-time price data, company filings (SEDAR+), executive information and insider trading, red flags, VETR Score, pedigree analysis, and historical performance charts.",
            category = FaqCategory.STOCKS
        ),

        // Alerts & Notifications
        FaqItem(
            question = "What types of alerts can I create?",
            answer = "You can create price alerts (when a stock reaches a target price), filing alerts (new regulatory filings), executive alerts (insider trading or management changes), and VETR Score alerts (significant score changes).",
            category = FaqCategory.ALERTS
        ),
        FaqItem(
            question = "How do I set up an alert?",
            answer = "Go to the Alerts tab, tap the '+' button, select your alert type, choose the stock and conditions, and save. You'll receive push notifications when the alert is triggered.",
            category = FaqCategory.ALERTS
        ),
        FaqItem(
            question = "Can I customize notification preferences?",
            answer = "Yes! In Settings, you can control which types of notifications you receive, set quiet hours, and choose whether to receive push notifications, email alerts, or both.",
            category = FaqCategory.ALERTS
        ),

        // VETR Scoring
        FaqItem(
            question = "What is a VETR Score?",
            answer = "The VETR Score is a proprietary 0-100 rating that evaluates a company's overall health based on financial metrics, management quality, filing activity, market performance, and other key indicators.",
            category = FaqCategory.SCORING
        ),
        FaqItem(
            question = "How is the VETR Score calculated?",
            answer = "The VETR Score combines multiple factors including financial health (30%), management pedigree (25%), filing consistency (20%), market performance (15%), and risk indicators (10%). Higher scores indicate stronger overall company health.",
            category = FaqCategory.SCORING
        ),
        FaqItem(
            question = "What are red flags?",
            answer = "Red flags are warning indicators that highlight potential risks, such as frequent executive turnover, delayed filings, unusual insider trading patterns, or significant declines in financial metrics.",
            category = FaqCategory.SCORING
        ),
        FaqItem(
            question = "What is the Pedigree Score?",
            answer = "The Pedigree Score evaluates the quality and track record of a company's management team, considering factors like industry experience, past successes, board composition, and insider ownership.",
            category = FaqCategory.SCORING
        ),

        // Account & Billing
        FaqItem(
            question = "How do I upgrade to Pro or Premium?",
            answer = "Tap on your profile, select 'Upgrade to Pro' (or Premium), and follow the payment flow. You can upgrade or downgrade your subscription at any time in Settings.",
            category = FaqCategory.ACCOUNT
        ),
        FaqItem(
            question = "How do I cancel my subscription?",
            answer = "Go to Settings > Subscription > Manage Subscription. You can cancel at any time, and you'll retain Pro/Premium features until the end of your current billing period.",
            category = FaqCategory.ACCOUNT
        ),
        FaqItem(
            question = "Is my data secure?",
            answer = "Yes. VETTR uses bank-level encryption for all data transmission and storage. We never sell your personal information, and you can delete your account and all associated data at any time in Settings.",
            category = FaqCategory.ACCOUNT
        )
    )
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun FaqScreenPreview() {
    VettrTheme {
        FaqScreen()
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun FaqScreenTabletPreview() {
    VettrTheme {
        FaqScreen()
    }
}
