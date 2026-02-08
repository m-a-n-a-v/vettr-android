package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "filings",
    foreignKeys = [
        ForeignKey(
            entity = Stock::class,
            parentColumns = ["id"],
            childColumns = ["stock_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["stock_id"])]
)
data class Filing(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "stock_id")
    val stockId: String,

    val type: String,
    val title: String,
    val date: Long,
    val summary: String,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "is_material")
    val isMaterial: Boolean = false
)
