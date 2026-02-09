package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.TokenManager
import com.vettr.android.core.model.User
import com.vettr.android.core.model.VettrTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of AuthRepository for Phase 1.
 * Uses in-memory state and creates mock User objects for authentication.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager
) : AuthRepository {

    // In-memory state for current user
    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        // Auto-authenticate with a default user for live API mode
        // Bypasses auth screen so the app goes directly to main content
        _currentUser.value = createMockUser("demo@vettr.com")
        tokenManager.saveToken("live_api_token")
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            // Mock implementation: accept any email/password
            val mockUser = createMockUser(email)
            val mockToken = "mock_access_token_${System.currentTimeMillis()}"

            // Save token
            tokenManager.saveToken(mockToken)
            tokenManager.saveRefreshToken("mock_refresh_token_${System.currentTimeMillis()}")

            // Update current user
            _currentUser.value = mockUser

            Result.success(mockUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            // Mock implementation: create user from Google ID token
            val mockUser = createMockUser("google-user@gmail.com")
            val mockToken = "mock_google_access_token_${System.currentTimeMillis()}"

            // Save token
            tokenManager.saveToken(mockToken)
            tokenManager.saveRefreshToken("mock_google_refresh_token_${System.currentTimeMillis()}")

            // Update current user
            _currentUser.value = mockUser

            Result.success(mockUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            // Mock implementation: accept any email/password for signup
            val mockUser = createMockUser(email)
            val mockToken = "mock_signup_token_${System.currentTimeMillis()}"

            // Save token
            tokenManager.saveToken(mockToken)
            tokenManager.saveRefreshToken("mock_signup_refresh_token_${System.currentTimeMillis()}")

            // Update current user
            _currentUser.value = mockUser

            Result.success(mockUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        // Clear tokens
        tokenManager.clearAll()

        // Clear current user
        _currentUser.value = null
    }

    override fun getCurrentUser(): Flow<User?> {
        return _currentUser
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return _currentUser.map { it != null }
    }

    /**
     * Create a mock User object for testing.
     */
    private fun createMockUser(email: String): User {
        return User(
            id = "mock_user_${System.currentTimeMillis()}",
            email = email,
            displayName = email.substringBefore("@"),
            avatarUrl = null,
            tier = VettrTier.FREE.name,
            createdAt = System.currentTimeMillis()
        )
    }
}
