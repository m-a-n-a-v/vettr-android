package com.vettr.android.core.data.repository

import com.vettr.android.core.model.AlertRule
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AlertRule CRUD operations.
 * Provides abstraction over alert rule data management with validation.
 */
interface AlertRuleRepository {
    /**
     * Create a new alert rule.
     * Validates that user hasn't exceeded max rules (50) and no duplicates exist.
     * @param rule AlertRule to create
     * @return Result containing created AlertRule or error
     */
    suspend fun createRule(rule: AlertRule): Result<AlertRule>

    /**
     * Update an existing alert rule.
     * @param rule AlertRule with updated values
     */
    suspend fun updateRule(rule: AlertRule)

    /**
     * Delete an alert rule by ID.
     * @param ruleId Rule ID to delete
     */
    suspend fun deleteRule(ruleId: String)

    /**
     * Get all alert rules for a specific user.
     * @param userId User ID
     * @return Flow emitting list of user's alert rules
     */
    fun getRulesForUser(userId: String): Flow<List<AlertRule>>

    /**
     * Get all alert rules for a specific stock ticker.
     * @param ticker Stock ticker symbol
     * @return Flow emitting list of alert rules for the stock
     */
    fun getRulesForStock(ticker: String): Flow<List<AlertRule>>

    /**
     * Toggle active status for an alert rule.
     * @param ruleId Rule ID to toggle
     */
    suspend fun toggleActive(ruleId: String)

    /**
     * Get count of alert rules for a user.
     * @param userId User ID
     * @return Flow emitting count of user's alert rules
     */
    fun getRuleCount(userId: String): Flow<Int>
}
