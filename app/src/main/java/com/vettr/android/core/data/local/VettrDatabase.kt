package com.vettr.android.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vettr.android.core.model.PlaceholderEntity

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VettrDatabase : RoomDatabase() {
    // DAOs will be added here as they are created
}
