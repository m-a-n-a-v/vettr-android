package com.vettr.android.feature.alerts

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.model.Stock
import com.vettr.android.designsystem.theme.VettrTheme
import kotlinx.coroutines.launch

/**
 * Alert Rule Creator Screen - Multi-step wizard for creating alert rules.
 * Steps: Stock Selection -> Rule Type -> Condition -> Frequency -> Review
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertRuleCreatorScreen(
    modifier: Modifier = Modifier,
    viewModel: AlertRuleCreatorViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSaveComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // Show success snackbar and navigate back when save completes
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Alert rule created successfully!")
                onSaveComplete()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Alert Rule",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { uiState.currentStep / 5f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )

            // Step content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState.currentStep) {
                    1 -> StockSelectionStep(
                        stocks = uiState.filteredStocks,
                        selectedStock = uiState.selectedStock,
                        onStockSelected = { viewModel.selectStock(it) },
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChanged = { viewModel.updateSearchQuery(it) }
                    )
                    2 -> RuleTypeStep(
                        selectedRuleType = uiState.selectedRuleType,
                        onRuleTypeSelected = { viewModel.selectRuleType(it) }
                    )
                    3 -> ConditionStep(
                        ruleType = uiState.selectedRuleType,
                        conditionValue = uiState.conditionValue,
                        onConditionValueChanged = { viewModel.updateConditionValue(it) }
                    )
                    4 -> FrequencyStep(
                        selectedFrequency = uiState.selectedFrequency,
                        onFrequencySelected = { viewModel.selectFrequency(it) }
                    )
                    5 -> ReviewStep(
                        stock = uiState.selectedStock,
                        ruleType = uiState.selectedRuleType,
                        conditionValue = uiState.conditionValue,
                        frequency = uiState.selectedFrequency,
                        onEditStep = { viewModel.goToStep(it) }
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (uiState.currentStep > 1) {
                    TextButton(onClick = { viewModel.previousStep() }) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (uiState.currentStep < 5) {
                    Button(
                        onClick = { viewModel.nextStep() },
                        enabled = viewModel.canProceedToNextStep()
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = { viewModel.saveRule(view) },
                        enabled = viewModel.canSaveRule()
                    ) {
                        Text("Save Rule")
                    }
                }
            }
        }
    }
}

/**
 * Step 1: Stock Selection
 */
@Composable
private fun StockSelectionStep(
    stocks: List<Stock>,
    selectedStock: Stock?,
    onStockSelected: (Stock) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select a Stock",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Choose the stock you want to create an alert for",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by ticker or company name") },
            singleLine = true
        )

        // Stock list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = stocks,
                key = { it.id }
            ) { stock ->
                StockSelectionCard(
                    stock = stock,
                    isSelected = selectedStock?.id == stock.id,
                    onClick = { onStockSelected(stock) }
                )
            }
        }
    }
}

/**
 * Stock selection card.
 */
@Composable
private fun StockSelectionCard(
    stock: Stock,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.ticker,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stock.exchange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Step 2: Rule Type Selection
 */
@Composable
private fun RuleTypeStep(
    selectedRuleType: String?,
    onRuleTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Choose Rule Type",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Select the type of alert you want to create",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Rule type cards
        RuleTypeCard(
            title = "Price Above",
            description = "Alert when stock price goes above a specific value",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            ruleType = "PRICE_ABOVE",
            isSelected = selectedRuleType == "PRICE_ABOVE",
            onClick = { onRuleTypeSelected("PRICE_ABOVE") }
        )

        RuleTypeCard(
            title = "Price Below",
            description = "Alert when stock price goes below a specific value",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            ruleType = "PRICE_BELOW",
            isSelected = selectedRuleType == "PRICE_BELOW",
            onClick = { onRuleTypeSelected("PRICE_BELOW") }
        )

        RuleTypeCard(
            title = "Price Increase",
            description = "Alert when stock price increases by a percentage",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            ruleType = "PRICE_INCREASE",
            isSelected = selectedRuleType == "PRICE_INCREASE",
            onClick = { onRuleTypeSelected("PRICE_INCREASE") }
        )

        RuleTypeCard(
            title = "Price Decrease",
            description = "Alert when stock price decreases by a percentage",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            ruleType = "PRICE_DECREASE",
            isSelected = selectedRuleType == "PRICE_DECREASE",
            onClick = { onRuleTypeSelected("PRICE_DECREASE") }
        )

        RuleTypeCard(
            title = "VETR Score Change",
            description = "Alert when VETR score changes significantly",
            icon = Icons.Default.Warning,
            ruleType = "VETR_SCORE_CHANGE",
            isSelected = selectedRuleType == "VETR_SCORE_CHANGE",
            onClick = { onRuleTypeSelected("VETR_SCORE_CHANGE") }
        )
    }
}

