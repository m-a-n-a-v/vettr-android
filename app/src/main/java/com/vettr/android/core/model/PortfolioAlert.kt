package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Portfolio alert entity representing auto-generated alerts for portfolio events.
 * Alert types include: insider buy/sell, cash runway, hold expiry, warrant breach,
 * score change, filing published.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property portfolioId Parent portfolio ID (foreign key)
 * @property alertType Type of alert (e.g., "insider_buy", "cash_runway", "score_change")
 * @property severity Alert severity level (e.g., "low", "medium", "high", "critical")
 * @property title Short alert title
 * @property message Full alert message
 * @property isRead Whether the user has read this alert
 * @property triggeredAt Timestamp when the alert was triggered (Unix epoch milliseconds)
 */
@Entity(
    tableName = "portfolio_alerts",
    foreignKeys = [
        ForeignKey(
            entity = Portfolio::class,
            parentColumns = ["id"],
            childColumns = ["portfolio_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["portfolio_id"])]
)
data class PortfolioAlert(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "portfolio_id")
    val portfolioId: String,

    @ColumnInfo(name = "alert_type")
    val alertType: String,

    val severity: String,

    val title: String,

    val message: String,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "triggered_at")
    val triggeredAt: Long = System.currentTimeMillis()
)
