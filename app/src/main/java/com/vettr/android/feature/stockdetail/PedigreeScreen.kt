package com.vettr.android.feature.stockdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.model.Executive
import com.vettr.android.designsystem.component.EmptyStateView
import com.vettr.android.designsystem.component.LoadingView
import com.vettr.android.designsystem.component.SearchBarView
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme
import java.util.UUID

/**
 * PedigreeScreen displays the executive team for a stock.
 * Includes search, sort, and filter functionality.
 */
@Composable
fun PedigreeScreen(
    viewModel: PedigreeViewModel = hiltViewModel()
) {
    val executives by viewModel.filteredExecutives.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val filterTitle by viewModel.filterTitle.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    PedigreeScreenContent(
        executives = executives,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::search,
        sortOption = sortOption,
        onSortOptionChange = viewModel::sort,
        filterTitle = filterTitle,
        onFilterTitleChange = viewModel::filter,
        isLoading = isLoading,
        onRefresh = viewModel::refresh
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PedigreeScreenContent(
    executives: List<Executive>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    filterTitle: FilterTitle,
    onFilterTitleChange: (FilterTitle) -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Spacing.md)
    ) {
        // Search bar
        SearchBarView(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(bottom = Spacing.md)
        )

        // Sort menu
        SortMenuView(
            sortOption = sortOption,
            onSortOptionChange = onSortOptionChange,
            modifier = Modifier.padding(bottom = Spacing.md)
        )

        // Filter chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(bottom = Spacing.md)
        ) {
            FilterTitle.entries.forEach { title ->
                FilterChip(
                    selected = filterTitle == title,
                    onClick = { onFilterTitleChange(title) },
                    label = {
                        Text(
                            text = title.displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VettrAccent,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = VettrCardBackground,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Content
        when {
            isLoading -> {
                LoadingView()
            }
            executives.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = "No Executives Found",
                    subtitle = "No executive team members match your search criteria",
                    actionLabel = "Refresh",
                    onAction = onRefresh
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(executives, key = { it.id }) { executive ->
                        ExecutiveRowView(
                            executive = executive,
                            onClick = { /* TODO: Navigate to detail screen */ }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sort menu dropdown for executive list sorting options.
 */
@Composable
fun SortMenuView(
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(VettrCardBackground)
                .clickable { expanded = true }
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort by: ${sortOption.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Sort menu",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onSortOptionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Individual row view for an executive.
 * Shows red background tint if tenure < 1 year.
 */
@Composable
fun ExecutiveRowView(
    executive: Executive,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (executive.yearsAtCompany < 1.0) {
        VettrRed.copy(alpha = 0.1f)
    } else {
        VettrCardBackground
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = executive.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                // Title
                Text(
                    text = executive.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs)
                )

                // Specialization
                if (executive.specialization.isNotEmpty()) {
                    Text(
                        text = executive.specialization,
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrAccent,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            // Tenure and risk badge
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${String.format("%.1f", executive.yearsAtCompany)} yrs",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                // Risk badge for tenure < 1 year
                if (executive.yearsAtCompany < 1.0) {
                    Box(
                        modifier = Modifier
                            .padding(top = Spacing.xs)
                            .background(VettrRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = Spacing.sm, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RISK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extension property to get display name for SortOption.
 */
val SortOption.displayName: String
    get() = when (this) {
        SortOption.NAME -> "Name"
        SortOption.TENURE -> "Tenure"
        SortOption.SCORE -> "Score"
    }

/**
 * Extension property to get display name for FilterTitle.
 */
val FilterTitle.displayName: String
    get() = when (this) {
        FilterTitle.ALL -> "All"
        FilterTitle.CEO -> "CEO"
        FilterTitle.CFO -> "CFO"
        FilterTitle.COO -> "COO"
    }

// Preview helpers
@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ExecutiveRowViewPreview() {
    VettrTheme {
        ExecutiveRowView(
            executive = Executive(
                id = UUID.randomUUID().toString(),
                stockId = "1",
                name = "John Smith",
                title = "Chief Executive Officer",
                yearsAtCompany = 5.5,
                previousCompanies = "[]",
                education = "MBA from Harvard",
                specialization = "Technology & Innovation"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ExecutiveRowViewRiskPreview() {
    VettrTheme {
        ExecutiveRowView(
            executive = Executive(
                id = UUID.randomUUID().toString(),
                stockId = "1",
                name = "Jane Doe",
                title = "Chief Financial Officer",
                yearsAtCompany = 0.8,
                previousCompanies = "[]",
                education = "CPA",
                specialization = "Financial Planning"
            ),
            onClick = {}
        )
    }
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun PedigreeScreenPreview() {
    VettrTheme {
        PedigreeScreenContent(
            executives = listOf(
                Executive(
                    id = "1",
                    stockId = "1",
                    name = "John Smith",
                    title = "Chief Executive Officer",
                    yearsAtCompany = 5.5,
                    previousCompanies = "[]",
                    education = "MBA from Harvard",
                    specialization = "Technology & Innovation"
                ),
                Executive(
                    id = "2",
                    stockId = "1",
                    name = "Jane Doe",
                    title = "Chief Financial Officer",
                    yearsAtCompany = 0.8,
                    previousCompanies = "[]",
                    education = "CPA",
                    specialization = "Financial Planning"
                )
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            sortOption = SortOption.NAME,
            onSortOptionChange = {},
            filterTitle = FilterTitle.ALL,
            onFilterTitleChange = {},
            isLoading = false,
            onRefresh = {}
        )
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun PedigreeScreenTabletPreview() {
    VettrTheme {
        PedigreeScreenContent(
            executives = listOf(
                Executive(
                    id = "1",
                    stockId = "1",
                    name = "John Smith",
                    title = "Chief Executive Officer",
                    yearsAtCompany = 5.5,
                    previousCompanies = "[]",
                    education = "MBA from Harvard",
                    specialization = "Technology & Innovation"
                ),
                Executive(
                    id = "2",
                    stockId = "1",
                    name = "Jane Doe",
                    title = "Chief Financial Officer",
                    yearsAtCompany = 0.8,
                    previousCompanies = "[]",
                    education = "CPA",
                    specialization = "Financial Planning"
                )
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            sortOption = SortOption.NAME,
            onSortOptionChange = {},
            filterTitle = FilterTitle.ALL,
            onFilterTitleChange = {},
            isLoading = false,
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun PedigreeScreenEmptyPreview() {
    VettrTheme {
        PedigreeScreenContent(
            executives = emptyList(),
            searchQuery = "",
            onSearchQueryChange = {},
            sortOption = SortOption.NAME,
            onSortOptionChange = {},
            filterTitle = FilterTitle.ALL,
            onFilterTitleChange = {},
            isLoading = false,
            onRefresh = {}
        )
    }
}
