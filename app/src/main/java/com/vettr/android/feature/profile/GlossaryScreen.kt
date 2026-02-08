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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * Data class representing a glossary term.
 */
@androidx.compose.runtime.Immutable
data class GlossaryTerm(
    val term: String,
    val definition: String,
    val category: GlossaryCategory
)

/**
 * Categories for organizing glossary terms.
 */
enum class GlossaryCategory(val displayName: String) {
    ALL("All"),
    STOCKS("Stocks"),
    FILINGS("Filings"),
    EXECUTIVES("Executives"),
    METRICS("Metrics"),
    ALERTS("Alerts")
}

/**
 * Glossary screen showing investment terminology and definitions.
 * Features searchable and filterable list of 25+ terms organized by category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GlossaryCategory.ALL) }

    val allTerms = remember { getAllGlossaryTerms() }

    // Filter terms based on search query and selected category
    val filteredTerms by remember {
        derivedStateOf {
            allTerms.filter { term ->
                val matchesSearch = searchQuery.isEmpty() ||
                    term.term.contains(searchQuery, ignoreCase = true) ||
                    term.definition.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == GlossaryCategory.ALL ||
                    term.category == selectedCategory
                matchesSearch && matchesCategory
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Glossary",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClearClick = { searchQuery = "" },
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )

            // Category filter chips
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )

            // Results count
            Text(
                text = "${filteredTerms.size} term${if (filteredTerms.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
            )

            // Terms list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                item {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                }

                items(filteredTerms, key = { it.term }) { term ->
                    GlossaryTermCard(
                        term = term,
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
            }
        }
    }
}

/**
 * Search bar for filtering glossary terms.
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search terms...",
                color = VettrTextSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = VettrAccent
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = VettrTextSecondary
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = VettrCardBackground,
            unfocusedContainerColor = VettrCardBackground,
            focusedTextColor = VettrTextPrimary,
            unfocusedTextColor = VettrTextPrimary,
            cursorColor = VettrAccent,
            focusedIndicatorColor = VettrAccent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

/**
 * Row of filter chips for selecting category.
 */
@Composable
private fun CategoryFilterRow(
    selectedCategory: GlossaryCategory,
    onCategorySelected: (GlossaryCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        GlossaryCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = VettrCardBackground,
                    selectedContainerColor = VettrAccent,
                    labelColor = VettrTextSecondary,
                    selectedLabelColor = VettrNavy
                )
            )
        }
    }
}

/**
 * Card displaying a single glossary term and definition.
 */
