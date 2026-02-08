package com.vettr.android.feature.pulse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.MetricCard
import com.vettr.android.designsystem.component.SearchBarView
import com.vettr.android.designsystem.component.SectionHeader
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Pulse screen - displays market overview and strategic events.
 */
@Composable
fun PulseScreen(
    modifier: Modifier = Modifier,
    viewModel: PulseViewModel = hiltViewModel()
) {
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val filings by viewModel.filings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Search bar with notification bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBarView(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { /* TODO: Navigate to notifications */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Market Overview Section
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                SectionHeader(title = "Market Overview")

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    item {
                        MetricCard(
                            title = "TSX",
                            value = "21,543.25",
                            change = 1.25,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    item {
                        MetricCard(
                            title = "S&P 500",
                            value = "4,783.45",
                            change = 0.85,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    item {
                        MetricCard(
                            title = "NASDAQ",
                            value = "15,095.14",
                            change = -0.45,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }

            // Placeholder for future sections
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun PulseScreenPreview() {
    VettrTheme {
        PulseScreen()
    }
}
