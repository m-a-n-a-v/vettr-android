package com.vettr.android.feature.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.vettr.android.core.data.remote.RegisterDeviceRequest
import com.vettr.android.core.data.remote.VettrApi
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.util.notification.VettrFirebaseMessagingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication screen.
 * Manages auth state and handles sign-in, sign-up, and Google auth operations.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val vettrApi: VettrApi,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observe authentication state from repository
        viewModelScope.launch {
            authRepository.isAuthenticated().collect { authenticated ->
                _uiState.update { it.copy(isAuthenticated = authenticated) }
            }
        }
    }

    /**
     * Sign in with email and password.
     */
    fun signInWithEmail() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email and password cannot be empty")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signInWithEmail(email, password)

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            errorMessage = null
                        )
                    }
                    registerFcmToken()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Sign in failed"
                        )
                    }
                }
            )
        }
    }

    /**
     * Sign in with Google using ID token.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signInWithGoogle(idToken)

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            errorMessage = null
                        )
                    }
                    registerFcmToken()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Google sign in failed"
                        )
                    }
                }
            )
        }
    }

    /**
     * Sign up with email and password.
     */
    fun signUp() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email and password cannot be empty")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signUp(email, password)

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            errorMessage = null
                        )
                    }
                    registerFcmToken()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Sign up failed"
                        )
                    }
                }
            )
        }
    }

    /**
     * Update email field in UI state.
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    /**
     * Update password field in UI state.
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Register the FCM token with the backend after successful authentication.
     * First checks for a pending token in SharedPreferences (set by VettrFirebaseMessagingService),
     * then falls back to requesting the current token from Firebase.
     * Non-blocking: failures are silently ignored.
     */
    private fun registerFcmToken() {
        viewModelScope.launch {
            try {
                // Check for pending token first (set by onNewToken callback)
                val pendingToken = VettrFirebaseMessagingService.getPendingFcmToken(application)
                if (pendingToken != null) {
                    vettrApi.registerDevice(RegisterDeviceRequest(token = pendingToken))
                    VettrFirebaseMessagingService.clearPendingFcmToken(application)
                    return@launch
                }

                // Otherwise request current token from Firebase
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    viewModelScope.launch {
                        try {
                            vettrApi.registerDevice(RegisterDeviceRequest(token = token))
                        } catch (_: Exception) {
                            // Non-blocking: FCM registration failure should not affect user experience
                        }
                    }
                }
            } catch (_: Exception) {
                // Non-blocking: FCM registration failure should not affect user experience
            }
        }
    }
}

/**
 * UI state for authentication screen.
 */
@androidx.compose.runtime.Immutable
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)
