package com.vettr.android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.data.remote.AutocompleteResultDto
import com.vettr.android.designsystem.component.PrimaryButton
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Guest Landing screen shown as the first screen for unauthenticated users.
 *
 * Displays VETTR branding, a live stock search bar with debounced autocomplete,
 * feature highlight cards, and CTA buttons to sign up or log in.
 * Tapping a search result navigates to the guest stock preview screen.
 *
 * @param onGetStartedClick Callback when user taps "Get Started" to create an account
 * @param onLogInClick Callback when user taps "Sign In" to go to login screen
 * @param onStockSelected Callback with ticker symbol when user selects a search result
 * @param viewModel GuestLandingViewModel providing autocomplete state
 */
@Composable
fun GuestLandingScreen(
    onGetStartedClick: () -> Unit = {},
    onLogInClick: () -> Unit = {},
    onStockSelected: (String) -> Unit = {},
    viewModel: GuestLandingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacing
        item { Spacer(modifier = Modifier.height(Spacing.xl)) }

        // Logo and branding
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                }

                Text(
                    text = "VETTR",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "AI-Powered Stock Due Diligence",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.lg)) }

        // Search bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search stocks by ticker or name...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VettrCardBackground,
                    unfocusedContainerColor = VettrCardBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )
        }

        // Autocomplete results
        if (uiState.autocompleteResults.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(Spacing.xs)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column {
                        uiState.autocompleteResults.forEachIndexed { index, result ->
                            AutocompleteResultRow(
                                result = result,
                                onClick = { onStockSelected(result.ticker) }
                            )
                            if (index < uiState.autocompleteResults.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Feature highlights (only show when not searching)
        if (uiState.autocompleteResults.isEmpty() && uiState.searchQuery.isEmpty()) {
            item { Spacer(modifier = Modifier.height(Spacing.lg)) }

            item {
                Text(
                    text = "Why VETTR?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(Spacing.sm)) }

            item {
                FeatureHighlightCard(
                    icon = Icons.Default.BarChart,
                    title = "VETR Score",
                    description = "Proprietary 0-100 scoring of every stock across 4 key pillars"
                )
            }

            item { Spacer(modifier = Modifier.height(Spacing.sm)) }

            item {
                FeatureHighlightCard(
                    icon = Icons.Default.Shield,
                    title = "Red Flag Detection",
                    description = "Automated risk identification for mining and resource stocks"
                )
            }

            item { Spacer(modifier = Modifier.height(Spacing.sm)) }

            item {
                FeatureHighlightCard(
                    icon = Icons.Default.TrendingUp,
                    title = "Portfolio Insights",
                    description = "AI-generated insights on warrant overhang, cash runway, and more"
                )
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.lg)) }

        // CTA buttons
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                PrimaryButton(
                    text = "Get Started",
                    onClick = onGetStartedClick,
                    fullWidth = true,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )

                TextButton(
                    onClick = onLogInClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account? Sign In",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.lg)) }
    }
}

/**
 * A single autocomplete search result row.
 */
@Composable
private fun AutocompleteResultRow(
    result: AutocompleteResultDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ticker badge
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result.ticker,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Company name and exchange
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.companyName ?: result.ticker,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (result.exchange != null) {
                Text(
                    text = result.exchange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Feature highlight card for the landing page.
 */
@Composable
private fun FeatureHighlightCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun GuestLandingScreenPreview() {
    VettrTheme {
        GuestLandingScreen()
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun GuestLandingScreenTabletPreview() {
    VettrTheme {
        GuestLandingScreen()
    }
}
