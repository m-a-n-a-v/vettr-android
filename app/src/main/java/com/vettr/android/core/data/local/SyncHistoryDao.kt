package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.SyncHistory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SyncHistory entities.
 * Tracks data synchronization operations for monitoring and debugging.
 */
@Dao
interface SyncHistoryDao {
    /**
     * Get all sync history records, ordered by most recent.
     * @return Flow emitting list of all sync history records
     */
    @Query("SELECT * FROM sync_history ORDER BY started_at DESC")
    fun getAll(): Flow<List<SyncHistory>>

    /**
     * Get recent sync history records.
     * @param limit Maximum number of records to return (default: 10)
     * @return Flow emitting list of recent sync history records
     */
    @Query("SELECT * FROM sync_history ORDER BY started_at DESC LIMIT :limit")
    fun getRecent(limit: Int = 10): Flow<List<SyncHistory>>

    /**
     * Get a specific sync history record by ID.
     * @param id Sync history record ID
     * @return Flow emitting sync history or null if not found
     */
    @Query("SELECT * FROM sync_history WHERE id = :id")
    fun getById(id: String): Flow<SyncHistory?>

    /**
     * Get all sync operations currently in progress.
     * @return Flow emitting list of in-progress sync operations
     */
    @Query("SELECT * FROM sync_history WHERE status = 'in_progress'")
    fun getInProgress(): Flow<List<SyncHistory>>

    /**
     * Insert a sync history record, replacing if already exists.
     * @param syncHistory Sync history record to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncHistory: SyncHistory)

    /**
     * Update completion details for a sync operation.
     * @param id Sync history record ID
     * @param completedAt Completion timestamp
     * @param duration Duration in milliseconds
     * @param status Final status (e.g., "success", "failed")
     */
    @Query("UPDATE sync_history SET completed_at = :completedAt, duration = :duration, status = :status WHERE id = :id")
    suspend fun updateCompletion(id: String, completedAt: Long, duration: Long, status: String)

    /**
     * Update error information for a failed sync operation.
     * @param id Sync history record ID
     * @param errors Error message or details
     */
    @Query("UPDATE sync_history SET errors = :errors, status = 'failed' WHERE id = :id")
    suspend fun updateErrors(id: String, errors: String)

    /**
     * Delete old sync history records before a specified timestamp.
     * Useful for cleanup to prevent unbounded database growth.
     * @param timestamp Cutoff timestamp (records older than this will be deleted)
     */
    @Query("DELETE FROM sync_history WHERE started_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
