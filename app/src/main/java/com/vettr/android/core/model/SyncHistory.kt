package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sync_history")
data class SyncHistory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    val duration: Long? = null,

    @ColumnInfo(name = "items_synced")
    val itemsSynced: Int = 0,

    val errors: String? = null,

    val status: String
)
