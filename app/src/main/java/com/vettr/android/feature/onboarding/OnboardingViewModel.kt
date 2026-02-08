package com.vettr.android.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing onboarding state.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    /**
     * Mark onboarding as completed and store in DataStore.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.markOnboardingCompleted()
        }
    }

    /**
     * Reset onboarding completion (for replaying from Profile > About).
     */
    fun resetOnboarding() {
        viewModelScope.launch {
            onboardingRepository.resetOnboarding()
        }
    }
}
