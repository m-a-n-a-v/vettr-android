package com.vettr.android.feature.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.core.data.remote.NewsArticleResponse
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrSurfaceVariant
import com.vettr.android.designsystem.theme.VettrTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    onArticleClick: (String) -> Unit = {},
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        // Source filter chips
        if (uiState.sources.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                SourceChip(
                    label = "All",
                    isSelected = uiState.selectedSource == null,
                    onClick = { viewModel.selectSource(null) }
                )
                uiState.sources.forEach { source ->
                    SourceChip(
                        label = source,
                        isSelected = uiState.selectedSource == source,
                        onClick = { viewModel.selectSource(source) }
                    )
                }
            }
        }

        when {
            uiState.isLoading && uiState.articles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VettrAccent)
                }
            }
            uiState.error != null && uiState.articles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Failed to load news",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = "Tap to retry",
                            style = MaterialTheme.typography.labelMedium,
                            color = VettrAccent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { viewModel.refresh() }
                        )
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        item { Spacer(modifier = Modifier.height(Spacing.xs)) }

                        items(uiState.filteredArticles, key = { it.id }) { article ->
                            NewsArticleRow(
                                article = article,
                                onClick = {
                                    // Open in browser
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                    context.startActivity(intent)
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) VettrAccent else VettrSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Color.White else VettrTextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NewsArticleRow(
    article: NewsArticleResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VettrSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Source + material badge
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = article.source,
                style = MaterialTheme.typography.labelSmall,
                color = VettrAccent,
                fontWeight = FontWeight.SemiBold
            )
            if (article.isMaterial) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(VettrRed.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "MATERIAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = VettrRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = article.publishedAt.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = VettrTextSecondary
            )
        }

        // Title
        Text(
            text = article.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Summary
        article.summary?.let { summary ->
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = VettrTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Tickers
        if (article.tickers.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                article.tickers.take(5).forEach { ticker ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(VettrAccent.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticker,
                            style = MaterialTheme.typography.labelSmall,
                            color = VettrAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
