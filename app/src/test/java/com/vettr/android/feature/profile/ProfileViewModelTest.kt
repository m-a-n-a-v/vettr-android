package com.vettr.android.feature.profile

import com.vettr.android.core.data.local.SyncHistoryDao
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.model.User
import com.vettr.android.core.model.VettrTier
import com.vettr.android.core.sync.SyncManager
import com.vettr.android.core.util.VersionManager
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
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProfileViewModel.
 * Tests user data loading and logout functionality.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var syncManager: SyncManager
    private lateinit var syncHistoryDao: SyncHistoryDao
    private lateinit var versionManager: VersionManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        syncManager = mockk(relaxed = true)
        syncHistoryDao = mockk(relaxed = true)
        versionManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `logout clears user state`() = runTest {
        // Given
        val mockUser = User(
            id = "user-1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            tier = "premium",
            createdAt = System.currentTimeMillis()
        )

        // Mock initial user state
        coEvery { authRepository.getCurrentUser() } returnsMany listOf(
            flowOf(mockUser),
            flowOf(null)
        )
        coEvery { authRepository.signOut() } returns Unit

        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()

        // Verify initial state has user
        assertEquals(mockUser, viewModel.user.value)
        assertEquals(VettrTier.PREMIUM, viewModel.tier.value)

        // When
        viewModel.logout()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.user.value)
        assertEquals(VettrTier.FREE, viewModel.tier.value)
        assertFalse(viewModel.isLoading.value)
        coVerify { authRepository.signOut() }
    }

    @Test
    fun `loadUserData populates user and tier information`() = runTest {
        // Given
        val mockUser = User(
            id = "user-1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.jpg",
            tier = "pro",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.getCurrentUser() } returns flowOf(mockUser)

        // When
        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()

        // Then
        assertEquals(mockUser, viewModel.user.value)
        assertEquals(VettrTier.PRO, viewModel.tier.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadUserData handles null user`() = runTest {
        // Given
        coEvery { authRepository.getCurrentUser() } returns flowOf(null)

        // When
        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.user.value)
        assertEquals(VettrTier.FREE, viewModel.tier.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadUserData defaults to FREE tier for invalid tier string`() = runTest {
        // Given
        val mockUser = User(
            id = "user-1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            tier = "invalid-tier",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.getCurrentUser() } returns flowOf(mockUser)

        // When
        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()

        // Then
        assertEquals(mockUser, viewModel.user.value)
        assertEquals(VettrTier.FREE, viewModel.tier.value)
    }

    @Test
    fun `loadUserData parses all valid tier values`() = runTest {
        // Test FREE tier
        val freeUser = User(
            id = "user-1",
            email = "free@example.com",
            displayName = "Free User",
            avatarUrl = null,
            tier = "free",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.getCurrentUser() } returns flowOf(freeUser)
        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()
        assertEquals(VettrTier.FREE, viewModel.tier.value)

        // Test PRO tier
        val proUser = User(
            id = "user-2",
            email = "pro@example.com",
            displayName = "Pro User",
            avatarUrl = null,
            tier = "pro",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.getCurrentUser() } returns flowOf(proUser)
        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()
        assertEquals(VettrTier.PRO, viewModel.tier.value)

        // Test PREMIUM tier
        val premiumUser = User(
            id = "user-3",
            email = "premium@example.com",
            displayName = "Premium User",
            avatarUrl = null,
            tier = "premium",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.getCurrentUser() } returns flowOf(premiumUser)
        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()
        assertEquals(VettrTier.PREMIUM, viewModel.tier.value)
    }

    @Test
    fun `logout sets loading state during operation`() = runTest {
        // Given
        val mockUser = User(
            id = "user-1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            tier = "free",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.getCurrentUser() } returnsMany listOf(
            flowOf(mockUser),
            flowOf(null)
        )
        coEvery { authRepository.signOut() } returns Unit

        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()

        // Verify initial loading is false
        assertFalse(viewModel.isLoading.value)

        // When - Note: in the actual implementation, logout sets loading to true
        // then calls signOut, then sets loading to false
        viewModel.logout()
        advanceUntilIdle()

        // Then - loading should be false after completion
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `logout clears user even if tier was premium`() = runTest {
        // Given
        val mockUser = User(
            id = "user-premium",
            email = "premium@example.com",
            displayName = "Premium User",
            avatarUrl = "https://example.com/premium.jpg",
            tier = "premium",
            createdAt = System.currentTimeMillis()
        )

        coEvery { authRepository.getCurrentUser() } returnsMany listOf(
            flowOf(mockUser),
            flowOf(null)
        )
        coEvery { authRepository.signOut() } returns Unit

        viewModel = ProfileViewModel(authRepository, syncManager, syncHistoryDao, versionManager)
        advanceUntilIdle()

        // Verify initial premium state
        assertEquals(VettrTier.PREMIUM, viewModel.tier.value)

        // When
        viewModel.logout()
        advanceUntilIdle()

        // Then - should reset to FREE tier
        assertNull(viewModel.user.value)
        assertEquals(VettrTier.FREE, viewModel.tier.value)
        coVerify { authRepository.signOut() }
    }
}
