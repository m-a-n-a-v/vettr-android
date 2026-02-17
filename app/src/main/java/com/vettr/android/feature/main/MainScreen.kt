package com.vettr.android.feature.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
 * @param windowSizeClass WindowSizeClass for adaptive layouts.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    windowSizeClass: WindowSizeClass
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
            // Bottom navigation destinations use crossfade
            composable(
                route = BottomNavDestination.Pulse.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                PulseScreen(
                    modifier = Modifier.fillMaxSize(),
                    windowSizeClass = windowSizeClass,
                    onStockClick = { stockId ->
                        navController.navigate("stock_detail/$stockId")
                    },
                    onNavigateToStocks = {
                        selectedItemIndex = BottomNavDestination.items.indexOf(BottomNavDestination.Stocks)
                        navController.navigate(BottomNavDestination.Stocks.route) {
                            popUpTo(BottomNavDestination.Pulse.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSeeAllFilings = {
                        selectedItemIndex = BottomNavDestination.items.indexOf(BottomNavDestination.Stocks)
                        navController.navigate(BottomNavDestination.Stocks.route) {
                            popUpTo(BottomNavDestination.Pulse.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSeeAllTopScores = {
                        selectedItemIndex = BottomNavDestination.items.indexOf(BottomNavDestination.Stocks)
                        navController.navigate(BottomNavDestination.Stocks.route) {
                            popUpTo(BottomNavDestination.Pulse.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSeeAllMovers = {
                        selectedItemIndex = BottomNavDestination.items.indexOf(BottomNavDestination.Stocks)
                        navController.navigate(BottomNavDestination.Stocks.route) {
                            popUpTo(BottomNavDestination.Pulse.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(
                route = BottomNavDestination.Discovery.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                DiscoveryScreen(
                    modifier = Modifier.fillMaxSize(),
                    windowSizeClass = windowSizeClass,
                    onStockClick = { stockId ->
                        navController.navigate("stock_detail/$stockId")
                    }
                )
            }
            composable(
                route = BottomNavDestination.Stocks.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                StocksScreen(
                    modifier = Modifier.fillMaxSize(),
                    onStockClick = { stockId ->
                        navController.navigate("stock_detail/$stockId")
                    }
                )
            }
            composable(
                route = BottomNavDestination.Alerts.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                AlertsScreen(
                    modifier = Modifier.fillMaxSize(),
                    onCreateAlert = {
                        navController.navigate("alert_rule_creator")
                    }
                )
            }
            composable(
                route = BottomNavDestination.Profile.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToAbout = {
                        navController.navigate("onboarding")
                    },
                    onNavigateToGlossary = {
                        navController.navigate("glossary")
                    },
                    onNavigateToFaq = {
                        navController.navigate("faq")
                    },
                    onNavigateToTerms = {
                        navController.navigate("terms")
                    },
                    onNavigateToPrivacy = {
                        navController.navigate("privacy")
                    },
                    onNavigateToContact = {
                        navController.navigate("contact")
                    }
                )
            }

            // Detail screens use slide-in from right (forward) / slide-out to right (back)
            composable(
                route = "settings",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                com.vettr.android.feature.profile.SettingsScreen(
                    onBackClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(
                route = "onboarding",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                OnboardingScreen(
                    onComplete = { navController.navigateUp() },
                    onSkip = { navController.navigateUp() }
                )
            }

            composable(
                route = "glossary",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                com.vettr.android.feature.profile.GlossaryScreen(
                    onBackClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(
                route = "faq",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                com.vettr.android.feature.profile.FaqScreen(
                    onBackClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(
                route = "terms",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                com.vettr.android.feature.profile.TermsScreen(
                    onBackClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(
                route = "privacy",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                com.vettr.android.feature.profile.PrivacyScreen(
                    onBackClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(
                route = "contact",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                com.vettr.android.feature.profile.ContactScreen(
                    onBackClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(
                route = "stock_detail/{stockId}?tab={tab}",
                arguments = listOf(
                    navArgument("stockId") { type = NavType.StringType },
                    navArgument("tab") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                StockDetailRoute(
                    onBackClick = { navController.navigateUp() },
                    windowSizeClass = windowSizeClass,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Alert rule creator uses bottom sheet slide-up animation
            composable(
                route = "alert_rule_creator",
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                AlertRuleCreatorScreen(
                    onBackClick = { navController.navigateUp() },
                    onSaveComplete = { navController.navigateUp() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun MainScreenPreview() {
    VettrTheme {
        MainScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun MainScreenTabletPreview() {
    VettrTheme {
        MainScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(840.dp, 1200.dp))
        )
    }
}
