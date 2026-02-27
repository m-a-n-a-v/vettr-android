package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Portfolio insight entity representing AI-generated insights for portfolio analysis.
 * Insight types: warrant_overhang, cash_runway, sedi_insider, hold_expiry,
 * flow_through_seasonality, executive_pedigree.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property portfolioId Parent portfolio ID (foreign key)
 * @property insightType Type of insight (e.g., "warrant_overhang", "cash_runway")
 * @property severity Insight severity level (e.g., "low", "medium", "high", "critical")
 * @property title Short insight title
 * @property summary Detailed insight summary text
 * @property isDismissed Whether the user has dismissed this insight
 * @property createdAt Creation timestamp (Unix epoch milliseconds)
 */
@Entity(
    tableName = "portfolio_insights",
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
data class PortfolioInsight(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "portfolio_id")
    val portfolioId: String,

    @ColumnInfo(name = "insight_type")
    val insightType: String,

    val severity: String,

    val title: String,

    val summary: String,

    @ColumnInfo(name = "is_dismissed")
    val isDismissed: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
