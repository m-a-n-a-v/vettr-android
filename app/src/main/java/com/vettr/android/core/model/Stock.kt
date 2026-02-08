package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

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
