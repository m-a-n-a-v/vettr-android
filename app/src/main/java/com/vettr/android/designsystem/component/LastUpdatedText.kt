package com.vettr.android.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/**
 * Displays a relative timestamp for last updated time.
 * Formats as "Last updated X minutes ago", "Last updated X hours ago", etc.
 *
 * @param lastUpdatedAt Timestamp in milliseconds, or null if never updated
 * @param modifier Modifier for styling
 */
@Composable
fun LastUpdatedText(
    lastUpdatedAt: Long?,
    modifier: Modifier = Modifier
) {
    if (lastUpdatedAt == null) return

    val relativeTime = formatRelativeTime(lastUpdatedAt)
    Text(
        text = "Last updated $relativeTime",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * Format timestamp to relative time string.
 * @param timestamp Timestamp in milliseconds
 * @return Formatted relative time string
 */
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> {
            val minutes = diff / 60_000
            "$minutes minute${if (minutes == 1L) "" else "s"} ago"
        }
        diff < 86400_000 -> {
            val hours = diff / 3600_000
            "$hours hour${if (hours == 1L) "" else "s"} ago"
        }
        diff < 604800_000 -> {
            val days = diff / 86400_000
            "$days day${if (days == 1L) "" else "s"} ago"
        }
        else -> {
            val weeks = diff / 604800_000
            "$weeks week${if (weeks == 1L) "" else "s"} ago"
        }
    }
}
