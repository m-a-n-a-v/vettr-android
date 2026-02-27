package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Portfolio entity representing a user's investment portfolio.
 * Supports CSV import, manual entry, and brokerage linking.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property userId Owner user ID
 * @property provider Portfolio source provider (csv, manual, flinks, snaptrade)
 * @property accountId Optional linked brokerage account ID
 * @property name User-defined portfolio name
 * @property status Portfolio status (active, syncing, error)
 * @property createdAt Creation timestamp (Unix epoch milliseconds)
 * @property updatedAt Last update timestamp (Unix epoch milliseconds)
 */
@Entity(tableName = "portfolios")
data class Portfolio(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String,

    val provider: String,

    @ColumnInfo(name = "account_id")
    val accountId: String? = null,

    val name: String,

    val status: String = "active",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
