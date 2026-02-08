package com.vettr.android.core.util

import android.view.HapticFeedbackConstants
import android.view.View
import com.vettr.android.core.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for providing haptic feedback throughout the app.
 * Respects user preferences for haptic feedback from SettingsRepository.
 *
 * Usage in Compose:
 * ```
 * val view = LocalView.current
 * val hapticService = hiltViewModel<YourViewModel>().hapticService
 * LaunchedEffect(Unit) {
 *     hapticService.medium(view)
 * }
 * ```
 *
 * Feedback types:
 * - light: For simple taps, selections, and UI interactions
 * - medium: For successful actions, confirmations
 * - heavy: For critical actions like delete, remove
 * - error: For errors, rejections, and failed actions
 */
@Singleton
class HapticService @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Performs light haptic feedback (simple tap).
     * Use for: taps, selections, toggles, navigation.
     */
    suspend fun light(view: View?) {
        if (view != null && isHapticEnabled()) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    /**
     * Performs medium haptic feedback (success/confirmation).
     * Use for: successful actions, favorites added, alerts created.
     */
    suspend fun medium(view: View?) {
        if (view != null && isHapticEnabled()) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    /**
     * Performs heavy haptic feedback (critical action).
     * Use for: delete, remove, destructive actions.
     */
    suspend fun heavy(view: View?) {
        if (view != null && isHapticEnabled()) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Performs error haptic feedback (rejection/failure).
     * Use for: errors, rejections, failed validations.
     */
    suspend fun error(view: View?) {
        if (view != null && isHapticEnabled()) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        }
    }

    /**
     * Check if haptic feedback is enabled in user settings.
     */
    private suspend fun isHapticEnabled(): Boolean {
        return settingsRepository.hapticFeedbackEnabled.first()
    }
}