/**
 * Rule type selection card.
 */
@Composable
private fun RuleTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    ruleType: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Step 3: Condition (contextual based on rule type)
 */
@Composable
private fun ConditionStep(
    ruleType: String?,
    conditionValue: String,
    onConditionValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Set Condition",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        val (label, hint, suffix) = when (ruleType) {
            "PRICE_ABOVE", "PRICE_BELOW" -> Triple(
                "Enter price threshold",
                "e.g., 12.50",
                "$"
            )
            "PRICE_INCREASE", "PRICE_DECREASE" -> Triple(
                "Enter percentage change",
                "e.g., 5",
                "%"
            )
            "VETR_SCORE_CHANGE" -> Triple(
                "Enter minimum score change",
                "e.g., 10",
                "points"
            )
            else -> Triple("Enter value", "", "")
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = conditionValue,
            onValueChange = onConditionValueChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(hint) },
            suffix = { Text(suffix) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        // Helpful explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "How this works:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (ruleType) {
                        "PRICE_ABOVE" -> "You'll be notified when the stock price goes above your specified value."
                        "PRICE_BELOW" -> "You'll be notified when the stock price goes below your specified value."
                        "PRICE_INCREASE" -> "You'll be notified when the stock price increases by your specified percentage."
                        "PRICE_DECREASE" -> "You'll be notified when the stock price decreases by your specified percentage."
                        "VETR_SCORE_CHANGE" -> "You'll be notified when the VETR score changes by at least your specified number of points."
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Step 4: Frequency Selection
 */
@Composable
private fun FrequencyStep(
    selectedFrequency: String?,
    onFrequencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Notification Frequency",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "How often do you want to be notified?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FrequencyOption(
            title = "Real-time",
            description = "Get notified immediately when condition is met",
            frequency = "REAL_TIME",
            isSelected = selectedFrequency == "REAL_TIME",
            onClick = { onFrequencySelected("REAL_TIME") }
        )

        FrequencyOption(
            title = "Once per day",
            description = "Get one notification per day when condition is met",
            frequency = "DAILY",
            isSelected = selectedFrequency == "DAILY",
            onClick = { onFrequencySelected("DAILY") }
        )

        FrequencyOption(
            title = "Once per week",
            description = "Get one notification per week when condition is met",
            frequency = "WEEKLY",
            isSelected = selectedFrequency == "WEEKLY",
            onClick = { onFrequencySelected("WEEKLY") }
        )
    }
}

/**
 * Frequency option card.
 */
@Composable
private fun FrequencyOption(
    title: String,
    description: String,
    frequency: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Step 5: Review
 */
@Composable
private fun ReviewStep(
    stock: Stock?,
    ruleType: String?,
    conditionValue: String,
    frequency: String?,
    onEditStep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review & Confirm",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Review your alert rule before saving",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Stock
        ReviewItem(
            label = "Stock",
            value = stock?.let { "${it.ticker} - ${it.name}" } ?: "Not selected",
            onEdit = { onEditStep(1) }
        )

        // Rule Type
        ReviewItem(
            label = "Rule Type",
            value = when (ruleType) {
                "PRICE_ABOVE" -> "Price Above"
                "PRICE_BELOW" -> "Price Below"
                "PRICE_INCREASE" -> "Price Increase"
                "PRICE_DECREASE" -> "Price Decrease"
                "VETR_SCORE_CHANGE" -> "VETR Score Change"
                else -> "Not selected"
            },
            onEdit = { onEditStep(2) }
        )

        // Condition
        ReviewItem(
            label = "Condition",
            value = when (ruleType) {
                "PRICE_ABOVE", "PRICE_BELOW" -> "$$conditionValue"
                "PRICE_INCREASE", "PRICE_DECREASE" -> "$conditionValue%"
                "VETR_SCORE_CHANGE" -> "$conditionValue points"
                else -> conditionValue
            },
            onEdit = { onEditStep(3) }
        )

        // Frequency
        ReviewItem(
            label = "Frequency",
            value = when (frequency) {
                "REAL_TIME" -> "Real-time"
                "DAILY" -> "Once per day"
                "WEEKLY" -> "Once per week"
                else -> "Not selected"
            },
            onEdit = { onEditStep(4) }
        )
    }
}

/**
 * Review item with edit button.
 */
@Composable
private fun ReviewItem(
    label: String,
    value: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertRuleCreatorScreenPreview() {
    VettrTheme {
        AlertRuleCreatorScreen()
    }
}
