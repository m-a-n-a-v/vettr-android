package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * AlertRule entity for user-defined alert rules.
 * Stores rules that trigger notifications when specific conditions are met for stocks.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property userId User who owns this alert rule
 * @property stockTicker Stock ticker this rule applies to
 * @property ruleType Type of rule (e.g., "price", "filing", "insider", "red_flag")
 * @property triggerCondition Condition that triggers the alert (e.g., "price > 10.0")
 * @property isActive Whether the rule is currently active
 * @property createdAt Rule creation timestamp (Unix epoch milliseconds)
 * @property lastTriggeredAt Timestamp when alert last triggered (nullable)
 * @property frequency Alert frequency (e.g., "Real-time", "Daily", "Weekly")
 */
@Entity(
    tableName = "alert_rules",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["stock_ticker"])
    ]
)
data class AlertRule(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "stock_ticker")
    val stockTicker: String,

    @ColumnInfo(name = "rule_type")
    val ruleType: String,

    @ColumnInfo(name = "trigger_condition")
    val triggerCondition: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "last_triggered_at")
    val lastTriggeredAt: Long? = null,

    val frequency: String
)
