package com.vettr.android.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vettr.android.core.util.AnalyticsService
import com.vettr.android.core.util.FeedbackService
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrTextPrimary
import com.vettr.android.designsystem.theme.VettrTextSecondary
import kotlinx.coroutines.launch

/**
 * Feedback categories for user feedback submissions.
 */
enum class FeedbackCategory(val displayName: String) {
    BUG("Bug"),
    FEATURE("Feature"),
    GENERAL("General")
}

/**
 * Feedback screen - allows users to submit feedback to the VETTR team.
 * Features:
 * - Category picker (Bug/Feature/General)
 * - Multi-line feedback text input
 * - Optional email input
 * - Submit button that sends feedback to support@vettr.ca (mock)
 * - Shows thank you snackbar on successful submission
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    feedbackService: FeedbackService,
    analyticsService: AnalyticsService,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val categories = remember { FeedbackCategory.entries.toList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Feedback") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VettrNavy,
                    titleContentColor = VettrTextPrimary,
                    navigationIconContentColor = VettrTextPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = VettrNavy,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header
            Text(
                text = "We'd love to hear from you",
                style = MaterialTheme.typography.headlineSmall,
                color = VettrTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your feedback helps us improve VETTR for everyone.",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Category picker
            Card(
                colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium,
                        color = VettrTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEachIndexed { index, category ->
                            SegmentedButton(
                                selected = selectedCategory == index,
                                onClick = { selectedCategory = index },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = categories.size
                                ),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = VettrAccent,
                                    activeContentColor = VettrNavy,
                                    inactiveContainerColor = VettrNavy,
                                    inactiveContentColor = VettrTextSecondary
                                )
                            ) {
                                Text(category.displayName)
                            }
                        }
                    }
                }
            }

            // Feedback text input
            Card(
                colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md)
                ) {
                    Text(
                        text = "Your Feedback",
                        style = MaterialTheme.typography.titleMedium,
                        color = VettrTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = { Text("Tell us what's on your mind...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = VettrTextPrimary,
                            unfocusedTextColor = VettrTextPrimary,
                            focusedContainerColor = VettrNavy,
                            unfocusedContainerColor = VettrNavy,
                            focusedBorderColor = VettrAccent,
                            unfocusedBorderColor = VettrTextSecondary,
                            cursorColor = VettrAccent,
                            focusedPlaceholderColor = VettrTextSecondary,
                            unfocusedPlaceholderColor = VettrTextSecondary
                        ),
                        maxLines = 10
                    )
                }
            }

            // Email input (optional)
            Card(
                colors = CardDefaults.cardColors(containerColor = VettrCardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.titleMedium,
                            color = VettrTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Optional",
                            style = MaterialTheme.typography.bodySmall,
                            color = VettrTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        placeholder = { Text("your.email@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = VettrTextPrimary,
                            unfocusedTextColor = VettrTextPrimary,
                            focusedContainerColor = VettrNavy,
                            unfocusedContainerColor = VettrNavy,
                            focusedBorderColor = VettrAccent,
                            unfocusedBorderColor = VettrTextSecondary,
                            cursorColor = VettrAccent,
                            focusedPlaceholderColor = VettrTextSecondary,
                            unfocusedPlaceholderColor = VettrTextSecondary
                        ),
                        singleLine = true
                    )
                    Text(
                        text = "We'll only use this to follow up on your feedback",
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrTextSecondary,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            // Submit button
            Button(
                onClick = {
                    if (feedbackText.isNotBlank()) {
                        coroutineScope.launch {
                            // Record feedback submission
                            val submissionCount = feedbackService.recordFeedbackSubmission()

                            // Track analytics event
                            analyticsService.trackEvent(
                                "feedback_submitted",
                                mapOf(
                                    "category" to categories[selectedCategory].displayName,
                                    "has_email" to emailText.isNotBlank(),
                                    "submission_count" to submissionCount
                                )
                            )

                            // Mock sending to support@vettr.ca
                            // In production, this would send the feedback to a backend service
                            println("Feedback submitted to support@vettr.ca:")
                            println("Category: ${categories[selectedCategory].displayName}")
                            println("Feedback: $feedbackText")
                            println("Email: ${emailText.ifBlank { "Not provided" }}")

                            // Show thank you snackbar
                            snackbarHostState.showSnackbar("Thank you for your feedback!")

                            // Clear form
                            feedbackText = ""
                            emailText = ""
                            selectedCategory = 0
                        }
                    }
                },
                enabled = feedbackText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Submit Feedback",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Help text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = VettrCardBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(Spacing.md)
            ) {
                Column {
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.titleSmall,
                        color = VettrTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "Your feedback is sent directly to our team at support@vettr.ca. We review all submissions and use them to prioritize improvements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrTextSecondary
                    )
                }
            }
        }
    }
}

// Note: Preview is not possible for FeedbackScreen as it requires FeedbackService and AnalyticsService
// which need Android Context and cannot be mocked in preview environment
