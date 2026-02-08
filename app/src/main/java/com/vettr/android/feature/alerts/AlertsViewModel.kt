package com.vettr.android.feature.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.AlertRuleRepository
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.model.AlertRule
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
 * ViewModel for the Alerts screen.
 * Manages alert rules, filtering, and user interactions.
 */
@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRuleRepository: AlertRuleRepository,
    private val authRepository: AuthRepository,
    val hapticService: HapticService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private var lastRefreshTime: Long = 0
    private val refreshDebounceMs = 10_000L // 10 seconds

    init {
        loadRules()
    }

    /**
     * Load alert rules for the current user.
     * Groups rules by stock ticker and applies the selected filter.
     */
    fun loadRules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.getCurrentUser().collectLatest { user ->
                if (user != null) {
                    alertRuleRepository.getRulesForUser(user.id).collectLatest { rules ->
                        val filteredRules = when (_uiState.value.selectedFilter) {
                            AlertFilter.ALL -> rules
                            AlertFilter.ACTIVE -> rules.filter { it.isActive }
                            AlertFilter.TRIGGERED -> rules.filter { it.lastTriggeredAt != null }
                        }

                        val groupedRules = groupRulesByTicker(filteredRules)

                        _uiState.update {
                            it.copy(
                                alertRules = filteredRules,
                                groupedAlertRules = groupedRules,
                                isLoading = false,
                                lastUpdatedAt = System.currentTimeMillis()
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            alertRules = emptyList(),
                            groupedAlertRules = emptyMap(),
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Toggle the active status of an alert rule.
     * @param ruleId ID of the rule to toggle
     */
    fun toggleRule(ruleId: String) {
        viewModelScope.launch {
            alertRuleRepository.toggleActive(ruleId)
        }
    }

    /**
     * Delete an alert rule.
     * @param ruleId ID of the rule to delete
     */
    fun deleteRule(ruleId: String, view: android.view.View?) {
        viewModelScope.launch {
            alertRuleRepository.deleteRule(ruleId)
            // Heavy haptic for delete action
            hapticService.heavy(view)
        }
    }

    /**
     * Duplicate an alert rule.
     * Creates a new rule with the same configuration but a new ID.
     * @param rule AlertRule to duplicate
     */
    fun duplicateRule(rule: AlertRule) {
        viewModelScope.launch {
            val duplicatedRule = rule.copy(
                id = "", // Will generate new UUID
                createdAt = System.currentTimeMillis(),
                lastTriggeredAt = null
            )
            alertRuleRepository.createRule(duplicatedRule)
        }
    }

    /**
     * Update the selected filter and reload rules.
     * @param filter AlertFilter to apply
     */
    fun setFilter(filter: AlertFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadRules()
    }

    /**
     * Refresh alert rules by reloading from repository.
     * Implements debounce logic to prevent more than 1 refresh per 10 seconds.
     */
    fun refresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < refreshDebounceMs) {
            // Skip refresh if within debounce window
            return
        }
        lastRefreshTime = currentTime
        loadRules()
    }

    /**
     * Group alert rules by stock ticker.
     * @param rules List of alert rules to group
     * @return Map of ticker to list of rules
     */
    private fun groupRulesByTicker(rules: List<AlertRule>): Map<String, List<AlertRule>> {
        return rules.groupBy { it.stockTicker }
            .toSortedMap() // Sort tickers alphabetically
    }
}

/**
 * UI state for the Alerts screen.
 */
data class AlertsUiState(
    val alertRules: List<AlertRule> = emptyList(),
    val groupedAlertRules: Map<String, List<AlertRule>> = emptyMap(),
    val selectedFilter: AlertFilter = AlertFilter.ALL,
    val isLoading: Boolean = false,
    val lastUpdatedAt: Long? = null
)

/**
 * Filter options for alert rules.
 */
enum class AlertFilter {
    ALL,
    ACTIVE,
    TRIGGERED
}
