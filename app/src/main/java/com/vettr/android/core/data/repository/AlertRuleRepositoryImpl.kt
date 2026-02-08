package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.AlertRuleDao
import com.vettr.android.core.model.AlertRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of AlertRuleRepository using local database.
 * Enforces business rules: max 50 rules per user, no duplicates for same stock/type.
 */
class AlertRuleRepositoryImpl @Inject constructor(
    private val alertRuleDao: AlertRuleDao
) : AlertRuleRepository {

    companion object {
        private const val MAX_RULES_PER_USER = 50
    }

    override suspend fun createRule(rule: AlertRule): Result<AlertRule> {
        return try {
            // Validate: Check max rules limit
            val currentCount = alertRuleDao.getCountByUserId(rule.userId).first()
            if (currentCount >= MAX_RULES_PER_USER) {
                return Result.failure(
                    IllegalStateException("Maximum $MAX_RULES_PER_USER alert rules exceeded")
                )
            }

            // Validate: Check for duplicates (same stock ticker + rule type)
            val existingRules = alertRuleDao.getByUserId(rule.userId).first()
            val hasDuplicate = existingRules.any {
                it.stockTicker == rule.stockTicker && it.ruleType == rule.ruleType
            }
            if (hasDuplicate) {
                return Result.failure(
                    IllegalStateException("Alert rule already exists for ${rule.stockTicker} with type ${rule.ruleType}")
                )
            }

            // Insert the rule
            alertRuleDao.insert(rule)
            Result.success(rule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRule(rule: AlertRule) {
        alertRuleDao.update(rule)
    }

    override suspend fun deleteRule(ruleId: String) {
        // Get the rule first, then delete it
        val rule = alertRuleDao.getById(ruleId).first()
        rule?.let {
            alertRuleDao.delete(it)
        }
    }

    override fun getRulesForUser(userId: String): Flow<List<AlertRule>> {
        return alertRuleDao.getByUserId(userId)
    }

    override fun getRulesForStock(ticker: String): Flow<List<AlertRule>> {
        return alertRuleDao.getByStockTicker(ticker)
    }

    override suspend fun toggleActive(ruleId: String) {
        val rule = alertRuleDao.getById(ruleId).first()
        rule?.let {
            val updatedRule = it.copy(isActive = !it.isActive)
            alertRuleDao.update(updatedRule)
        }
    }

    override fun getRuleCount(userId: String): Flow<Int> {
        return alertRuleDao.getCountByUserId(userId)
    }
}
