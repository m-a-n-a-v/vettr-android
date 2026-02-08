package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * SyncHistory entity tracking data synchronization operations.
 * Used for monitoring, debugging, and displaying sync status to users.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property startedAt Sync start timestamp (Unix epoch milliseconds)
 * @property completedAt Sync completion timestamp (nullable if still in progress)
 * @property duration Sync duration in milliseconds (nullable if not completed)
 * @property itemsSynced Number of items successfully synced
 * @property errors Error messages if sync failed (nullable)
 * @property status Sync status (e.g., "in_progress", "success", "failed")
 */
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
