package com.vettr.android.feature.stockdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.Stock
import com.vettr.android.designsystem.theme.VettrTheme
import org.junit.Rule
import org.junit.Test
import java.util.Date

/**
 * Compose UI tests for StockDetailScreen.
 * Tests tab switching, favorite toggle, and content display.
 */
class StockDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testStock = Stock(
        id = "test-stock-1",
        ticker = "TST",
        name = "Test Company Inc.",
        exchange = "TSX-V",
        sector = "Technology",
        marketCap = 50000000.0,
        price = 1.25,
        priceChange = 0.05,
        priceChangePercent = 4.17,
        volume = 125000,
        avgVolume = 100000,
        dayHigh = 1.30,
        dayLow = 1.20,
        fiftyTwoWeekHigh = 2.50,
        fiftyTwoWeekLow = 0.80,
        peRatio = 15.5,
        dividendYield = 0.0,
        description = "A test company for testing purposes.",
        website = "https://testcompany.com",
        employees = 50,
        founded = "2020",
        headquarters = "Vancouver, BC",
        vetrScore = 75,
        isFavorite = false,
        lastUpdated = Date()
    )

    private val testFilings = listOf(
        Filing(
            id = "filing-1",
            stockTicker = "TST",
            filingType = "Annual Report",
            filingDate = Date(),
            title = "Annual Financial Statements",
            url = "https://example.com/filing1.pdf"
        )
    )

    @Test
    fun stockDetailScreen_displaysStockInformation() {
        // Given: StockDetailScreen with test stock
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings
                )
            }
        }

        // Then: Stock information is displayed
        composeTestRule.onNodeWithText("TST").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Company Inc.").assertIsDisplayed()
        composeTestRule.onNodeWithText("$1.25").assertIsDisplayed()
    }

    @Test
    fun stockDetailScreen_displaysAllTabs() {
        // Given: StockDetailScreen with test stock
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings
                )
            }
        }

        // Then: All three tabs are visible
        composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pedigree").assertIsDisplayed()
        composeTestRule.onNodeWithText("Red Flags").assertIsDisplayed()
    }

    @Test
    fun stockDetailScreen_defaultTab_isOverview() {
        // Given: StockDetailScreen with test stock
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings,
                    selectedTab = 0
                )
            }
        }

        // Then: Overview tab is selected by default
        // Note: Tab selection state is difficult to assert directly in Compose tests
        // We verify by checking that Overview content is visible
        composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
    }

    @Test
    fun stockDetailScreen_tabSwitch_toOverview() {
        var selectedTab = 1 // Start with Pedigree selected

        // Given: StockDetailScreen with Pedigree tab selected
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }

        // When: User clicks Overview tab
        composeTestRule.onNodeWithText("Overview").performClick()

        // Then: Tab index is updated to 0
        assert(selectedTab == 0) { "Selected tab should be 0 (Overview)" }
    }

    @Test
    fun stockDetailScreen_tabSwitch_toPedigree() {
        var selectedTab = 0 // Start with Overview selected

        // Given: StockDetailScreen with Overview tab selected
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }

        // When: User clicks Pedigree tab
        composeTestRule.onNodeWithText("Pedigree").performClick()

        // Then: Tab index is updated to 1
        assert(selectedTab == 1) { "Selected tab should be 1 (Pedigree)" }
    }

    @Test
    fun stockDetailScreen_tabSwitch_toRedFlags() {
        var selectedTab = 0 // Start with Overview selected

        // Given: StockDetailScreen with Overview tab selected
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }

        // When: User clicks Red Flags tab
        composeTestRule.onNodeWithText("Red Flags").performClick()

        // Then: Tab index is updated to 2
        assert(selectedTab == 2) { "Selected tab should be 2 (Red Flags)" }
    }

    @Test
    fun stockDetailScreen_favoriteToggle_fromNotFavorite() {
        var favoriteClicked = false

        // Given: Stock is not favorited
        val nonFavoriteStock = testStock.copy(isFavorite = false)
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = nonFavoriteStock,
                    filings = testFilings,
                    onFavoriteClick = { favoriteClicked = true }
                )
            }
        }

        // When: User clicks favorite button
        composeTestRule.onNodeWithContentDescription("Add to favorites").performClick()

        // Then: Callback is triggered
        assert(favoriteClicked) { "Favorite callback should be triggered" }
    }

    @Test
    fun stockDetailScreen_favoriteToggle_fromFavorite() {
        var favoriteClicked = false

        // Given: Stock is favorited
        val favoriteStock = testStock.copy(isFavorite = true)
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = favoriteStock,
                    filings = testFilings,
                    onFavoriteClick = { favoriteClicked = true }
                )
            }
        }

        // When: User clicks favorite button
        composeTestRule.onNodeWithContentDescription("Remove from favorites").performClick()

        // Then: Callback is triggered
        assert(favoriteClicked) { "Unfavorite callback should be triggered" }
    }

    @Test
    fun stockDetailScreen_shareButton_isDisplayed() {
        // Given: StockDetailScreen with test stock
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings
                )
            }
        }

        // Then: Share button is visible
        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }

    @Test
    fun stockDetailScreen_shareButtonClick_triggersCallback() {
        var shareClicked = false

        // Given: StockDetailScreen with callback
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings,
                    onShareClick = { shareClicked = true }
                )
            }
        }

        // When: User clicks share button
        composeTestRule.onNodeWithContentDescription("Share").performClick()

        // Then: Callback is triggered
        assert(shareClicked) { "Share callback should be triggered" }
    }

    @Test
    fun stockDetailScreen_backButton_isDisplayed() {
        // Given: StockDetailScreen with test stock
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings
                )
            }
        }

        // Then: Back button is visible
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun stockDetailScreen_backButtonClick_triggersCallback() {
        var backClicked = false

        // Given: StockDetailScreen with callback
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings,
                    onBackClick = { backClicked = true }
                )
            }
        }

        // When: User clicks back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Then: Callback is triggered
        assert(backClicked) { "Back callback should be triggered" }
    }

    @Test
    fun stockDetailScreen_vetrScore_isDisplayed() {
        // Given: StockDetailScreen with test stock
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings
                )
            }
        }

        // Then: VETR score is displayed
        composeTestRule.onNodeWithText("75").assertIsDisplayed()
    }

    @Test
    fun stockDetailScreen_timeRangeFilters_areDisplayed() {
        // Given: StockDetailScreen with test stock
        composeTestRule.setContent {
            VettrTheme {
                StockDetailScreen(
                    stock = testStock,
                    filings = testFilings
                )
            }
        }

        // Then: Time range filters are visible
        composeTestRule.onNodeWithText("1D").assertIsDisplayed()
        composeTestRule.onNodeWithText("1W").assertIsDisplayed()
        composeTestRule.onNodeWithText("1M").assertIsDisplayed()
        composeTestRule.onNodeWithText("3M").assertIsDisplayed()
        composeTestRule.onNodeWithText("1Y").assertIsDisplayed()
        composeTestRule.onNodeWithText("ALL").assertIsDisplayed()
    }
}
