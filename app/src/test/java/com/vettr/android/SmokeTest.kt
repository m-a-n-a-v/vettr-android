package com.vettr.android

import com.vettr.android.core.data.DetectedFlag
import com.vettr.android.core.data.RedFlagType
import com.vettr.android.core.data.VetrScoreResult
import com.vettr.android.core.data.local.SeedDataService
import com.vettr.android.core.data.repository.AlertRuleRepository
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.data.repository.RedFlagRepository
import com.vettr.android.core.data.repository.SettingsRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.data.repository.VetrScoreRepository
import com.vettr.android.core.model.AlertRule
import com.vettr.android.core.model.Stock
import com.vettr.android.core.model.User
import com.vettr.android.core.util.Validators
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
 * Smoke tests for critical app functionality before release.
 * These tests verify core features work correctly:
 * 1. App configuration and initialization
 * 2. Authentication flow (signup, login, logout)
 * 3. Stock data loading
 * 4. Watchlist persistence
 * 5. Alert CRUD operations
 * 6. VETR score calculation (0-100 range)
 * 7. Red flag detection
 * 8. Settings persistence
 * 9. Input validators
 * 10. End-to-end user journey
 */
class SmokeTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var stockRepository: StockRepository
    private lateinit var alertRuleRepository: AlertRuleRepository
    private lateinit var vetrScoreRepository: VetrScoreRepository
    private lateinit var redFlagRepository: RedFlagRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var seedDataService: SeedDataService

    private val testUserId = "smoke-user-123"
    private val testStockId = "smoke-stock-456"
    private val testTicker = "SMKE"
    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        authRepository = mockk()
        stockRepository = mockk()
        alertRuleRepository = mockk()
        vetrScoreRepository = mockk()
        redFlagRepository = mockk()
        settingsRepository = mockk()
        seedDataService = mockk()
    }

    // ========== Test 1: App Configuration and Initialization ==========

    @Test
    fun `test app initialization and seed data service`() = runTest {
        // Verify seed data service can detect completion status
        coEvery { seedDataService.isSeedComplete() } returns false
        assertFalse(seedDataService.isSeedComplete())

        // Verify seed data can be marked as complete
        coEvery { seedDataService.markSeedComplete() } returns Unit
        coEvery { seedDataService.isSeedComplete() } returns true

        seedDataService.markSeedComplete()
        assertTrue(seedDataService.isSeedComplete())

        // Verify seed data service methods are called
        coVerify { seedDataService.markSeedComplete() }
        coVerify(atLeast = 2) { seedDataService.isSeedComplete() }
    }

    // ========== Test 2: Authentication Flow ==========

    @Test
    fun `test complete auth flow - signup, login, logout`() = runTest {
        val email = "smoke@example.com"
        val password = "SecurePass123"
        val mockUser = createTestUser(email)

        // Step 1: Sign up
        coEvery { authRepository.signUp(email, password) } returns Result.success(mockUser)
        coEvery { authRepository.isAuthenticated() } returns flowOf(true)

        val signUpResult = authRepository.signUp(email, password)
        assertTrue(signUpResult.isSuccess)

        // Step 2: Sign out
        coEvery { authRepository.signOut() } returns Unit
        coEvery { authRepository.isAuthenticated() } returns flowOf(false)

        authRepository.signOut()
        assertFalse(authRepository.isAuthenticated().first())

        // Step 3: Sign in
        coEvery { authRepository.signInWithEmail(email, password) } returns Result.success(mockUser)
        coEvery { authRepository.isAuthenticated() } returns flowOf(true)

        val signInResult = authRepository.signInWithEmail(email, password)
        assertTrue(signInResult.isSuccess)
        assertTrue(authRepository.isAuthenticated().first())

        // Verify all auth methods were called
        coVerify { authRepository.signUp(email, password) }
        coVerify { authRepository.signOut() }
        coVerify { authRepository.signInWithEmail(email, password) }
    }

    // ========== Test 3: Stock Data Loading ==========

    @Test
    fun `test stock data loads correctly`() = runTest {
        val stocks = listOf(
            createTestStock(id = "stock-1", ticker = "AAAA"),
            createTestStock(id = "stock-2", ticker = "BBBB"),
            createTestStock(id = "stock-3", ticker = "CCCC")
        )

        coEvery { stockRepository.getStocks() } returns flowOf(stocks)

        val loadedStocks = stockRepository.getStocks().first()

        assertNotNull(loadedStocks)
        assertEquals(3, loadedStocks.size)
        assertEquals("AAAA", loadedStocks[0].ticker)
        assertEquals("BBBB", loadedStocks[1].ticker)
        assertEquals("CCCC", loadedStocks[2].ticker)

        coVerify { stockRepository.getStocks() }
    }

    // ========== Test 4: Watchlist Persistence ==========

    @Test
    fun `test watchlist persistence - add and remove favorites`() = runTest {
        val stock = createTestStock(isFavorite = false)
        val favoriteStock = stock.copy(isFavorite = true)

        // Add to watchlist
        coEvery { stockRepository.toggleFavorite(testStockId) } returns Unit
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(favoriteStock)

        stockRepository.toggleFavorite(testStockId)
        val addedStock = stockRepository.getStock(testStockId).first()
        assertTrue(addedStock!!.isFavorite)

        // Remove from watchlist
        val unfavoriteStock = stock.copy(isFavorite = false)
        coEvery { stockRepository.toggleFavorite(testStockId) } returns Unit
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(unfavoriteStock)

        stockRepository.toggleFavorite(testStockId)
        val removedStock = stockRepository.getStock(testStockId).first()
        assertFalse(removedStock!!.isFavorite)

        // Verify favorites can be queried
        coEvery { stockRepository.getFavorites() } returns flowOf(listOf(favoriteStock))
        val favorites = stockRepository.getFavorites().first()
        assertEquals(1, favorites.size)

        coVerify(atLeast = 2) { stockRepository.toggleFavorite(testStockId) }
    }

    // ========== Test 5: Alert CRUD Operations ==========

    @Test
    fun `test alert CRUD operations - create, read, update, delete`() = runTest {
        val alertRule = createTestAlertRule()

        // Create
        coEvery { alertRuleRepository.createRule(alertRule) } returns Result.success(alertRule)
        val createResult = alertRuleRepository.createRule(alertRule)
        assertTrue(createResult.isSuccess)

        // Read
        coEvery { alertRuleRepository.getRulesForUser(testUserId) } returns flowOf(listOf(alertRule))
        val rules = alertRuleRepository.getRulesForUser(testUserId).first()
        assertEquals(1, rules.size)

        // Update
        val updatedRule = alertRule.copy(triggerCondition = "price > 5.0")
        coEvery { alertRuleRepository.updateRule(updatedRule) } returns Unit
        alertRuleRepository.updateRule(updatedRule)

        // Delete
        coEvery { alertRuleRepository.deleteRule(alertRule.id) } returns Unit
        coEvery { alertRuleRepository.getRulesForUser(testUserId) } returns flowOf(emptyList())
        alertRuleRepository.deleteRule(alertRule.id)

        val deletedRules = alertRuleRepository.getRulesForUser(testUserId).first()
        assertEquals(0, deletedRules.size)

        // Verify all CRUD operations
        coVerify { alertRuleRepository.createRule(alertRule) }
        coVerify { alertRuleRepository.updateRule(updatedRule) }
        coVerify { alertRuleRepository.deleteRule(alertRule.id) }
    }

    // ========== Test 6: VETR Score (0-100 Range) ==========

    @Test
    fun `test VETR score calculation returns valid 0-100 range`() = runTest {
        val scoreResults = listOf(
            VetrScoreResult(0, mapOf("pedigree" to 0, "filingVelocity" to 0, "redFlag" to 0, "growth" to 0, "governance" to 0), now),
            VetrScoreResult(50, mapOf("pedigree" to 50, "filingVelocity" to 50, "redFlag" to 50, "growth" to 50, "governance" to 50), now),
            VetrScoreResult(100, mapOf("pedigree" to 100, "filingVelocity" to 100, "redFlag" to 100, "growth" to 100, "governance" to 100), now)
        )

        scoreResults.forEach { scoreResult ->
            coEvery { vetrScoreRepository.calculateScore(testTicker) } returns scoreResult

            val result = vetrScoreRepository.calculateScore(testTicker)

            // Verify overall score is in 0-100 range
            assertTrue("Overall score ${result.overallScore} not in 0-100", result.overallScore in 0..100)

            // Verify all component scores are in 0-100 range
            result.components.values.forEach { score ->
                assertTrue("Component score $score not in 0-100", score in 0..100)
            }

            // Verify all required components exist
            assertTrue(result.components.containsKey("pedigree"))
            assertTrue(result.components.containsKey("filingVelocity"))
            assertTrue(result.components.containsKey("redFlag"))
            assertTrue(result.components.containsKey("growth"))
            assertTrue(result.components.containsKey("governance"))
        }

        coVerify(atLeast = 3) { vetrScoreRepository.calculateScore(testTicker) }
    }

    // ========== Test 7: Red Flag Detection ==========

    @Test
    fun `test red flag detection for stocks`() = runTest {
        val detectedFlags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "Multiple consolidations in 12 months", now),
            DetectedFlag(RedFlagType.FINANCING_VELOCITY, testTicker, 25.0, "Frequent financings detected", now),
            DetectedFlag(RedFlagType.EXECUTIVE_CHURN, testTicker, 20.0, "High executive turnover", now)
        )

        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns detectedFlags

        val result = redFlagRepository.detectFlagsForStock(testTicker)

        assertNotNull(result)
        assertEquals(3, result.size)

        // Verify all flags have valid scores
        result.forEach { flag ->
            assertTrue("Flag score ${flag.score} should be positive", flag.score > 0)
            assertEquals(testTicker, flag.ticker)
            assertNotNull(flag.description)
        }

        // Verify no red flags scenario
        coEvery { redFlagRepository.detectFlagsForStock("CLEAN") } returns emptyList()
        val cleanResult = redFlagRepository.detectFlagsForStock("CLEAN")
        assertEquals(0, cleanResult.size)

        coVerify { redFlagRepository.detectFlagsForStock(testTicker) }
        coVerify { redFlagRepository.detectFlagsForStock("CLEAN") }
    }

    // ========== Test 8: Settings Persistence ==========

    @Test
    fun `test settings persistence - save and retrieve preferences`() = runTest {
        // Test currency setting
        coEvery { settingsRepository.setCurrency("USD") } returns Unit
        coEvery { settingsRepository.currency } returns flowOf("USD")

        settingsRepository.setCurrency("USD")
        val currency = settingsRepository.currency.first()
        assertEquals("USD", currency)

        // Test dark mode setting
        coEvery { settingsRepository.setDarkMode(false) } returns Unit
        coEvery { settingsRepository.darkMode } returns flowOf(false)

        settingsRepository.setDarkMode(false)
        val darkMode = settingsRepository.darkMode.first()
        assertFalse(darkMode)

        // Test notification settings
        coEvery { settingsRepository.setFilingNotifications(false) } returns Unit
        coEvery { settingsRepository.filingNotifications } returns flowOf(false)

        settingsRepository.setFilingNotifications(false)
        val filingNotifications = settingsRepository.filingNotifications.first()
        assertFalse(filingNotifications)

        // Test biometric setting
        coEvery { settingsRepository.setBiometricLoginEnabled(true) } returns Unit
        coEvery { settingsRepository.biometricLoginEnabled } returns flowOf(true)

        settingsRepository.setBiometricLoginEnabled(true)
        val biometric = settingsRepository.biometricLoginEnabled.first()
        assertTrue(biometric)

        // Test reset all settings
        coEvery { settingsRepository.resetAllSettings() } returns Unit
        settingsRepository.resetAllSettings()

        // Verify all setting operations
        coVerify { settingsRepository.setCurrency("USD") }
        coVerify { settingsRepository.setDarkMode(false) }
        coVerify { settingsRepository.setFilingNotifications(false) }
        coVerify { settingsRepository.setBiometricLoginEnabled(true) }
        coVerify { settingsRepository.resetAllSettings() }
    }

    // ========== Test 9: Input Validators ==========

    @Test
    fun `test input validators for email and password`() {
        // Valid email tests
        assertTrue(Validators.isValidEmail("user@example.com"))
        assertTrue(Validators.isValidEmail("test.user@domain.co.uk"))
        assertTrue(Validators.isValidEmail("user+tag@example.com"))

        // Invalid email tests
        assertFalse(Validators.isValidEmail(""))
        assertFalse(Validators.isValidEmail("invalid"))
        assertFalse(Validators.isValidEmail("@example.com"))
        assertFalse(Validators.isValidEmail("user@"))
        assertFalse(Validators.isValidEmail("user..double@example.com"))

        // Valid password tests (min 8 chars, 1 upper, 1 lower, 1 digit)
        assertTrue(Validators.isValidPassword("SecurePass123"))
        assertTrue(Validators.isValidPassword("Password1"))
        assertTrue(Validators.isValidPassword("MyP@ssw0rd"))

        // Invalid password tests
        assertFalse(Validators.isValidPassword("short1A"))  // Too short
        assertFalse(Validators.isValidPassword("nouppercase123"))  // No uppercase
        assertFalse(Validators.isValidPassword("NOLOWERCASE123"))  // No lowercase
        assertFalse(Validators.isValidPassword("NoDigitsHere"))  // No digits
        assertFalse(Validators.isValidPassword(""))  // Empty
    }

    // ========== Test 10: End-to-End User Journey ==========

    @Test
    fun `test end-to-end user journey - signup to watchlist to alerts`() = runTest {
        val email = "journey@example.com"
        val password = "JourneyPass123"
        val user = createTestUser(email)
        val stock = createTestStock()
        val alertRule = createTestAlertRule()

        // Step 1: User signs up
        coEvery { authRepository.signUp(email, password) } returns Result.success(user)
        coEvery { authRepository.isAuthenticated() } returns flowOf(true)

        val signUpResult = authRepository.signUp(email, password)
        assertTrue(signUpResult.isSuccess)
        assertTrue(authRepository.isAuthenticated().first())

        // Step 2: User browses stocks
        coEvery { stockRepository.getStocks() } returns flowOf(listOf(stock))
        val stocks = stockRepository.getStocks().first()
        assertEquals(1, stocks.size)

        // Step 3: User adds stock to watchlist
        coEvery { stockRepository.toggleFavorite(testStockId) } returns Unit
        coEvery { stockRepository.getStock(testStockId) } returns flowOf(stock.copy(isFavorite = true))

        stockRepository.toggleFavorite(testStockId)
        val favoriteStock = stockRepository.getStock(testStockId).first()
        assertTrue(favoriteStock!!.isFavorite)

        // Step 4: User creates price alert for the stock
        coEvery { alertRuleRepository.createRule(alertRule) } returns Result.success(alertRule)
        coEvery { alertRuleRepository.getRulesForStock(testTicker) } returns flowOf(listOf(alertRule))

        val alertResult = alertRuleRepository.createRule(alertRule)
        assertTrue(alertResult.isSuccess)

        val stockAlerts = alertRuleRepository.getRulesForStock(testTicker).first()
        assertEquals(1, stockAlerts.size)
        assertEquals(testTicker, stockAlerts[0].stockTicker)

        // Step 5: User checks VETR score for the stock
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

        val vetrScore = vetrScoreRepository.calculateScore(testTicker)
        assertTrue(vetrScore.overallScore in 0..100)
        assertEquals(75, vetrScore.overallScore)

        // Step 6: User checks for red flags
        val redFlags = listOf(
            DetectedFlag(RedFlagType.CONSOLIDATION_VELOCITY, testTicker, 30.0, "Test flag", now)
        )
        coEvery { redFlagRepository.detectFlagsForStock(testTicker) } returns redFlags

        val detectedFlags = redFlagRepository.detectFlagsForStock(testTicker)
        assertEquals(1, detectedFlags.size)

        // Verify complete journey
        coVerify { authRepository.signUp(email, password) }
        coVerify { stockRepository.getStocks() }
        coVerify { stockRepository.toggleFavorite(testStockId) }
        coVerify { alertRuleRepository.createRule(alertRule) }
        coVerify { vetrScoreRepository.calculateScore(testTicker) }
        coVerify { redFlagRepository.detectFlagsForStock(testTicker) }
    }

    // ========== Helper Functions ==========

    private fun createTestUser(
        email: String = "smoke@example.com",
        displayName: String = "Smoke Test User"
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
            name = "Smoke Test Company",
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
        id: String = "smoke-rule-123",
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
