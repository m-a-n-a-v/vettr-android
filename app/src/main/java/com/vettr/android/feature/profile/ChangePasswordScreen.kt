package com.vettr.android.feature.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vettr.android.designsystem.component.PrimaryButton
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrTextPrimary
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Change Password screen allowing users to update their password.
 * Features:
 * - Current password field
 * - New password field with strength indicator (LinearProgressIndicator)
 * - Confirm new password field
 * - All fields have password visibility toggles
 * - Validation: 8+ chars, mixed case, numbers, special char
 * - Success: Snackbar + navigate back
 * - Error: Snackbar with specific error
 */
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle success - show snackbar and navigate back
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Password changed successfully")
            onBackClick()
        }
    }

    // Handle error - show snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ChangePasswordScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onChangePassword = viewModel::changePassword,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordScreenContent(
    uiState: ChangePasswordUiState,
    snackbarHostState: SnackbarHostState,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        containerColor = VettrNavy
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "Enter your current password and choose a new one. Your new password must be at least 8 characters with uppercase, lowercase, numbers, and special characters.",
                style = MaterialTheme.typography.bodyMedium,
                color = VettrTextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Current password field
            PasswordField(
                value = uiState.currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = "Current Password",
                passwordVisible = currentPasswordVisible,
                onToggleVisibility = { currentPasswordVisible = !currentPasswordVisible }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // New password field
            PasswordField(
                value = uiState.newPassword,
                onValueChange = onNewPasswordChange,
                label = "New Password",
                passwordVisible = newPasswordVisible,
                onToggleVisibility = { newPasswordVisible = !newPasswordVisible }
            )

            // Password strength indicator
            if (uiState.newPassword.isNotEmpty()) {
                PasswordStrengthIndicator(strength = uiState.passwordStrength)
            }

            // Confirm password field
            PasswordField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm New Password",
                passwordVisible = confirmPasswordVisible,
                onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            // Password mismatch warning
            if (uiState.confirmPassword.isNotEmpty() && uiState.newPassword != uiState.confirmPassword) {
                Text(
                    text = "Passwords do not match",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Error message
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Change password button
            PrimaryButton(
                text = "Change Password",
                onClick = onChangePassword,
                isLoading = uiState.isLoading,
                enabled = uiState.currentPassword.isNotBlank() &&
                        uiState.newPassword.isNotBlank() &&
                        uiState.confirmPassword.isNotBlank() &&
                        !uiState.isLoading,
                fullWidth = true,
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Password requirements
            PasswordRequirements(password = uiState.newPassword)
        }
    }
}

/**
 * Reusable password text field with visibility toggle.
 */
@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = VettrTextSecondary
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VettrAccent,
            unfocusedBorderColor = VettrTextSecondary.copy(alpha = 0.5f),
            focusedLabelColor = VettrAccent,
            unfocusedLabelColor = VettrTextSecondary,
            focusedTextColor = VettrTextPrimary,
            unfocusedTextColor = VettrTextPrimary,
            cursorColor = VettrAccent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Password strength indicator using LinearProgressIndicator with color-coded strength levels.
 */
@Composable
private fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val strengthColor by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.NONE -> VettrTextSecondary.copy(alpha = 0.3f)
            PasswordStrength.WEAK -> Color(0xFFFF4444)
            PasswordStrength.FAIR -> Color(0xFFFF8800)
            PasswordStrength.GOOD -> Color(0xFFFFBB00)
            PasswordStrength.STRONG -> Color(0xFF00C853)
        },
        label = "strengthColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LinearProgressIndicator(
            progress = { strength.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = strengthColor,
            trackColor = VettrTextSecondary.copy(alpha = 0.2f)
        )

        if (strength != PasswordStrength.NONE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Password Strength",
                    style = MaterialTheme.typography.bodySmall,
                    color = VettrTextSecondary
                )
                Text(
                    text = strength.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = strengthColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Password requirements checklist showing which criteria are met.
 */
@Composable
private fun PasswordRequirements(
    password: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(
            text = "Password Requirements",
            style = MaterialTheme.typography.titleSmall,
            color = VettrTextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        RequirementRow(
            text = "At least 8 characters",
            isMet = password.length >= 8
        )
        RequirementRow(
            text = "Contains uppercase letter",
            isMet = password.any { it.isUpperCase() }
        )
        RequirementRow(
            text = "Contains lowercase letter",
            isMet = password.any { it.isLowerCase() }
        )
        RequirementRow(
            text = "Contains a number",
            isMet = password.any { it.isDigit() }
        )
        RequirementRow(
            text = "Contains a special character",
            isMet = password.any { !it.isLetterOrDigit() }
        )
    }
}

/**
 * Individual requirement row with check/cross indicator.
 */
@Composable
private fun RequirementRow(
    text: String,
    isMet: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isMet) Color(0xFF00C853) else VettrTextSecondary.copy(alpha = 0.6f)
    val indicator = if (isMet) "+" else "-"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = indicator,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ChangePasswordScreenPreview() {
    VettrTheme {
        ChangePasswordScreenContent(
            uiState = ChangePasswordUiState(
                newPassword = "MyPass1",
                passwordStrength = PasswordStrength.FAIR
            ),
            snackbarHostState = SnackbarHostState(),
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onChangePassword = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFF0D1B2A, widthDp = 840)
@Composable
fun ChangePasswordScreenTabletPreview() {
    VettrTheme {
        ChangePasswordScreenContent(
            uiState = ChangePasswordUiState(
                currentPassword = "oldpassword",
                newPassword = "NewStr0ng!Pass",
                confirmPassword = "NewStr0ng!Pass",
                passwordStrength = PasswordStrength.STRONG
            ),
            snackbarHostState = SnackbarHostState(),
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onChangePassword = {},
            onBackClick = {}
        )
    }
}
