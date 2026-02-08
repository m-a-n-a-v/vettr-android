package com.vettr.android.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
 *
 * @param modifier Modifier to be applied to the NavHost.
 * @param navController NavHostController for navigation.
 * @param authRepository Repository for checking authentication state.
 */
@Composable
fun VettrNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository
) {
    // Observe authentication state to handle navigation to/from auth
    val isAuthenticated by authRepository.isAuthenticated().collectAsStateWithLifecycle(initialValue = false)

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
                // TODO: Replace with MainScreen (with bottom navigation) in US-067
                // For now, show a placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Main Screen - Coming Soon")
                }
            }
        }
    }
}
