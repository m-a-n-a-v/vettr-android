package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * AlertRule entity for user-defined alert rules.
 * Stores rules that trigger notifications when specific conditions are met for stocks.
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
