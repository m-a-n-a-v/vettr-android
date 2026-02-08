package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * RedFlagHistory entity representing detected red flags over time.
 * Tracks historical red flag detections for trend analysis and user awareness.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property stockTicker Stock ticker this red flag applies to
 * @property flagType Type of red flag (e.g., "insider_selling", "accounting_irregularity")
 * @property severity Severity level (e.g., "Low", "Medium", "High", "Critical")
 * @property score Numeric severity score (0-100)
 * @property description Human-readable description of the red flag
 * @property detectedAt Timestamp when flag was detected (Unix epoch milliseconds)
 * @property isAcknowledged Whether user has acknowledged this flag
 */
@Entity(
    tableName = "red_flag_history",
    indices = [Index(value = ["stock_ticker"])]
)
data class RedFlagHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "stock_ticker") val stockTicker: String,
    @ColumnInfo(name = "flag_type") val flagType: String,
    val severity: String,
    val score: Double,
    val description: String,
    @ColumnInfo(name = "detected_at") val detectedAt: Long,
    @ColumnInfo(name = "is_acknowledged") val isAcknowledged: Boolean = false
)
