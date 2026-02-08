package com.vettr.android.feature.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.AlertRuleRepository
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.AlertRule
import com.vettr.android.core.model.Stock
import com.vettr.android.core.util.HapticService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Alert Rule Creator screen.
 * Manages the multi-step wizard state and saves alert rules.
 */
@HiltViewModel
class AlertRuleCreatorViewModel @Inject constructor(
    private val alertRuleRepository: AlertRuleRepository,
    private val stockRepository: StockRepository,
    private val authRepository: AuthRepository,
    val hapticService: HapticService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertRuleCreatorUiState())
    val uiState: StateFlow<AlertRuleCreatorUiState> = _uiState.asStateFlow()

    init {
        loadStocks()
    }

    /**
     * Load all available stocks.
     */
    private fun loadStocks() {
        viewModelScope.launch {
            stockRepository.getStocks().collectLatest { stocks ->
                _uiState.update {
                    it.copy(
                        availableStocks = stocks,
                        filteredStocks = stocks
                    )
                }
            }
        }
    }

    /**
     * Update search query and filter stocks.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.availableStocks
            } else {
                state.availableStocks.filter { stock ->
                    stock.ticker.contains(query, ignoreCase = true) ||
                    stock.name.contains(query, ignoreCase = true)
                }
            }
            state.copy(
                searchQuery = query,
                filteredStocks = filtered
            )
        }
    }

    /**
     * Select a stock for the alert rule.
     */
    fun selectStock(stock: Stock) {
        _uiState.update { it.copy(selectedStock = stock) }
    }

    /**
     * Select a rule type.
     */
    fun selectRuleType(ruleType: String) {
        _uiState.update { it.copy(selectedRuleType = ruleType) }
    }

    /**
     * Update the condition value.
     */
    fun updateConditionValue(value: String) {
        _uiState.update { it.copy(conditionValue = value) }
    }

    /**
     * Select notification frequency.
     */
    fun selectFrequency(frequency: String) {
        _uiState.update { it.copy(selectedFrequency = frequency) }
    }

    /**
     * Navigate to the next step.
     */
    fun nextStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep < 5 && canProceedToNextStep()) {
            _uiState.update { it.copy(currentStep = currentStep + 1) }
        }
    }

    /**
     * Navigate to the previous step.
     */
    fun previousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep > 1) {
            _uiState.update { it.copy(currentStep = currentStep - 1) }
        }
    }

    /**
     * Navigate to a specific step (for edit buttons in review).
     */
    fun goToStep(step: Int) {
        if (step in 1..5) {
            _uiState.update { it.copy(currentStep = step) }
        }
    }

    /**
     * Check if user can proceed to the next step.
     */
    fun canProceedToNextStep(): Boolean {
        val state = _uiState.value
        return when (state.currentStep) {
            1 -> state.selectedStock != null
            2 -> state.selectedRuleType != null
            3 -> state.conditionValue.isNotBlank() && isValidConditionValue(state.conditionValue)
            4 -> state.selectedFrequency != null
            else -> false
        }
    }

    /**
     * Check if the rule can be saved.
     */
    fun canSaveRule(): Boolean {
        val state = _uiState.value
        return state.selectedStock != null &&
               state.selectedRuleType != null &&
               state.conditionValue.isNotBlank() &&
               isValidConditionValue(state.conditionValue) &&
               state.selectedFrequency != null
    }

    /**
     * Validate condition value.
     */
    private fun isValidConditionValue(value: String): Boolean {
        return value.toDoubleOrNull() != null && value.toDouble() > 0
    }

    /**
     * Save the alert rule.
     */
    fun saveRule(view: android.view.View?) {
        val state = _uiState.value
        if (!canSaveRule()) return

        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                if (user != null && state.selectedStock != null && state.selectedRuleType != null && state.selectedFrequency != null) {
                    val triggerCondition = buildTriggerCondition(
                        state.selectedRuleType,
                        state.conditionValue
                    )

                    val alertRule = AlertRule(
                        userId = user.id,
                        stockTicker = state.selectedStock.ticker,
                        ruleType = state.selectedRuleType,
                        triggerCondition = triggerCondition,
                        frequency = state.selectedFrequency,
                        isActive = true,
                        createdAt = System.currentTimeMillis()
                    )

                    val result = alertRuleRepository.createRule(alertRule)
                    result.onSuccess {
                        _uiState.update { it.copy(saveSuccess = true) }
                        // Medium haptic for successful alert creation
                        hapticService.medium(view)
                    }
                    result.onFailure { error ->
                        _uiState.update { it.copy(errorMessage = error.message) }
                        // Error haptic for failure
                        hapticService.error(view)
                    }
                }
            }
        }
    }

    /**
     * Build trigger condition string based on rule type and value.
     */
    private fun buildTriggerCondition(ruleType: String, value: String): String {
        return when (ruleType) {
            "PRICE_ABOVE" -> "Price above $$value"
            "PRICE_BELOW" -> "Price below $$value"
            "PRICE_INCREASE" -> "Price increase by $value%"
            "PRICE_DECREASE" -> "Price decrease by $value%"
            "VETR_SCORE_CHANGE" -> "VETR score change by $value points"
            else -> value
        }
    }
}

/**
 * UI state for the Alert Rule Creator screen.
 */
@androidx.compose.runtime.Immutable
data class AlertRuleCreatorUiState(
    val currentStep: Int = 1,
    val availableStocks: List<Stock> = emptyList(),
    val filteredStocks: List<Stock> = emptyList(),
    val searchQuery: String = "",
    val selectedStock: Stock? = null,
    val selectedRuleType: String? = null,
    val conditionValue: String = "",
    val selectedFrequency: String? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)
