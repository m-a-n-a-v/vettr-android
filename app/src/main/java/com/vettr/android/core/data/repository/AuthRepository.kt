package com.vettr.android.core.data.repository

import com.vettr.android.core.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {
    /**
     * Sign in with email and password.
     * @return Result containing User on success, exception on failure.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User>

    /**
     * Sign in with Google using ID token from Google Sign-In.
     * @return Result containing User on success, exception on failure.
     */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Sign up with email and password.
     * @return Result containing User on success, exception on failure.
     */
    suspend fun signUp(email: String, password: String): Result<User>

    /**
     * Sign out the current user and clear auth tokens.
     */
    suspend fun signOut()

    /**
     * Get the current authenticated user.
     * @return Flow emitting current User or null if not authenticated.
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Check if user is authenticated.
     * @return Flow emitting true if user is authenticated, false otherwise.
     */
    fun isAuthenticated(): Flow<Boolean>

    /**
     * Request a password reset link for the given email address.
     * @param email The user's email address
     * @return Result with Unit on success, exception on failure.
     */
    suspend fun forgotPassword(email: String): Result<Unit>

    /**
     * Change the current user's password.
     * @param currentPassword The user's current password
     * @param newPassword The new password to set
     * @return Result with Unit on success, exception on failure.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}
