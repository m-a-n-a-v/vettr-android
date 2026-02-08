package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.SyncHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncHistoryDao {
    @Query("SELECT * FROM sync_history ORDER BY started_at DESC")
    fun getAll(): Flow<List<SyncHistory>>

    @Query("SELECT * FROM sync_history ORDER BY started_at DESC LIMIT :limit")
    fun getRecent(limit: Int = 10): Flow<List<SyncHistory>>

    @Query("SELECT * FROM sync_history WHERE id = :id")
    fun getById(id: String): Flow<SyncHistory?>

    @Query("SELECT * FROM sync_history WHERE status = 'in_progress'")
    fun getInProgress(): Flow<List<SyncHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncHistory: SyncHistory)

    @Query("UPDATE sync_history SET completed_at = :completedAt, duration = :duration, status = :status WHERE id = :id")
    suspend fun updateCompletion(id: String, completedAt: Long, duration: Long, status: String)

    @Query("UPDATE sync_history SET errors = :errors, status = 'failed' WHERE id = :id")
    suspend fun updateErrors(id: String, errors: String)

    @Query("DELETE FROM sync_history WHERE started_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