@Composable
private fun GlossaryTermCard(
    term: GlossaryTerm,
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
            // Term name
            Text(
                text = term.term,
                style = MaterialTheme.typography.titleMedium,
                color = VettrAccent,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Category badge
            Box(
                modifier = Modifier
                    .background(
                        color = VettrTextSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = term.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Definition
            Text(
                text = term.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextPrimary
            )
        }
    }
}

/**
 * Returns all glossary terms organized by category.
 */
private fun getAllGlossaryTerms(): List<GlossaryTerm> {
    return listOf(
        // Stocks category
        GlossaryTerm(
            term = "TSX-V",
            definition = "TSX Venture Exchange - a Canadian stock exchange focused on emerging companies and venture capital investments.",
            category = GlossaryCategory.STOCKS
        ),
        GlossaryTerm(
            term = "CSE",
            definition = "Canadian Securities Exchange - an alternative stock exchange in Canada for emerging and growing companies.",
            category = GlossaryCategory.STOCKS
        ),
        GlossaryTerm(
            term = "Ticker Symbol",
            definition = "A unique series of letters assigned to a security or stock for trading purposes.",
            category = GlossaryCategory.STOCKS
        ),
        GlossaryTerm(
            term = "Market Cap",
            definition = "Market Capitalization - the total dollar market value of a company's outstanding shares, calculated by multiplying the share price by the total number of outstanding shares.",
            category = GlossaryCategory.STOCKS
        ),
        GlossaryTerm(
            term = "Micro-cap",
            definition = "Stocks with a market capitalization typically between $50 million and $300 million. These are considered higher risk but potentially higher reward investments.",
            category = GlossaryCategory.STOCKS
        ),
        GlossaryTerm(
            term = "Venture Capital",
            definition = "Financing provided to startups and small businesses with perceived long-term growth potential.",
            category = GlossaryCategory.STOCKS
        ),

        // Filings category
        GlossaryTerm(
            term = "SEDAR+",
            definition = "System for Electronic Document Analysis and Retrieval Plus - Canada's official filing system for public companies to submit regulatory documents.",
            category = GlossaryCategory.FILINGS
        ),
        GlossaryTerm(
            term = "MD&A",
            definition = "Management's Discussion and Analysis - a section of a company's annual report where management discusses the company's performance, financial condition, and future prospects.",
            category = GlossaryCategory.FILINGS
        ),
        GlossaryTerm(
            term = "Press Release",
            definition = "Official statement issued by a company to provide information about significant events, financial results, or other material changes.",
            category = GlossaryCategory.FILINGS
        ),
        GlossaryTerm(
            term = "Material Change Report",
            definition = "A mandatory filing that must be submitted when a company experiences a significant change that could affect the value of its securities.",
            category = GlossaryCategory.FILINGS
        ),
        GlossaryTerm(
            term = "Insider Report",
            definition = "A disclosure of trades made by company insiders (executives, directors, and significant shareholders) in their own company's stock.",
            category = GlossaryCategory.FILINGS
        ),

        // Executives category
        GlossaryTerm(
            term = "C-Suite",
            definition = "The group of the most senior executives in a company, typically including CEO, CFO, COO, and other Chief officers.",
            category = GlossaryCategory.EXECUTIVES
        ),
        GlossaryTerm(
            term = "CEO",
            definition = "Chief Executive Officer - the highest-ranking executive in a company, responsible for overall operations and strategy.",
            category = GlossaryCategory.EXECUTIVES
        ),
        GlossaryTerm(
            term = "CFO",
            definition = "Chief Financial Officer - the executive responsible for managing the company's financial actions, planning, and reporting.",
            category = GlossaryCategory.EXECUTIVES
        ),
        GlossaryTerm(
            term = "Board of Directors",
            definition = "A group of individuals elected to represent shareholders and oversee company management.",
            category = GlossaryCategory.EXECUTIVES
        ),
        GlossaryTerm(
            term = "Insider Trading",
            definition = "The buying or selling of a company's securities by individuals with access to non-public, material information. Must be reported to regulators.",
            category = GlossaryCategory.EXECUTIVES
        ),

        // Metrics category
        GlossaryTerm(
            term = "VETR Score",
            definition = "A proprietary 0-100 score that evaluates a company's overall health based on financials, management quality, filing activity, and market performance.",
            category = GlossaryCategory.METRICS
        ),
        GlossaryTerm(
            term = "Red Flag",
            definition = "Warning indicators that suggest potential risks, such as frequent executive turnover, delayed filings, or unusual trading patterns.",
            category = GlossaryCategory.METRICS
        ),
        GlossaryTerm(
            term = "Pedigree Score",
            definition = "A measure of management team quality based on executive backgrounds, track records, and industry experience.",
            category = GlossaryCategory.METRICS
        ),
        GlossaryTerm(
            term = "Volume",
            definition = "The number of shares traded during a given period. High volume often indicates strong investor interest.",
            category = GlossaryCategory.METRICS
        ),
        GlossaryTerm(
            term = "52-Week High/Low",
            definition = "The highest and lowest prices at which a stock has traded over the past year.",
            category = GlossaryCategory.METRICS
        ),
        GlossaryTerm(
            term = "P/E Ratio",
            definition = "Price-to-Earnings Ratio - a valuation metric calculated by dividing the current share price by earnings per share. Used to assess if a stock is overvalued or undervalued.",
            category = GlossaryCategory.METRICS
        ),

        // Alerts category
        GlossaryTerm(
            term = "Price Alert",
            definition = "A notification triggered when a stock reaches a specific price threshold you've set.",
            category = GlossaryCategory.ALERTS
        ),
        GlossaryTerm(
            term = "Filing Alert",
            definition = "A notification sent when a company in your watchlist submits a new regulatory filing.",
            category = GlossaryCategory.ALERTS
        ),
        GlossaryTerm(
            term = "Executive Alert",
            definition = "A notification triggered by executive changes, such as appointments, departures, or insider trading activity.",
            category = GlossaryCategory.ALERTS
        ),
        GlossaryTerm(
            term = "VETR Score Alert",
            definition = "A notification sent when a stock's VETR Score changes significantly, indicating a shift in company health.",
            category = GlossaryCategory.ALERTS
        ),
        GlossaryTerm(
            term = "Watchlist",
            definition = "A personalized list of stocks you're monitoring. You can receive alerts for any activity related to stocks in your watchlist.",
            category = GlossaryCategory.ALERTS
        )
    )
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun GlossaryScreenPreview() {
    VettrTheme {
        GlossaryScreen()
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun GlossaryScreenTabletPreview() {
    VettrTheme {
        GlossaryScreen()
    }
}
