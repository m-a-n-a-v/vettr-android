package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Filing entity representing SEC regulatory filings.
 * Stores information about company filings such as 10-K, 10-Q, 8-K, etc.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property stockId Associated stock ID (foreign key to Stock entity)
 * @property type Filing type (e.g., "10-K", "10-Q", "8-K")
 * @property title Filing title
 * @property date Filing date (Unix epoch milliseconds)
 * @property summary Brief summary of the filing
 * @property isRead Whether user has marked this filing as read
 * @property isMaterial Whether this filing contains material information
 */
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
