package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vettr.android.core.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User entities.
 * Manages authenticated user data with reactive queries.
 */
@Dao
interface UserDao {
    /**
     * Get all users from the database.
     * Note: In most cases, there will be only one user (the current user).
     * @return Flow emitting list of all users
     */
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<User>>

    /**
     * Get user by unique identifier.
     * @param id User ID
     * @return Flow emitting user or null if not found
     */
    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: String): Flow<User?>

    /**
     * Get user by email address.
     * @param email User email
     * @return Flow emitting user or null if not found
     */
    @Query("SELECT * FROM users WHERE email = :email")
    fun getByEmail(email: String): Flow<User?>

    /**
     * Insert a user, replacing if already exists.
     * @param user User to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    /**
     * Update an existing user.
     * @param user User to update
     */
    @Update
    suspend fun update(user: User)

    /**
     * Delete a user from the database.
     * @param user User to delete
     */
    @Delete
    suspend fun delete(user: User)

    /**
     * Delete all users from the database.
     * Used when signing out to clear cached user data.
     */
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
