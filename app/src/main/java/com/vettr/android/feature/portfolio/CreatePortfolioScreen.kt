package com.vettr.android.feature.portfolio

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrSurfaceVariant
import com.vettr.android.designsystem.theme.VettrTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePortfolioScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPortfolioCreated: (String) -> Unit = {},
    viewModel: CreatePortfolioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { stream ->
                viewModel.parseCsv(stream)
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.createdPortfolioId != null) {
            onPortfolioCreated(uiState.createdPortfolioId!!)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Create Portfolio") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VettrNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Portfolio Name") },
                    placeholder = { Text("e.g., My Mining Portfolio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VettrAccent,
                        unfocusedBorderColor = VettrTextSecondary.copy(alpha = 0.3f),
                        focusedLabelColor = VettrAccent,
                        cursorColor = VettrAccent
                    )
                )
            }

            item {
                Text(
                    text = "Import Method",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ProviderCard(
                    icon = Icons.Default.CloudUpload,
                    title = "CSV Upload",
                    description = "Import holdings from a CSV file",
                    isSelected = uiState.provider == "csv",
                    onClick = {
                        viewModel.selectProvider("csv")
                        csvLauncher.launch("text/*")
                    }
                )
            }

            item {
                ProviderCard(
                    icon = Icons.Default.Edit,
                    title = "Manual Entry",
                    description = "Add holdings one by one",
                    isSelected = uiState.provider == "manual",
                    onClick = { viewModel.selectProvider("manual") }
                )
            }

            item {
                ProviderCard(
                    icon = Icons.Default.Link,
                    title = "Brokerage Link",
                    description = "Connect your brokerage account",
                    isSelected = false,
                    isDisabled = true,
                    onClick = {}
                )
            }

            // CSV preview
            if (uiState.csvHoldings.isNotEmpty()) {
                item {
                    Text(
                        text = "Parsed Holdings (${uiState.csvHoldings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(uiState.csvHoldings) { holding ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VettrSurfaceVariant)
                            .padding(Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = holding.ticker,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${holding.quantity} @ $${String.format("%.2f", holding.avgCost)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = VettrTextSecondary
                        )
                    }
                }
            }

            // CSV errors
            if (uiState.csvErrors.isNotEmpty()) {
                item {
                    Column {
                        uiState.csvErrors.forEach { err ->
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = VettrRed
                            )
                        }
                    }
                }
            }

            // Error
            if (uiState.error != null) {
                item {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrRed
                    )
                }
            }

            // Create button
            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                Button(
                    onClick = { viewModel.createPortfolio() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.name.isNotBlank() && uiState.provider.isNotEmpty() && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = VettrAccent)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Portfolio", color = Color.White)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(Spacing.xl)) }
        }
    }
}

@Composable
private fun ProviderCard(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    isDisabled: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isDisabled -> VettrTextSecondary.copy(alpha = 0.1f)
        isSelected -> VettrAccent
        else -> VettrTextSecondary.copy(alpha = 0.3f)
    }
    val alpha = if (isDisabled) 0.4f else 1f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(if (isSelected) VettrAccent.copy(alpha = 0.05f) else Color.Transparent)
            .clickable(enabled = !isDisabled, onClick = onClick)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) VettrAccent else VettrTextSecondary.copy(alpha = alpha),
            modifier = Modifier.size(32.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isDisabled) "Coming Soon" else description,
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary.copy(alpha = alpha)
            )
        }
    }
}
