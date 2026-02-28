package com.vettr.android.core.data.repository

import clerk.android.Clerk
import clerk.android.resource.SignIn
import clerk.android.resource.SignUp
import com.vettr.android.core.data.local.TokenManager
import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.model.User
import com.vettr.android.core.model.VettrTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clerk-backed implementation of AuthRepository.
 *
 * Delegates sign-in/sign-up to Clerk's Android SDK.
 * Clerk manages token storage and refresh automatically.
 * The AuthInterceptor reads the current Clerk session token on every request.
 */
@Singleton
class ClerkAuthRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager,
    private val vettrApi: VettrApi
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        // Restore session from Clerk if a session already exists (e.g. after app restart).
        val clerkUser = Clerk.shared.user
        if (clerkUser != null) {
            _currentUser.value = mapClerkUser(clerkUser)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val signIn = SignIn.create(strategy = SignIn.CreateParams.Password(
                identifier = email,
                password = password
            ))
            if (signIn.status == "complete") {
                val user = mapClerkUser(Clerk.shared.user!!)
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in incomplete. Please check your credentials."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sign in failed. Please try again."))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            // Clerk handles Google OAuth — this method is called after Clerk's OAuth callback
            // with the resulting Clerk session. Map the current Clerk user.
            val clerkUser = Clerk.shared.user
                ?: return Result.failure(Exception("Google sign in incomplete."))
            val user = mapClerkUser(clerkUser)
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Google sign in failed."))
        }
    }

    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val signUp = SignUp.create(
                strategy = SignUp.CreateParams.EmailPassword(
                    emailAddress = email,
                    password = password
                )
            )
            if (signUp.status == "complete") {
                val user = mapClerkUser(Clerk.shared.user!!)
                _currentUser.value = user
                Result.success(user)
            } else {
                // Email verification may be required
                Result.failure(Exception("Please verify your email to continue."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sign up failed. Please try again."))
        }
    }

    override suspend fun signOut() {
        try {
            Clerk.shared.signOut()
        } finally {
            tokenManager.clearAll()
            _currentUser.value = null
        }
    }

    override fun getCurrentUser(): Flow<User?> = _currentUser

    override fun isAuthenticated(): Flow<Boolean> = _currentUser.map { it != null }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = vettrApi.forgotPassword(
                com.vettr.android.core.data.remote.ForgotPasswordRequest(email = email)
            )
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
                com.vettr.android.core.data.remote.ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to change password."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error. Please try again."))
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response = vettrApi.deleteAccount()
            if (response.isSuccessful) {
                signOut()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete account (${response.code()})."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error. Please try again."))
        }
    }

    // MARK: - Private helpers

    private fun mapClerkUser(clerkUser: clerk.android.resource.User): User {
        val email = clerkUser.emailAddresses.firstOrNull()?.emailAddress ?: ""
        val displayName = listOfNotNull(clerkUser.firstName, clerkUser.lastName)
            .joinToString(" ")
            .trim()
            .ifEmpty { email.substringBefore("@") }
        return User(
            id = clerkUser.id,
            email = email,
            displayName = displayName,
            avatarUrl = clerkUser.imageUrl,
            tier = VettrTier.FREE.name,
            createdAt = System.currentTimeMillis()
        )
    }
}
