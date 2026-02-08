package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Stock entity representing a publicly traded company.
 * Stores core information about TSX-V and CSE stocks for the VETTR platform.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property ticker Stock ticker symbol (e.g., "AAPL", "TSLA")
 * @property name Company name (e.g., "Apple Inc.")
 * @property exchange Exchange where stock is traded (e.g., "TSX-V", "CSE")
 * @property sector Industry sector (e.g., "Technology", "Mining")
 * @property marketCap Market capitalization in CAD
 * @property price Current stock price in CAD
 * @property priceChange Price change percentage (e.g., 2.5 for +2.5%)
 * @property vetrScore VETR quality score (0-100)
 * @property isFavorite Whether user has marked this stock as favorite
 */
@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val ticker: String,

    val name: String,

    val exchange: String,

    val sector: String,

    @ColumnInfo(name = "market_cap")
    val marketCap: Double,

    val price: Double,

    @ColumnInfo(name = "price_change")
    val priceChange: Double,

    @ColumnInfo(name = "vetr_score")
    val vetrScore: Int,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)
