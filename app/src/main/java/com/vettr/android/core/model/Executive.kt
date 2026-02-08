package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "executives",
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
data class Executive(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "stock_id")
    val stockId: String,

    val name: String,

    val title: String,

    @ColumnInfo(name = "years_at_company")
    val yearsAtCompany: Double,

    @ColumnInfo(name = "previous_companies")
    val previousCompanies: String, // JSON-serialized list

    val education: String,

    val specialization: String,

    @ColumnInfo(name = "social_linkedin")
    val socialLinkedIn: String? = null,

    @ColumnInfo(name = "social_twitter")
    val socialTwitter: String? = null
)
