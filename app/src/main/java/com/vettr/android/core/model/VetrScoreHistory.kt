package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

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
