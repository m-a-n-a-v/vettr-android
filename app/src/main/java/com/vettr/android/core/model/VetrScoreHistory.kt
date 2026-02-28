package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * VetrScoreHistory entity representing historical VETR Score V2 calculations.
 * Tracks score evolution over time for trend analysis and charting.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property stockTicker Stock ticker this score applies to
 * @property overallScore Overall VETR Score (0-100)
 * @property financialSurvivalScore Cash runway, FCF, debt health pillar score (0-100)
 * @property operationalEfficiencyScore Sector-specific operational ratios pillar score (0-100)
 * @property shareholderStructureScore Pedigree, dilution, insider conviction, warrant overhang pillar score (0-100)
 * @property marketSentimentScore Liquidity, momentum, news, short interest, analyst targets pillar score (0-100)
 * @property calculatedAt Timestamp when score was calculated (Unix epoch milliseconds)
 */
@Entity(
    tableName = "vetr_score_history",
    indices = [Index(value = ["stock_ticker"])]
)
data class VetrScoreHistory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "stock_ticker")
    val stockTicker: String,

    @ColumnInfo(name = "overall_score")
    val overallScore: Int,

    @ColumnInfo(name = "financial_survival_score")
    val financialSurvivalScore: Int,

    @ColumnInfo(name = "operational_efficiency_score")
    val operationalEfficiencyScore: Int,

    @ColumnInfo(name = "shareholder_structure_score")
    val shareholderStructureScore: Int,

    @ColumnInfo(name = "market_sentiment_score")
    val marketSentimentScore: Int,

    @ColumnInfo(name = "calculated_at")
    val calculatedAt: Long
)
