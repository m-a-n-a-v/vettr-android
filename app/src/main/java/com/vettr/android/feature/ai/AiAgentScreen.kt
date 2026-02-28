package com.vettr.android.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.data.remote.AiAgentResponse
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrSurfaceVariant
import com.vettr.android.designsystem.theme.VettrTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAgentScreen(
    modifier: Modifier = Modifier,
    viewModel: AiAgentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Usage indicator
        uiState.usage?.let { usage ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Questions: ${usage.questionsUsed}/${usage.questionsLimit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrTextSecondary
                )
                Text(
                    text = "Resets: ${usage.resetAt.take(10)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VettrTextSecondary
                )
            }
        }

        // Conversation area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item { Spacer(modifier = Modifier.height(Spacing.sm)) }

            // Welcome state if no messages
            if (uiState.messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        Text(
                            text = "VETTR AI Agent",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ask questions about any mining stock. Enter a ticker and ask away.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VettrTextSecondary
                        )
                    }
                }

                // Suggested questions
                if (uiState.suggestedQuestions.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = "Suggested Questions",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(uiState.suggestedQuestions) { question ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(VettrSurfaceVariant)
                                .clickable {
                                    viewModel.askQuestion(questionId = question.id)
                                }
                                .padding(Spacing.md)
                        ) {
                            Column {
                                Text(
                                    text = question.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VettrAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = question.question,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Messages
            items(uiState.messages) { message ->
                if (message.isUser) {
                    UserMessageBubble(message = message.content)
                } else {
                    AiMessageBubble(
                        message = message.content,
                        response = message.response,
                        onFollowUpClick = { viewModel.askFollowUp(it) }
                    )
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = VettrAccent,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Thinking...",
                            style = MaterialTheme.typography.bodySmall,
                            color = VettrTextSecondary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(Spacing.sm)) }
        }

        // Error
        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = VettrRed,
                modifier = Modifier.padding(horizontal = Spacing.md)
            )
        }

        // Input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(Spacing.sm)
        ) {
            // Ticker input
            OutlinedTextField(
                value = uiState.ticker,
                onValueChange = { viewModel.updateTicker(it) },
                label = { Text("Ticker") },
                placeholder = { Text("e.g., AAPL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VettrAccent,
                    unfocusedBorderColor = VettrTextSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = VettrAccent,
                    cursorColor = VettrAccent
                ),
                trailingIcon = {
                    if (uiState.messages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearConversation() }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = VettrTextSecondary
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Question input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.updateInput(it) },
                    placeholder = { Text("Ask about this stock...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VettrAccent,
                        unfocusedBorderColor = VettrTextSecondary.copy(alpha = 0.3f),
                        cursorColor = VettrAccent
                    )
                )

                IconButton(
                    onClick = {
                        viewModel.askQuestion(customQuestion = uiState.inputText)
                    },
                    enabled = !uiState.isLoading && uiState.ticker.isNotBlank()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (!uiState.isLoading && uiState.ticker.isNotBlank()) VettrAccent else VettrTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun UserMessageBubble(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                .background(VettrAccent)
                .padding(Spacing.md)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiMessageBubble(
    message: String,
    response: AiAgentResponse?,
    onFollowUpClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .background(VettrSurfaceVariant)
                .padding(Spacing.md)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Data points
                if (response != null && response.dataPoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    response.dataPoints.forEach { dp ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dp.key,
                                style = MaterialTheme.typography.labelSmall,
                                color = VettrTextSecondary
                            )
                            Text(
                                text = dp.value,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Follow-up questions
        if (response != null && response.followUpQuestions.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                response.followUpQuestions.forEach { question ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(VettrAccent.copy(alpha = 0.1f))
                            .clickable { onFollowUpClick(question) }
                            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.labelSmall,
                            color = VettrAccent,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
