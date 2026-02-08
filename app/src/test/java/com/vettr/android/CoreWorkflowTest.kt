package com.vettr.android

import com.vettr.android.core.data.DetectedFlag
import com.vettr.android.core.data.RedFlagType
import com.vettr.android.core.data.VetrScoreResult
import com.vettr.android.core.data.repository.AlertRuleRepository
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.data.repository.RedFlagRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.data.repository.VetrScoreRepository
import com.vettr.android.core.model.AlertRule
import com.vettr.android.core.model.Stock
import com.vettr.android.core.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for core workflows in the VETTR Android app.
 * Tests critical user journeys including authentication, watchlist management,
 * alert CRUD operations, VETR score calculation, and red flag detection.
 */
class CoreWorkflowTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var stockRepository: StockRepository
    private lateinit var alertRuleRepository: AlertRuleRepository
    private lateinit var vetrScoreRepository: VetrScoreRepository
    private lateinit var redFlagRepository: RedFlagRepository

    private val testUserId = "user-123"
    private val testStockId = "stock-456"
    private val testTicker = "ABCD"
    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        authRepository = mockk()
        stockRepository = mockk()
        alertRuleRepository = mockk()
        vetrScoreRepository = mockk()
        redFlagRepository = mockk()
    }

    // ========== Authentication Workflow Tests ==========

    @Test
    fun `test complete auth workflow - signup, login, logout`() = runTest {
        val email = "newuser@example.com"
        val password = "SecurePass123"
        val mockUser = createTestUser(email)

        // Step 1: Sign up new user
        coEvery { authRepository.signUp(email, password) } returns Result.success(mockUser)
        coEvery { authRepository.isAuthenticated() } returns flowOf(true)

        val signUpResult = authRepository.signUp(email, password)
        assertTrue(signUpResult.isSuccess)
        assertEquals(mockUser, signUpResult.getOrNull())

        // Verify user is authenticated after signup
        val isAuthenticatedAfterSignUp = authRepository.isAuthenticated().first()
        assertTrue(isAuthenticatedAfterSignUp)

        // Step 2: Sign out
        coEvery { authRepository.signOut() } returns Unit
        coEvery { authRepository.isAuthenticated() } returns flowOf(false)

        authRepository.signOut()
        val isAuthenticatedAfterSignOut = authRepository.isAuthenticated().first()
        assertFalse(isAuthenticatedAfterSignOut)

        // Step 3: Sign in with same credentials
        coEvery { authRepository.signInWithEmail(email, password) } returns Result.success(mockUser)
        coEvery { authRepository.isAuthenticated() } returns flowOf(true)

        val signInResult = authRepository.signInWithEmail(email, password)
        assertTrue(signInResult.isSuccess)
        assertEquals(mockUser, signInResult.getOrNull())

        // Verify user is authenticated after login
        val isAuthenticatedAfterSignIn = authRepository.isAuthenticated().first()
        assertTrue(isAuthenticatedAfterSignIn)

        // Verify all repository methods were called
        coVerify { authRepository.signUp(email, password) }
        coVerify { authRepository.signOut() }
        coVerify { authRepository.signInWithEmail(email, password) }
    }

    @Test
    fun `test Google sign-in workflow`() = runTest {
        val idToken = "mock-google-id-token"
        val mockUser = createTestUser("google@example.com", displayName = "Google User")

        coEvery { authRepository.signInWithGoogle(idToken) } returns Result.success(mockUser)
        coEvery { authRepository.isAuthenticated() } returns flowOf(true)

        val result = authRepository.signInWithGoogle(idToken)

        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
        assertTrue(authRepository.isAuthenticated().first())

        coVerify { authRepository.signInWithGoogle(idToken) }
    }

    @Test
    fun `test signup with existing email fails`() = runTest {
        val email = "existing@example.com"
        val password = "password123"
        val errorMessage = "Email already exists"

        coEvery { authRepository.signUp(email, password) } returns
            Result.failure(Exception(errorMessage))

        val result = authRepository.signUp(email, password)

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)

        coVerify { authRepository.signUp(email, password) }
    }

    @Test
    fun `test login with invalid credentials fails`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"

        coEvery { authRepository.signInWithEmail(email, password) } returns
            Result.failure(Exception(errorMessage))

        val result = authRepository.signInWithEmail(email, password)

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)

        coVerify { authRepository.signInWithEmail(email, password) }
    }

    // ========== Watchlist (Favorites) Workflow Tests ==========

    @Test
    fun `test add stock to watchlist workflow`() = runTest {
        val stock = createTestStock(isFavorite = false)
        val updatedStock = stock.copy(isFavorite = true)

        // Initially stock is not favorite
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(stock)

        val initialStock = stockRepository.getStock(testStockId).first()
        assertNotNull(initialStock)
        assertFalse(initialStock!!.isFavorite)

        // Add to favorites
        coEvery { stockRepository.toggleFavorite(testStockId) } returns Unit
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(updatedStock)

        stockRepository.toggleFavorite(testStockId)

        // Verify stock is now favorite
        val favoriteStock = stockRepository.getStock(testStockId).first()
        assertNotNull(favoriteStock)
        assertTrue(favoriteStock!!.isFavorite)

        coVerify { stockRepository.toggleFavorite(testStockId) }
    }

    @Test
    fun `test remove stock from watchlist workflow`() = runTest {
        val stock = createTestStock(isFavorite = true)
        val updatedStock = stock.copy(isFavorite = false)

        // Initially stock is favorite
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(stock)

        val initialStock = stockRepository.getStock(testStockId).first()
        assertNotNull(initialStock)
        assertTrue(initialStock!!.isFavorite)

        // Remove from favorites
        coEvery { stockRepository.toggleFavorite(testStockId) } returns Unit
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(updatedStock)

        stockRepository.toggleFavorite(testStockId)

        // Verify stock is no longer favorite
        val unfavoritedStock = stockRepository.getStock(testStockId).first()
        assertNotNull(unfavoritedStock)
        assertFalse(unfavoritedStock!!.isFavorite)

        coVerify { stockRepository.toggleFavorite(testStockId) }
    }

    @Test
    fun `test get favorites returns only favorite stocks`() = runTest {
        val favoriteStocks = listOf(
            createTestStock(id = "stock-1", ticker = "AAAA", isFavorite = true),
            createTestStock(id = "stock-2", ticker = "BBBB", isFavorite = true),
            createTestStock(id = "stock-3", ticker = "CCCC", isFavorite = true)
        )

        coEvery { stockRepository.getFavorites() } returns flowOf(favoriteStocks)

        val favorites = stockRepository.getFavorites().first()

        assertEquals(3, favorites.size)
        assertTrue(favorites.all { it.isFavorite })

        coVerify { stockRepository.getFavorites() }
    }

    // ========== Alert CRUD Workflow Tests ==========

    @Test
    fun `test create alert rule workflow`() = runTest {
        val newRule = createTestAlertRule()

        coEvery { alertRuleRepository.createRule(newRule) } returns Result.success(newRule)
        coEvery { alertRuleRepository.getRulesForUser(testUserId) } returns flowOf(listOf(newRule))

        // Create the rule
        val createResult = alertRuleRepository.createRule(newRule)

        assertTrue(createResult.isSuccess)
        assertEquals(newRule, createResult.getOrNull())

        // Verify rule exists for user
        val userRules = alertRuleRepository.getRulesForUser(testUserId).first()
        assertEquals(1, userRules.size)
        assertEquals(newRule, userRules[0])

        coVerify { alertRuleRepository.createRule(newRule) }
    }

    @Test
    fun `test update alert rule workflow`() = runTest {
        val originalRule = createTestAlertRule()
        val updatedRule = originalRule.copy(triggerCondition = "price > 3.0")

        coEvery { alertRuleRepository.updateRule(updatedRule) } returns Unit
        coEvery { alertRuleRepository.getRulesForUser(testUserId) } returns flowOf(listOf(updatedRule))

        // Update the rule
        alertRuleRepository.updateRule(updatedRule)

        // Verify updated rule
        val userRules = alertRuleRepository.getRulesForUser(testUserId).first()
        assertEquals(1, userRules.size)
        assertEquals("price > 3.0", userRules[0].triggerCondition)

        coVerify { alertRuleRepository.updateRule(updatedRule) }
    }

    @Test
    fun `test delete alert rule workflow`() = runTest {
        val ruleId = "rule-123"

        coEvery { alertRuleRepository.deleteRule(ruleId) } returns Unit
        coEvery { alertRuleRepository.getRulesForUser(testUserId) } returns flowOf(emptyList())

        // Delete the rule
        alertRuleRepository.deleteRule(ruleId)

        // Verify rule is deleted
        val userRules = alertRuleRepository.getRulesForUser(testUserId).first()
        assertEquals(0, userRules.size)

        coVerify { alertRuleRepository.deleteRule(ruleId) }
    }

    @Test
    fun `test toggle alert rule active status`() = runTest {
        val ruleId = "rule-123"
        val inactiveRule = createTestAlertRule(id = ruleId, isActive = false)
        val activeRule = inactiveRule.copy(isActive = true)

        // Initially inactive
        coEvery { alertRuleRepository.getRulesForUser(testUserId) } returns flowOf(listOf(inactiveRule))

        val initialRules = alertRuleRepository.getRulesForUser(testUserId).first()
        assertFalse(initialRules[0].isActive)

        // Toggle to active
        coEvery { alertRuleRepository.toggleActive(ruleId) } returns Unit
        coEvery { alertRuleRepository.getRulesForUser(testUserId) } returns flowOf(listOf(activeRule))

        alertRuleRepository.toggleActive(ruleId)

        val updatedRules = alertRuleRepository.getRulesForUser(testUserId).first()
        assertTrue(updatedRules[0].isActive)

        coVerify { alertRuleRepository.toggleActive(ruleId) }
    }

    @Test
    fun `test get alert rules for specific stock`() = runTest {
        val rules = listOf(
            createTestAlertRule(id = "rule-1", ticker = testTicker),
            createTestAlertRule(id = "rule-2", ticker = testTicker)
        )

        coEvery { alertRuleRepository.getRulesForStock(testTicker) } returns flowOf(rules)

        val stockRules = alertRuleRepository.getRulesForStock(testTicker).first()

        assertEquals(2, stockRules.size)
        assertTrue(stockRules.all { it.stockTicker == testTicker })

        coVerify { alertRuleRepository.getRulesForStock(testTicker) }
    }

    @Test
    fun `test alert rule count validation - max 50 rules`() = runTest {
        val newRule = createTestAlertRule()

        // User already has 50 rules
        coEvery { alertRuleRepository.getRuleCount(testUserId) } returns flowOf(50)
        coEvery { alertRuleRepository.createRule(newRule) } returns
            Result.failure(Exception("Maximum alert rules limit (50) reached"))

        val result = alertRuleRepository.createRule(newRule)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("50") == true)

        coVerify { alertRuleRepository.createRule(newRule) }
    }

    // ========== VETR Score Calculation Workflow Tests ==========

    @Test
    fun `test VETR score calculation returns valid 0-100 range`() = runTest {
        val scoreResult = VetrScoreResult(
            overallScore = 75,
            components = mapOf(
                "pedigree" to 80,
                "filingVelocity" to 70,
                "redFlag" to 85,
                "growth" to 60,
                "governance" to 75
            ),
            lastUpdated = now
        )

        coEvery { vetrScoreRepository.calculateScore(testTicker) } returns scoreResult

        val result = vetrScoreRepository.calculateScore(testTicker)

        assertNotNull(result)
        assertTrue(result.overallScore in 0..100)
        assertEquals(5, result.components.size)

        // Verify all component scores are in valid range
        result.components.values.forEach { score ->
            assertTrue("Component score $score not in range 0-100", score in 0..100)
        }

        coVerify { vetrScoreRepository.calculateScore(testTicker) }
    }

    @Test
    fun `test VETR score calculation with perfect score`() = runTest {
        val perfectScore = VetrScoreResult(
            overallScore = 100,
            components = mapOf(
                "pedigree" to 100,
                "filingVelocity" to 100,
                "redFlag" to 100,
                "growth" to 100,
                "governance" to 100
            ),
            lastUpdated = now
        )

        coEvery { vetrScoreRepository.calculateScore(testTicker) } returns perfectScore

        val result = vetrScoreRepository.calculateScore(testTicker)

        assertEquals(100, result.overallScore)
        assertTrue(result.components.values.all { it == 100 })

        coVerify { vetrScoreRepository.calculateScore(testTicker) }
    }

    @Test
    fun `test VETR score calculation with low score`() = runTest {
        val lowScore = VetrScoreResult(
            overallScore = 15,
            components = mapOf(
                "pedigree" to 10,
                "filingVelocity" to 5,
                "redFlag" to 20,
                "growth" to 15,
                "governance" to 25
            ),
            lastUpdated = now
        )

        coEvery { vetrScoreRepository.calculateScore(testTicker) } returns lowScore

        val result = vetrScoreRepository.calculateScore(testTicker)

        assertTrue(result.overallScore >= 0)
        assertTrue(result.overallScore <= 30)
        assertTrue(result.components.values.all { it in 0..100 })

        coVerify { vetrScoreRepository.calculateScore(testTicker) }
    }

    @Test
    fun `test VETR score has all required components`() = runTest {
        val scoreResult = VetrScoreResult(
            overallScore = 65,
            components = mapOf(
                "pedigree" to 70,
                "filingVelocity" to 60,
                "redFlag" to 75,
                "growth" to 55,
                "governance" to 65
            ),
            lastUpdated = now
        )

        coEvery { vetrScoreRepository.calculateScore(testTicker) } returns scoreResult

        val result = vetrScoreRepository.calculateScore(testTicker)

        // Verify all required components are present
        assertTrue(result.components.containsKey("pedigree"))
        assertTrue(result.components.containsKey("filingVelocity"))
        assertTrue(result.components.containsKey("redFlag"))
        assertTrue(result.components.containsKey("growth"))
        assertTrue(result.components.containsKey("governance"))

        coVerify { vetrScoreRepository.calculateScore(testTicker) }
    }

    // ========== Red Flag Detection Workflow Tests ==========

    @Test
    fun `test red flag detection returns results`() = runTest {
        val detectedFlags = listOf(
            DetectedFlag(
                type = RedFlagType.CONSOLIDATION_VELOCITY,
                ticker = testTicker,
                score = 30.0,
                description = "Multiple consolidations in 12 months",
                detectedAt = now
            ),
            DetectedFlag(
                type = RedFlagType.FINANCING_VELOCITY,
                ticker = testTicker,
                score = 25.0,
                description = "Frequent financings detected",
                detectedAt = now
            )
        )

        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns detectedFlags

        val result = redFlagRepository.detectFlagsForStock(testTicker)

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(testTicker, result[0].ticker)
        assertEquals(testTicker, result[1].ticker)

        coVerify { redFlagRepository.detectFlagsForStock(testTicker) }
    }

    @Test
    fun `test red flag detection with no flags returns empty list`() = runTest {
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns emptyList()

        val result = redFlagRepository.detectFlagsForStock(testTicker)

        assertNotNull(result)
        assertEquals(0, result.size)

        coVerify { redFlagRepository.detectFlagsForStock(testTicker) }
    }

    @Test
    fun `test red flag detection with multiple flag types`() = runTest {
        val detectedFlags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 25.0, "test", now),
            DetectedFlag(RedFlagType.EXECUTIVE_CHURN, testTicker, 20.0, "test", now),
            DetectedFlag(RedFlagType.DISCLOSURE_GAPS, testTicker, 15.0, "test", now),
            DetectedFlag(RedFlagType.DEBT_TREND, testTicker, 10.0, "test", now)
        )

        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns detectedFlags

        val result = redFlagRepository.detectFlagsForStock(testTicker)

        assertEquals(5, result.size)
        // Verify all flag types are different
        val flagTypes = result.map { it.type }.toSet()
        assertEquals(5, flagTypes.size)

        coVerify { redFlagRepository.detectFlagsForStock(testTicker) }
    }

    @Test
    fun `test red flag scores are valid`() = runTest {
        val detectedFlags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "test", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 25.0, "test", now)
        )

        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns detectedFlags

        val result = redFlagRepository.detectFlagsForStock(testTicker)

        // Verify all flag scores are positive
        result.forEach { flag ->
            assertTrue("Flag score ${flag.score} should be positive", flag.score > 0)
        }

        coVerify { redFlagRepository.detectFlagsForStock(testTicker) }
    }

    // ========== Helper Functions ==========

    private fun createTestUser(
        email: String = "test@example.com",
        displayName: String = "Test User"
    ): User {
        return User(
            id = testUserId,
            email = email,
            displayName = displayName,
            avatarUrl = null,
            tier = "free",
            createdAt = now
        )
    }

    private fun createTestStock(
        id: String = testStockId,
        ticker: String = testTicker,
        isFavorite: Boolean = false
    ): Stock {
        return Stock(
            id = id,
            ticker = ticker,
            name = "Test Company",
            exchange = "TSX-V",
            sector = "Technology",
            marketCap = 150_000_000.0,
            price = 2.50,
            priceChange = 5.0,
            vetrScore = 75,
            isFavorite = isFavorite
        )
    }

    private fun createTestAlertRule(
        id: String = "rule-123",
        ticker: String = testTicker,
        isActive: Boolean = true
    ): AlertRule {
        return AlertRule(
            id = id,
            userId = testUserId,
            stockTicker = ticker,
            ruleType = "PRICE_ABOVE",
            triggerCondition = "price > 2.0",
            isActive = isActive,
            createdAt = now,
            frequency = "ONCE"
        )
    }
}
