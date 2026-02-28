package com.vettr.android.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

@Immutable
@Entity(tableName = "placeholder")
data class PlaceholderEntity(
    @PrimaryKey val id: Int = 0
)
