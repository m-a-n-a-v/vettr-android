package com.vettr.android.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.feature.alerts.AlertRuleCreatorScreen
import com.vettr.android.feature.alerts.AlertsScreen
import com.vettr.android.feature.discovery.DiscoveryScreen
import com.vettr.android.feature.onboarding.OnboardingScreen
import com.vettr.android.feature.profile.ProfileScreen
import com.vettr.android.feature.pulse.PulseScreen
import com.vettr.android.feature.stockdetail.StockDetailRoute
import com.vettr.android.feature.stockdetail.StocksScreen

/**
 * Bottom navigation destinations for the main app.
 */
sealed class BottomNavDestination(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Pulse : BottomNavDestination("pulse", Icons.AutoMirrored.Filled.TrendingUp, "Pulse")
    data object Discovery : BottomNavDestination("discovery", Icons.Default.Explore, "Discovery")
    data object Stocks : BottomNavDestination("stocks", Icons.AutoMirrored.Filled.List, "Stocks")
    data object Alerts : BottomNavDestination("alerts", Icons.Default.Notifications, "Alerts")
    data object Profile : BottomNavDestination("profile", Icons.Default.Person, "Profile")

    companion object {
        val items = listOf(Pulse, Discovery, Stocks, Alerts, Profile)
    }
}

/**
 * MainScreen with bottom navigation bar for the VETTR app.
 * Provides navigation between 5 main sections: Pulse, Discovery, Stocks, Alerts, and Profile.
 *
 * @param modifier Modifier to be applied to the MainScreen.
 * @param navController NavHostController for nested navigation between bottom tabs.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = VettrNavy,
                contentColor = VettrTextSecondary,
                tonalElevation = 8.dp
            ) {
                BottomNavDestination.items.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            navController.navigate(destination.route) {
                                // Pop up to the start destination to avoid building up a large stack
                                popUpTo(BottomNavDestination.Pulse.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(text = destination.label)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VettrAccent,
                            selectedTextColor = VettrAccent,
                            indicatorColor = VettrNavy,
                            unselectedIconColor = VettrTextSecondary,
                            unselectedTextColor = VettrTextSecondary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavDestination.Pulse.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavDestination.Pulse.route) {
                PulseScreen(
                    modifier = Modifier.fillMaxSize(),
                    onStockClick = { stockId ->
                        navController.navigate("stock_detail/$stockId")
                    }
                )
            }
            composable(BottomNavDestination.Discovery.route) {
                DiscoveryScreen(
                    modifier = Modifier.fillMaxSize(),
                    onStockClick = { stockId ->
                        navController.navigate("stock_detail/$stockId")
                    }
                )
            }
            composable(BottomNavDestination.Stocks.route) {
                StocksScreen(
                    modifier = Modifier.fillMaxSize(),
                    onStockClick = { stockId ->
                        navController.navigate("stock_detail/$stockId")
                    }
                )
            }
            composable(BottomNavDestination.Alerts.route) {
                AlertsScreen(
                    modifier = Modifier.fillMaxSize(),
                    onCreateAlert = {
                        navController.navigate("alert_rule_creator")
                    }
                )
            }
            composable(BottomNavDestination.Profile.route) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToAbout = {
                        navController.navigate("onboarding")
                    }
                )
            }

            // Settings screen
            composable("settings") {
                com.vettr.android.feature.profile.SettingsScreen(
                    onBackClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Onboarding screen
            composable("onboarding") {
                OnboardingScreen(
                    onComplete = { navController.navigateUp() },
                    onSkip = { navController.navigateUp() }
                )
            }

            // Stock detail screen
            composable(
                route = "stock_detail/{stockId}",
                arguments = listOf(navArgument("stockId") { type = NavType.StringType })
            ) {
                StockDetailRoute(
                    onBackClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Alert rule creator screen
            composable("alert_rule_creator") {
                AlertRuleCreatorScreen(
                    onBackClick = { navController.navigateUp() },
                    onSaveComplete = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    VettrTheme {
        MainScreen()
    }
}
