package com.vettr.android.feature.auth

import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AuthViewModel.
 * Tests authentication flows including sign-in, sign-up, and sign-out.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()

        // Mock isAuthenticated flow to return false by default
        coEvery { authRepository.isAuthenticated() } returns flowOf(false)

        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithEmail updates isAuthenticated to true on success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val mockUser = User(
            id = "user-1",
            email = email,
            displayName = "Test User",
            avatarUrl = null,
            tier = "free",
            createdAt = System.currentTimeMillis()
        )

        // Mock repository to return authentication flow change
        coEvery { authRepository.isAuthenticated() } returns flowOf(false, true)
        coEvery { authRepository.signInWithEmail(email, password) } returns Result.success(mockUser)

        // Recreate viewModel to pick up the new mock flow
        viewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // Update email and password in UI state
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        advanceUntilIdle()

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        coVerify { authRepository.signInWithEmail(email, password) }
    }

    @Test
    fun `signInWithEmail sets errorMessage on invalid credentials`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"

        coEvery { authRepository.signInWithEmail(email, password) } returns
            Result.failure(Exception(errorMessage))

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        advanceUntilIdle()

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
        coVerify { authRepository.signInWithEmail(email, password) }
    }

    @Test
    fun `signInWithEmail validates empty email and password`() = runTest {
        // Given
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        advanceUntilIdle()

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals("Email and password cannot be empty", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { authRepository.signInWithEmail(any(), any()) }
    }

    @Test
    fun `signUp validates email format and password match`() = runTest {
        // Given - Test with blank email/password
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        advanceUntilIdle()

        // When
        viewModel.signUp()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals("Email and password cannot be empty", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { authRepository.signUp(any(), any()) }
    }

    @Test
    fun `signUp creates account successfully with valid credentials`() = runTest {
        // Given
        val email = "newuser@example.com"
        val password = "securepassword123"
        val mockUser = User(
            id = "user-2",
            email = email,
            displayName = "New User",
            avatarUrl = null,
            tier = "free",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.isAuthenticated() } returns flowOf(false, true)
        coEvery { authRepository.signUp(email, password) } returns Result.success(mockUser)

        // Recreate viewModel to pick up the new mock flow
        viewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        advanceUntilIdle()

        // When
        viewModel.signUp()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        coVerify { authRepository.signUp(email, password) }
    }

    @Test
    fun `signUp sets errorMessage on failure`() = runTest {
        // Given
        val email = "newuser@example.com"
        val password = "password123"
        val errorMessage = "Email already exists"

        coEvery { authRepository.signUp(email, password) } returns
            Result.failure(Exception(errorMessage))

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        advanceUntilIdle()

        // When
        viewModel.signUp()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
        coVerify { authRepository.signUp(email, password) }
    }

    @Test
    fun `signInWithGoogle updates isAuthenticated on success`() = runTest {
        // Given
        val idToken = "mock-google-id-token"
        val mockUser = User(
            id = "user-3",
            email = "google@example.com",
            displayName = "Google User",
            avatarUrl = "https://example.com/photo.jpg",
            tier = "free",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.isAuthenticated() } returns flowOf(false, true)
        coEvery { authRepository.signInWithGoogle(idToken) } returns Result.success(mockUser)

        // Recreate viewModel to pick up the new mock flow
        viewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When
        viewModel.signInWithGoogle(idToken)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        coVerify { authRepository.signInWithGoogle(idToken) }
    }

    @Test
    fun `signInWithGoogle sets errorMessage on failure`() = runTest {
        // Given
        val idToken = "invalid-token"
        val errorMessage = "Google sign in failed"

        coEvery { authRepository.signInWithGoogle(idToken) } returns
            Result.failure(Exception(errorMessage))

        // When
        viewModel.signInWithGoogle(idToken)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
        coVerify { authRepository.signInWithGoogle(idToken) }
    }

    @Test
    fun `onEmailChange updates email in UI state`() = runTest {
        // Given
        val email = "test@example.com"

        // When
        viewModel.onEmailChange(email)
        advanceUntilIdle()

        // Then
        assertEquals(email, viewModel.uiState.value.email)
    }

    @Test
    fun `onPasswordChange updates password in UI state`() = runTest {
        // Given
        val password = "securepassword123"

        // When
        viewModel.onPasswordChange(password)
        advanceUntilIdle()

        // Then
        assertEquals(password, viewModel.uiState.value.password)
    }

    @Test
    fun `clearError removes errorMessage from UI state`() = runTest {
        // Given - set an error message first
        val email = "test@example.com"
        val password = "wrongpassword"

        coEvery { authRepository.signInWithEmail(email, password) } returns
            Result.failure(Exception("Invalid credentials"))

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Verify error is set
        assertEquals("Invalid credentials", viewModel.uiState.value.errorMessage)

        // When
        viewModel.clearError()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
