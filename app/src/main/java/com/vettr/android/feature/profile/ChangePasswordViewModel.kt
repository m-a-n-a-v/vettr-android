package com.vettr.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Change Password screen.
 */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.NONE
)

/**
 * Password strength levels with associated progress and descriptions.
 */
enum class PasswordStrength(val progress: Float, val label: String) {
    NONE(0f, ""),
    WEAK(0.25f, "Weak"),
    FAIR(0.5f, "Fair"),
    GOOD(0.75f, "Good"),
    STRONG(1f, "Strong")
}

/**
 * ViewModel for the Change Password screen.
 * Handles password validation, strength calculation, and API calls.
 */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentPasswordChange(password: String) {
        _uiState.update {
            it.copy(currentPassword = password, errorMessage = null)
        }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                newPassword = password,
                errorMessage = null,
                passwordStrength = calculatePasswordStrength(password)
            )
        }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update {
            it.copy(confirmPassword = password, errorMessage = null)
        }
    }

    /**
     * Calculate password strength based on:
     * - Length (8+ chars)
     * - Has uppercase letters
     * - Has lowercase letters
     * - Has numbers
     * - Has special characters
     */
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE

        var score = 0

        // Length check
        if (password.length >= 8) score++
        if (password.length >= 12) score++

        // Character variety
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 3 -> PasswordStrength.FAIR
            score <= 4 -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }
    }

    /**
     * Validate inputs and submit password change request.
     */
    fun changePassword() {
        val state = _uiState.value

        // Validation
        if (state.currentPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your current password") }
            return
        }

        if (state.newPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a new password") }
            return
        }

        if (state.newPassword.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters") }
            return
        }

        if (!state.newPassword.any { it.isUpperCase() }) {
            _uiState.update { it.copy(errorMessage = "Password must contain an uppercase letter") }
            return
        }

        if (!state.newPassword.any { it.isLowerCase() }) {
            _uiState.update { it.copy(errorMessage = "Password must contain a lowercase letter") }
            return
        }

        if (!state.newPassword.any { it.isDigit() }) {
            _uiState.update { it.copy(errorMessage = "Password must contain a number") }
            return
        }

        if (!state.newPassword.any { !it.isLetterOrDigit() }) {
            _uiState.update { it.copy(errorMessage = "Password must contain a special character") }
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        if (state.currentPassword == state.newPassword) {
            _uiState.update { it.copy(errorMessage = "New password must be different from current password") }
            return
        }

        // Submit
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.changePassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = true)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to change password"
                        )
                    }
                }
            )
        }
    }
}
