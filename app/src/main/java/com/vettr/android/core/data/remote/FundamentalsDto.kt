package com.vettr.android.core.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Comprehensive fundamentals response for a stock.
 */
data class FundamentalsResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("valuation") val valuation: ValuationDto?,
    @SerializedName("earnings") val earnings: EarningsDto?,
    @SerializedName("analystConsensus") val analystConsensus: AnalystConsensusDto?,
    @SerializedName("shortInterest") val shortInterest: ShortInterestDto?,
    @SerializedName("institutionalHolders") val institutionalHolders: List<InstitutionalHolderDto> = emptyList(),
    @SerializedName("insiderData") val insiderData: InsiderDataDto?,
    @SerializedName("dividends") val dividends: DividendDto?
)

data class ValuationDto(
    @SerializedName("pe") val pe: Double?,
    @SerializedName("pb") val pb: Double?,
    @SerializedName("ps") val ps: Double?,
    @SerializedName("priceFcf") val priceFcf: Double?,
    @SerializedName("evEbitda") val evEbitda: Double?,
    @SerializedName("marketCap") val marketCap: Double?,
    @SerializedName("enterpriseValue") val enterpriseValue: Double?
)

data class EarningsDto(
    @SerializedName("history") val history: List<EarningsHistoryItemDto> = emptyList(),
    @SerializedName("estimates") val estimates: List<EarningsEstimateDto> = emptyList()
)

data class EarningsHistoryItemDto(
    @SerializedName("quarter") val quarter: String,
    @SerializedName("eps") val eps: Double?,
    @SerializedName("revenue") val revenue: Double?,
    @SerializedName("surprise") val surprise: Double?
)

data class EarningsEstimateDto(
    @SerializedName("quarter") val quarter: String,
    @SerializedName("epsEstimate") val epsEstimate: Double?,
    @SerializedName("revenueEstimate") val revenueEstimate: Double?
)

data class AnalystConsensusDto(
    @SerializedName("rating") val rating: String,
    @SerializedName("targetPrice") val targetPrice: Double?,
    @SerializedName("numberOfAnalysts") val numberOfAnalysts: Int,
    @SerializedName("strongBuy") val strongBuy: Int = 0,
    @SerializedName("buy") val buy: Int = 0,
    @SerializedName("hold") val hold: Int = 0,
    @SerializedName("sell") val sell: Int = 0,
    @SerializedName("strongSell") val strongSell: Int = 0
)

data class ShortInterestDto(
    @SerializedName("shortPercent") val shortPercent: Double?,
    @SerializedName("daysToCover") val daysToCover: Double?,
    @SerializedName("shortRatio") val shortRatio: Double?
)

data class InstitutionalHolderDto(
    @SerializedName("name") val name: String,
    @SerializedName("shares") val shares: Long,
    @SerializedName("percentHeld") val percentHeld: Double
)

data class InsiderDataDto(
    @SerializedName("recentTransactions") val recentTransactions: List<InsiderTransactionDto> = emptyList(),
    @SerializedName("netBuySell") val netBuySell: Double = 0.0
)

data class InsiderTransactionDto(
    @SerializedName("name") val name: String,
    @SerializedName("title") val title: String?,
    @SerializedName("type") val type: String,
    @SerializedName("shares") val shares: Long,
    @SerializedName("price") val price: Double?,
    @SerializedName("date") val date: String
)

data class DividendDto(
    @SerializedName("yield") val yieldPercent: Double?,
    @SerializedName("annualDividend") val annualDividend: Double?,
    @SerializedName("payoutRatio") val payoutRatio: Double?,
    @SerializedName("exDate") val exDate: String?
)

/**
 * VETTR Score peer comparison response.
 */
data class ScoreComparisonResponse(
    @SerializedName("peerScores") val peerScores: List<PeerScoreDto> = emptyList(),
    @SerializedName("sectorAverage") val sectorAverage: Double,
    @SerializedName("percentileRank") val percentileRank: Double
)

data class PeerScoreDto(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("vetrScore") val vetrScore: Int,
    @SerializedName("stockId") val stockId: String? = null
)

/**
 * Red flag trend response.
 */
data class RedFlagTrendResponse(
    @SerializedName("trendPoints") val trendPoints: List<RedFlagTrendPointDto> = emptyList()
)

data class RedFlagTrendPointDto(
    @SerializedName("date") val date: String,
    @SerializedName("count") val count: Int,
    @SerializedName("severityBreakdown") val severityBreakdown: SeverityBreakdownDto?
)

data class SeverityBreakdownDto(
    @SerializedName("critical") val critical: Int = 0,
    @SerializedName("warning") val warning: Int = 0,
    @SerializedName("info") val info: Int = 0
)
