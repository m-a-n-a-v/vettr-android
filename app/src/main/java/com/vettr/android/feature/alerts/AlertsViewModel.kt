package com.vettr.android.feature.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.AlertRuleRepository
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.model.AlertRule
import com.vettr.android.core.util.HapticService
import com.vettr.android.core.util.ObservabilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Alerts screen.
 * Manages alert rules, filtering, and user interactions.
 * Uses reactive flows from Room database for live data updates.
 */
@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRuleRepository: AlertRuleRepository,
    private val authRepository: AuthRepository,
    val hapticService: HapticService,
    private val observabilityService: ObservabilityService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(AlertFilter.ALL)

    private var lastRefreshTime: Long = 0
    private val refreshDebounceMs = 10_000L // 10 seconds
    private var screenLoadStartTime: Long = 0
    private var dataCollectionJob: Job? = null

    init {
        screenLoadStartTime = System.currentTimeMillis()
        observeAlertRules()
    }

    /**
     * Observe alert rules reactively.
     * Combines user flow with filter state to produce filtered, grouped rules.
     * Room database changes automatically propagate to the UI.
     */
    private fun observeAlertRules() {
        dataCollectionJob?.cancel()
        dataCollectionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.getCurrentUser()
                .flatMapLatest { user ->
                    if (user != null) {
                        alertRuleRepository.getRulesForUser(user.id)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .combine(_selectedFilter) { rules, filter ->
                    Pair(rules, filter)
                }
                .collectLatest { (rules, filter) ->
                    val filteredRules = when (filter) {
                        AlertFilter.ALL -> rules
                        AlertFilter.ACTIVE -> rules.filter { it.isActive }
                        AlertFilter.TRIGGERED -> rules.filter { it.lastTriggeredAt != null }
                    }

                    val groupedRules = groupRulesByTicker(filteredRules)

                    _uiState.update {
                        it.copy(
                            alertRules = filteredRules,
                            groupedAlertRules = groupedRules,
                            allRulesCount = rules.size,
                            activeRulesCount = rules.count { r -> r.isActive },
                            triggeredRulesCount = rules.count { r -> r.lastTriggeredAt != null },
                            selectedFilter = filter,
                            isLoading = false,
                            lastUpdatedAt = System.currentTimeMillis()
                        )
                    }

                    // Track screen load time on first data load
                    if (screenLoadStartTime > 0) {
                        val loadTime = System.currentTimeMillis() - screenLoadStartTime
                        observabilityService.trackScreenLoadTime("Alerts", loadTime)
                        screenLoadStartTime = 0 // Only track once
                    }
                }
        }
    }

    /**
     * Toggle the active status of an alert rule.
     * Room Flow will automatically emit updated data to the UI.
     * @param ruleId ID of the rule to toggle
     */
    fun toggleRule(ruleId: String) {
        viewModelScope.launch {
            alertRuleRepository.toggleActive(ruleId)
        }
    }

    /**
     * Delete an alert rule.
     * Room Flow will automatically emit updated data to the UI.
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
     * Update the selected filter.
     * The reactive combine flow will automatically re-filter the rules.
     * @param filter AlertFilter to apply
     */
    fun setFilter(filter: AlertFilter) {
        _selectedFilter.value = filter
    }

    /**
     * Refresh alert rules by re-observing from repository.
     * Implements debounce logic to prevent more than 1 refresh per 10 seconds.
     */
    fun refresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < refreshDebounceMs) {
            // Skip refresh if within debounce window
            return
        }
        lastRefreshTime = currentTime
        observeAlertRules()
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
@androidx.compose.runtime.Immutable
data class AlertsUiState(
    val alertRules: List<AlertRule> = emptyList(),
    val groupedAlertRules: Map<String, List<AlertRule>> = emptyMap(),
    val allRulesCount: Int = 0,
    val activeRulesCount: Int = 0,
    val triggeredRulesCount: Int = 0,
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
