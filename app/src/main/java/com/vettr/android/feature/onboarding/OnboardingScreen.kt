package com.vettr.android.feature.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vettr.android.designsystem.theme.VettrTheme
import kotlinx.coroutines.launch

/**
 * Onboarding carousel screen with 5 slides introducing key features.
 * Displays: Welcome, Watchlist, VETR Score, Alerts, and Red Flags.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Skip button at top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        viewModel.completeOnboarding()
                        onSkip()
                    }) {
                        Text("Skip", color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // Horizontal pager for slides
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    OnboardingSlide(
                        slide = onboardingSlides[page]
                    )
                }

                // Dot indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                        )
                    }
                }

                // Next/Done button at bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val isLastPage = pagerState.currentPage == 4
                    Button(
                        onClick = {
                            if (isLastPage) {
                                viewModel.completeOnboarding()
                                onComplete()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isLastPage) "Done" else "Next",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual onboarding slide displaying an icon, title, and description.
 */
@Composable
fun OnboardingSlide(
    slide: OnboardingSlideData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = slide.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Data class representing a single onboarding slide.
 */
data class OnboardingSlideData(
    val icon: ImageVector,
    val title: String,
    val description: String
)

/**
 * The 5 onboarding slides shown to new users.
 */
val onboardingSlides = listOf(
    OnboardingSlideData(
        icon = Icons.Filled.Star,
        title = "Welcome to VETTR",
        description = "Your intelligence platform for venture and micro-cap investors. Make informed decisions with comprehensive data on Canadian stocks."
    ),
    OnboardingSlideData(
        icon = Icons.Filled.Favorite,
        title = "Build Your Watchlist",
        description = "Track stocks you care about and receive real-time updates on filings, price changes, and insider transactions."
    ),
    OnboardingSlideData(
        icon = Icons.Filled.CheckCircle,
        title = "VETR Score",
        description = "Our proprietary scoring system evaluates stocks across multiple dimensions to help you identify opportunities and risks at a glance."
    ),
    OnboardingSlideData(
        icon = Icons.Filled.Notifications,
        title = "Smart Alerts",
        description = "Set custom alerts for price movements, filing changes, insider activity, and more. Never miss important updates on your investments."
    ),
    OnboardingSlideData(
        icon = Icons.Filled.Warning,
        title = "Red Flag Detection",
        description = "Automatically identify potential risks like unusual insider selling, accounting irregularities, or concerning executive changes."
    )
)

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    VettrTheme {
        OnboardingScreen(
            onComplete = {},
            onSkip = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingSlidePreview() {
    VettrTheme {
        OnboardingSlide(
            slide = onboardingSlides[0]
        )
    }
}
