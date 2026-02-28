package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
import androidx.compose.runtime.Immutable

/**
 * Portfolio holding entity representing a stock position within a portfolio.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property portfolioId Parent portfolio ID (foreign key)
 * @property ticker Stock ticker symbol
 * @property quantity Number of shares held
 * @property avgCost Average cost per share
 * @property currentPrice Current market price per share
 * @property currentValue Current total value (quantity * currentPrice)
 * @property gainLoss Unrealized gain/loss in dollars
 * @property gainLossPercent Unrealized gain/loss as percentage
 */
@Immutable
@Entity(
    tableName = "portfolio_holdings",
    foreignKeys = [
        ForeignKey(
            entity = Portfolio::class,
            parentColumns = ["id"],
            childColumns = ["portfolio_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["portfolio_id"])]
)
data class PortfolioHolding(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "portfolio_id")
    val portfolioId: String,

    val ticker: String,

    val quantity: Double,

    @ColumnInfo(name = "avg_cost")
    val avgCost: Double,

    @ColumnInfo(name = "current_price")
    val currentPrice: Double = 0.0,

    @ColumnInfo(name = "current_value")
    val currentValue: Double = 0.0,

    @ColumnInfo(name = "gain_loss")
    val gainLoss: Double = 0.0,

    @ColumnInfo(name = "gain_loss_percent")
    val gainLossPercent: Double = 0.0,

    @ColumnInfo(name = "vetr_score")
    val vetrScore: Int? = null,

    @ColumnInfo(name = "price_change_percent")
    val priceChangePercent: Double? = null,

    @ColumnInfo(name = "name")
    val name: String? = null
)
