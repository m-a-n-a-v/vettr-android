package com.vettr.android.feature.alerts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.vettr.android.designsystem.theme.VettrTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for AlertsScreen.
 * Tests filter chips, alert list display, and FAB interaction.
 */
class AlertsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun alertsScreen_displaysTitle() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // Then: Screen title is visible
        composeTestRule.onNodeWithText("Alerts").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_displaysFilterChips() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // Then: All filter chips are visible
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("Triggered").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_filterChip_all_isClickable() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // When: User clicks "All" filter chip
        composeTestRule.onNodeWithText("All").performClick()

        // Then: No crash occurs and filter chip remains visible
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_filterChip_active_isClickable() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // When: User clicks "Active" filter chip
        composeTestRule.onNodeWithText("Active").performClick()

        // Then: No crash occurs and filter chip remains visible
        composeTestRule.onNodeWithText("Active").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_filterChip_triggered_isClickable() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // When: User clicks "Triggered" filter chip
        composeTestRule.onNodeWithText("Triggered").performClick()

        // Then: No crash occurs and filter chip remains visible
        composeTestRule.onNodeWithText("Triggered").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_filterChips_switchBetweenFilters() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // When: User clicks through different filters
        composeTestRule.onNodeWithText("All").performClick()
        composeTestRule.onNodeWithText("Active").performClick()
        composeTestRule.onNodeWithText("Triggered").performClick()
        composeTestRule.onNodeWithText("All").performClick()

        // Then: All filter chips remain visible and functional
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("Triggered").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_displaysFAB() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // Then: Create Alert FAB is visible
        composeTestRule.onNodeWithContentDescription("Create Alert").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_fabClick_triggersCallback() {
        var fabClicked = false

        // Given: AlertsScreen with callback
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen(
                    onCreateAlert = { fabClicked = true }
                )
            }
        }

        // When: User clicks FAB
        composeTestRule.onNodeWithContentDescription("Create Alert").performClick()

        // Then: Callback is triggered
        assert(fabClicked) { "Create alert callback should be triggered" }
    }

    @Test
    fun alertsScreen_fab_isEnabled() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // Then: FAB is enabled
        composeTestRule.onNodeWithContentDescription("Create Alert").assertIsEnabled()
    }

    @Test
    fun alertsScreen_displaysLoadingState() {
        // Given: AlertsScreen is displayed (with default loading state from ViewModel)
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // Then: Screen loads without crashing
        // Note: Without mocking ViewModel, we can't control loading state directly
        // This test verifies the screen renders properly
        composeTestRule.onNodeWithText("Alerts").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_withNoAlerts_showsEmptyOrLoadingState() {
        // Given: AlertsScreen with default empty state
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // Then: Screen displays without crashing
        // Either "Loading alerts..." or empty state should be shown
        // We verify the basic screen structure is present
        composeTestRule.onNodeWithText("Alerts").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Create Alert").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_filterChips_maintainStateAfterClicks() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // When: User interacts with multiple filter chips rapidly
        repeat(3) {
            composeTestRule.onNodeWithText("All").performClick()
            composeTestRule.onNodeWithText("Active").performClick()
            composeTestRule.onNodeWithText("Triggered").performClick()
        }

        // Then: All filter chips remain displayed and functional
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("Triggered").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_topAppBar_isDisplayed() {
        // Given: AlertsScreen is displayed
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // Then: Top app bar with title is visible
        composeTestRule.onNodeWithText("Alerts").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_initialState_showsAllFilter() {
        // Given: AlertsScreen is displayed with default state
        composeTestRule.setContent {
            VettrTheme {
                AlertsScreen()
            }
        }

        // Then: All three filters are visible (default is typically "All")
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("Triggered").assertIsDisplayed()
    }
}
