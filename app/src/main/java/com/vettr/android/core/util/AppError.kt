package com.vettr.android.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing different types of errors that can occur in the app.
 * Each error type provides a user-friendly message, icon, and suggested action.
 */
sealed class AppError(
    open val userMessage: String,
    open val icon: ImageVector,
    open val suggestedAction: SuggestedAction
) {
    data class NetworkError(
        override val userMessage: String = "Unable to connect to the network. Please check your connection and try again.",
        override val icon: ImageVector = Icons.Default.CloudOff,
        override val suggestedAction: SuggestedAction = SuggestedAction.Retry
    ) : AppError(userMessage, icon, suggestedAction)

    data class InvalidData(
        override val userMessage: String = "The data received is invalid. Please try again later.",
        override val icon: ImageVector = Icons.Default.Warning,
        override val suggestedAction: SuggestedAction = SuggestedAction.Retry
    ) : AppError(userMessage, icon, suggestedAction)

    data class NotFound(
        override val userMessage: String = "The requested information could not be found.",
        override val icon: ImageVector = Icons.Default.SearchOff,
        override val suggestedAction: SuggestedAction = SuggestedAction.GoHome
    ) : AppError(userMessage, icon, suggestedAction)

    data class Unauthorized(
        override val userMessage: String = "You are not authorized to access this resource. Please sign in again.",
        override val icon: ImageVector = Icons.Default.Lock,
        override val suggestedAction: SuggestedAction = SuggestedAction.ContactSupport
    ) : AppError(userMessage, icon, suggestedAction)

    data class ServerError(
        override val userMessage: String = "A server error occurred. Our team has been notified. Please try again later.",
        override val icon: ImageVector = Icons.Default.ErrorOutline,
        override val suggestedAction: SuggestedAction = SuggestedAction.Retry
    ) : AppError(userMessage, icon, suggestedAction)

    data class Unknown(
        override val userMessage: String = "An unexpected error occurred. Please try again.",
        override val icon: ImageVector = Icons.Default.ErrorOutline,
        override val suggestedAction: SuggestedAction = SuggestedAction.Retry
    ) : AppError(userMessage, icon, suggestedAction)
}

/**
 * Suggested actions for the user to take when an error occurs.
 */
enum class SuggestedAction {
    Retry,
    GoHome,
    ContactSupport
}
