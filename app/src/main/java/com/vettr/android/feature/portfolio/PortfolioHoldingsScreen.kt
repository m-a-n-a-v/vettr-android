package com.vettr.android.feature.portfolio

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.model.PortfolioHolding
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrSurfaceVariant
import com.vettr.android.designsystem.theme.VettrTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioHoldingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onStockClick: (String) -> Unit = {},
    viewModel: PortfolioHoldingsViewModel = hiltViewModel()
) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showAddSheet by remember { mutableStateOf(false) }
    var holdingToDelete by remember { mutableStateOf<PortfolioHolding?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Holdings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VettrNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = VettrAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Holding")
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading && holdings.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                item { Spacer(modifier = Modifier.height(Spacing.sm)) }

                items(holdings, key = { it.id }) { holding ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                holdingToDelete = holding
                                false
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) VettrRed else Color.Transparent,
                                label = "swipeColor"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color)
                                    .padding(horizontal = Spacing.lg),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        HoldingRow(
                            holding = holding,
                            onClick = { onStockClick(holding.ticker) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Delete confirmation
    holdingToDelete?.let { holding ->
        AlertDialog(
            onDismissRequest = { holdingToDelete = null },
            title = { Text("Delete Holding") },
            text = { Text("Remove ${holding.ticker} from this portfolio?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHolding(holding.id)
                    holdingToDelete = null
                }) { Text("Delete", color = VettrRed) }
            },
            dismissButton = {
                TextButton(onClick = { holdingToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Add holding bottom sheet
    if (showAddSheet) {
        AddHoldingSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { ticker, qty, cost ->
                viewModel.addHolding(ticker, qty, cost)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun HoldingRow(
    holding: PortfolioHolding,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gainColor = if (holding.gainLoss >= 0) VettrGreen else VettrRed
    val gainPrefix = if (holding.gainLoss >= 0) "+" else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VettrSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = holding.ticker,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${holding.quantity} shares @ $${String.format("%.2f", holding.avgCost)}",
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format("%.2f", holding.currentPrice)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$gainPrefix$${String.format("%.2f", holding.gainLoss)} (${gainPrefix}${String.format("%.1f", holding.gainLossPercent)}%)",
                style = MaterialTheme.typography.bodySmall,
                color = gainColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHoldingSheet(
    onDismiss: () -> Unit,
    onAdd: (ticker: String, quantity: Double, avgCost: Double) -> Unit
) {
    var ticker by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var avgCost by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = VettrNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "Add Holding",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = ticker,
                onValueChange = { ticker = it.uppercase() },
                label = { Text("Ticker") },
                placeholder = { Text("e.g., AAPL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VettrAccent,
                    unfocusedBorderColor = VettrTextSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = VettrAccent,
                    cursorColor = VettrAccent
                )
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                placeholder = { Text("e.g., 100") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VettrAccent,
                    unfocusedBorderColor = VettrTextSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = VettrAccent,
                    cursorColor = VettrAccent
                )
            )

            OutlinedTextField(
                value = avgCost,
                onValueChange = { avgCost = it },
                label = { Text("Average Cost") },
                placeholder = { Text("e.g., 25.50") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VettrAccent,
                    unfocusedBorderColor = VettrTextSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = VettrAccent,
                    cursorColor = VettrAccent
                )
            )

            Button(
                onClick = {
                    val qty = quantity.toDoubleOrNull()
                    val cost = avgCost.toDoubleOrNull()
                    if (ticker.isNotBlank() && qty != null && cost != null) {
                        onAdd(ticker, qty, cost)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = ticker.isNotBlank() && quantity.toDoubleOrNull() != null && avgCost.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = VettrAccent)
            ) {
                Text("Add Holding", color = Color.White)
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}
