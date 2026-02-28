package com.vettr.android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.PrimaryButton
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrTextSecondary

/**
 * Forgot Password screen allowing users to request a password reset link.
 * Shows email input form or success state with instructions.
 */
@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        if (uiState.isSuccess) {
            // Success state
            SuccessContent(onBackToLogin = onBackToLogin)
        } else {
            // Form state
            FormContent(
                email = uiState.email,
                onEmailChange = { viewModel.onEmailChange(it) },
                onSendResetLink = { viewModel.sendResetLink() },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onBackToLogin = onBackToLogin
            )
        }
    }
}

@Composable
private fun FormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendResetLink: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Icon
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = VettrAccent,
            modifier = Modifier.size(48.dp)
        )

        // Title
        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Enter your email address and we'll send you a link to reset your password.",
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            placeholder = { Text("your@email.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VettrAccent,
                unfocusedBorderColor = VettrTextSecondary.copy(alpha = 0.3f),
                focusedLabelColor = VettrAccent,
                cursorColor = VettrAccent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Send Reset Link button
        PrimaryButton(
            text = "Send Reset Link",
            onClick = onSendResetLink,
            isLoading = isLoading,
            fullWidth = true
        )

        // Back to Login
        TextButton(onClick = onBackToLogin) {
            Text(
                text = "Back to Login",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrAccent
            )
        }
    }
}

@Composable
private fun SuccessContent(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = VettrGreen,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "Check Your Email",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "We've sent a password reset link to your email address. Please check your inbox and follow the instructions to reset your password.",
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextSecondary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "If you don't see the email, check your spam folder.",
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Back to Login button
        PrimaryButton(
            text = "Back to Login",
            onClick = onBackToLogin,
            fullWidth = true
        )
    }
}
