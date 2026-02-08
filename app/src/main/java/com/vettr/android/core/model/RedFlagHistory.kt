package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

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
