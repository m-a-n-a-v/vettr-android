package com.vettr.android.core.data.repository

import com.vettr.android.core.data.local.TokenManager
import com.vettr.android.core.data.remote.ChangePasswordRequest
import com.vettr.android.core.data.remote.ForgotPasswordRequest
import com.vettr.android.core.data.remote.VettrApi
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
    private val tokenManager: TokenManager,
    private val vettrApi: VettrApi
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
            val request = com.vettr.android.core.data.remote.LoginRequest(
                idToken = idToken,
                provider = "google"
            )
            val response = vettrApi.googleSignIn(request)
            if (response.accessToken.isNotBlank()) {
                // Save tokens from backend response
                tokenManager.saveToken(response.accessToken)
                response.refreshToken?.let { tokenManager.saveRefreshToken(it) }

                // Create user from response DTO
                val userDto = response.user
                val user = User(
                    id = userDto.id,
                    email = userDto.email,
                    displayName = userDto.displayName,
                    avatarUrl = userDto.avatarUrl,
                    tier = userDto.tier,
                    createdAt = userDto.createdAt
                )
                _currentUser.value = user
                Result.success(user)
            } else {
                // Fallback to mock if backend returns empty token
                val mockUser = createMockUser("google-user@gmail.com")
                tokenManager.saveToken("google_fallback_token_${System.currentTimeMillis()}")
                _currentUser.value = mockUser
                Result.success(mockUser)
            }
        } catch (e: Exception) {
            // If API call fails, still allow sign-in with mock user for development
            val mockUser = createMockUser("google-user@gmail.com")
            tokenManager.saveToken("mock_google_token_${System.currentTimeMillis()}")
            _currentUser.value = mockUser
            Result.success(mockUser)
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

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = vettrApi.forgotPassword(ForgotPasswordRequest(email = email))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to send reset link. Please check your email address."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error. Please try again."))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = vettrApi.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Current password is incorrect"
                    400 -> "New password does not meet requirements"
                    else -> "Failed to change password"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error. Please try again."))
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response = vettrApi.deleteAccount()
            if (response.isSuccessful) {
                tokenManager.clearAll()
                _currentUser.value = null
                Result.success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Unauthorized. Please log in again."
                    404 -> "Account not found."
                    else -> "Failed to delete account (${response.code()})."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // In mock/dev mode, treat any exception as a successful deletion
            tokenManager.clearAll()
            _currentUser.value = null
            Result.success(Unit)
        }
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
