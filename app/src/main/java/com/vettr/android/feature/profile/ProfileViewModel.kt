package com.vettr.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.model.User
import com.vettr.android.core.model.VettrTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Profile screen.
 * Manages user data, tier information, and authentication state.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _tier = MutableStateFlow(VettrTier.FREE)
    val tier: StateFlow<VettrTier> = _tier.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
    }

    /**
     * Load current user data from the AuthRepository.
     */
    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.update { true }
            authRepository.getCurrentUser().collect { currentUser ->
                _user.update { currentUser }
                currentUser?.let {
                    // Parse tier from user data
                    val userTier = try {
                        VettrTier.valueOf(it.tier.uppercase())
                    } catch (e: IllegalArgumentException) {
                        VettrTier.FREE // Default to FREE if tier is invalid
                    }
                    _tier.update { userTier }
                }
                _isLoading.update { false }
            }
        }
    }

    /**
     * Sign out the current user and clear authentication tokens.
     * After logout, the app should navigate to the auth screen.
     */
    fun logout() {
        viewModelScope.launch {
            _isLoading.update { true }
            authRepository.signOut()
            _user.update { null }
            _tier.update { VettrTier.FREE }
            _isLoading.update { false }
        }
    }
}
