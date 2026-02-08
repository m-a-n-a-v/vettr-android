package com.vettr.android.feature.main

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.feature.auth.AuthViewModel
import com.vettr.android.feature.auth.LoginScreen
import com.vettr.android.feature.auth.SignUpScreen
import com.vettr.android.feature.auth.WelcomeScreen
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    // Auth graph routes
    data object AuthGraph : Screen("auth")
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")

    // Main graph routes
    data object MainGraph : Screen("main")
    data object Main : Screen("main_screen")
}

/**
 * Main navigation host for the VETTR app.
 * Handles routing between auth flow and main app flow based on authentication state.
 * Also handles deep links from notifications and external URLs.
 *
 * @param modifier Modifier to be applied to the NavHost.
 * @param navController NavHostController for navigation.
 * @param authRepository Repository for checking authentication state.
 * @param deepLinkUri Deep link URI to navigate to, if any.
 * @param onDeepLinkHandled Callback when deep link has been handled.
 */
@Composable
fun VettrNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository,
    deepLinkUri: Uri? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    // Observe authentication state to handle navigation to/from auth
    val isAuthenticated by authRepository.isAuthenticated().collectAsStateWithLifecycle(initialValue = false)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle deep links
    LaunchedEffect(deepLinkUri) {
        deepLinkUri?.let { uri ->
            if (isAuthenticated) {
                val handled = handleDeepLink(uri, navController, snackbarHostState, coroutineScope)
                if (!handled) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Invalid link: Destination not found")
                    }
                }
            }
            onDeepLinkHandled()
        }
    }

    // Navigate to main when authenticated, to auth when not
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate(Screen.MainGraph.route) {
                popUpTo(Screen.AuthGraph.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.AuthGraph.route) {
                popUpTo(Screen.MainGraph.route) { inclusive = true }
            }
        }
    }

    val startDestination = if (isAuthenticated) {
        Screen.MainGraph.route
    } else {
        Screen.AuthGraph.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth navigation graph
        navigation(
            startDestination = Screen.Welcome.route,
            route = Screen.AuthGraph.route
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStartedClick = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onLogInClick = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onSignUpClick = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onGoogleSignInClick = {
                        // TODO: Handle Google Sign-In in future story
                    }
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    onGoogleSignInClick = {
                        // TODO: Handle Google Sign-In in future story
                    }
                )
            }
        }

        // Main app navigation graph
        navigation(
            startDestination = Screen.Main.route,
            route = Screen.MainGraph.route
        ) {
            composable(Screen.Main.route) {
                MainScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * Handles deep link navigation for vettr:// and https://vettr.app URLs.
 *
 * Supported formats:
 * - vettr://stock/{ticker} -> Navigate to stock detail
 * - vettr://stock/{ticker}?tab=pedigree -> Navigate to stock detail with tab parameter
 * - vettr://alert/{alertId} -> Navigate to alerts screen (alert detail not implemented yet)
 * - vettr://pedigree/{ticker} -> Navigate to stock detail with pedigree tab
 * - https://vettr.app/stock/{ticker} -> Same as vettr://stock/{ticker}
 *
 * @param uri The deep link URI to handle.
 * @param navController NavHostController to navigate.
 * @param snackbarHostState SnackbarHostState to show error messages.
 * @param coroutineScope CoroutineScope for launching snackbar.
 * @return True if deep link was handled, false otherwise.
 */
private suspend fun handleDeepLink(
    uri: Uri,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    coroutineScope: kotlinx.coroutines.CoroutineScope
): Boolean {
    val pathSegments = uri.pathSegments
    if (pathSegments.isEmpty()) return false

    return try {
        when (pathSegments[0]) {
            "stock" -> {
                if (pathSegments.size < 2) return false
                val ticker = pathSegments[1]

                // Navigate to MainGraph first, then to stock detail
                navController.navigate(Screen.MainGraph.route) {
                    popUpTo(Screen.AuthGraph.route) { inclusive = true }
                }

                // Navigate to stock detail screen
                navController.navigate("stock_detail/$ticker")

                // Check for tab query parameter
                val tabParam = uri.getQueryParameter("tab")
                if (tabParam != null) {
                    // Tab parameter will be handled by StockDetailScreen if needed
                    // For now, we just pass the ticker
                }
                true
            }
            "alert" -> {
                if (pathSegments.size < 2) return false
                val alertId = pathSegments[1]

                // Navigate to alerts screen
                navController.navigate(Screen.MainGraph.route) {
                    popUpTo(Screen.AuthGraph.route) { inclusive = true }
                }
                // Navigate to alerts tab (alert detail screen not implemented yet)
                // For now, just go to alerts screen
                true
            }
            "pedigree" -> {
                if (pathSegments.size < 2) return false
                val ticker = pathSegments[1]

                // Navigate to stock detail with pedigree tab
                navController.navigate(Screen.MainGraph.route) {
                    popUpTo(Screen.AuthGraph.route) { inclusive = true }
                }
                navController.navigate("stock_detail/$ticker?tab=pedigree")
                true
            }
            else -> false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
