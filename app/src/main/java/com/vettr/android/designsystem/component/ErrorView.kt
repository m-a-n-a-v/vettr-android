package com.vettr.android.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.core.util.AppError
import com.vettr.android.core.util.SuggestedAction
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Displays an error message with a retry button.
 * Legacy overload for backward compatibility.
 *
 * @param message The error message to display
 * @param onRetry Callback invoked when the Retry button is clicked
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = VettrRed,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VettrAccent,
                    contentColor = Color.White
                )
            ) {
                Text("Retry")
            }
        }
    }
}

/**
 * Displays an error state with an icon, message, and action button based on AppError.
 *
 * @param error The AppError to display
 * @param onRetry Callback invoked when the retry button is clicked (for Retry action)
 * @param onGoHome Callback invoked when the go home button is clicked (for GoHome action)
 * @param onContactSupport Callback invoked when the contact support button is clicked (for ContactSupport action)
 * @param modifier Modifier to be applied to the error view
 */
@Composable
fun ErrorView(
    error: AppError,
    onRetry: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.padding(Spacing.lg)
        ) {
            // Error icon
            Icon(
                imageVector = error.icon,
                contentDescription = "Error icon",
                tint = VettrRed,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = Spacing.sm)
            )

            // Error message
            Text(
                text = error.userMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Action button based on suggested action
            when (error.suggestedAction) {
                SuggestedAction.Retry -> {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VettrAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Retry")
                    }
                }
                SuggestedAction.GoHome -> {
                    Button(
                        onClick = onGoHome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VettrAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Go Home")
                    }
                }
                SuggestedAction.ContactSupport -> {
                    Button(
                        onClick = onContactSupport,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VettrAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Contact Support")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ErrorViewPreview() {
    VettrTheme {
        ErrorView(
            message = "Failed to load data. Please check your connection.",
            onRetry = { }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ErrorViewNetworkPreview() {
    VettrTheme {
        ErrorView(
            error = AppError.NetworkError()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ErrorViewNotFoundPreview() {
    VettrTheme {
        ErrorView(
            error = AppError.NotFound()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ErrorViewUnauthorizedPreview() {
    VettrTheme {
        ErrorView(
            error = AppError.Unauthorized()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ErrorViewServerErrorPreview() {
    VettrTheme {
        ErrorView(
            error = AppError.ServerError()
        )
    }
}
