package com.vettr.android.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placeholder")
data class PlaceholderEntity(
    @PrimaryKey val id: Int = 0
)
