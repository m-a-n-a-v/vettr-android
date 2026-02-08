package com.vettr.android.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vettr.android.core.model.AlertRule
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.PlaceholderEntity
import com.vettr.android.core.model.RedFlagHistory
import com.vettr.android.core.model.Stock
import com.vettr.android.core.model.User
import com.vettr.android.core.util.Converters

@Database(
    entities = [
        PlaceholderEntity::class,
        Stock::class,
        Filing::class,
        Executive::class,
        User::class,
        AlertRule::class,
        RedFlagHistory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VettrDatabase : RoomDatabase() {
    // DAOs will be added here as they are created
}
