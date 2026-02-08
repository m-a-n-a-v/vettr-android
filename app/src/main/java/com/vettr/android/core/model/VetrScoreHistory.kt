package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * VetrScoreHistory entity representing historical VETR Score calculations.
 * Tracks score evolution over time for trend analysis and charting.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property stockTicker Stock ticker this score applies to
 * @property overallScore Overall VETR Score (0-100)
 * @property pedigreeScore Executive team quality score component (0-100)
 * @property filingVelocityScore Filing frequency and consistency score component (0-100)
 * @property redFlagScore Red flag severity score component (0-100, lower is better)
 * @property growthScore Revenue and market growth score component (0-100)
 * @property governanceScore Corporate governance score component (0-100)
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

    @ColumnInfo(name = "pedigree_score")
    val pedigreeScore: Int,

    @ColumnInfo(name = "filing_velocity_score")
    val filingVelocityScore: Int,

    @ColumnInfo(name = "red_flag_score")
    val redFlagScore: Int,

    @ColumnInfo(name = "growth_score")
    val growthScore: Int,

    @ColumnInfo(name = "governance_score")
    val governanceScore: Int,

    @ColumnInfo(name = "calculated_at")
    val calculatedAt: Long
)
