package com.vettr.android.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for tracking user engagement milestones and triggering Play Store review prompts.
 * Implements a 90-day cooldown between review prompts to avoid spamming users.
 */
@Singleton
class FeedbackService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.feedbackDataStore: DataStore<Preferences> by preferencesDataStore(name = "feedback_preferences")

    private val reviewManager = ReviewManagerFactory.create(context)

    companion object {
        private val WATCHLIST_ADDITIONS_KEY = intPreferencesKey("watchlist_additions_count")
        private val LAST_REVIEW_PROMPT_KEY = longPreferencesKey("last_review_prompt_timestamp")
        private val FEEDBACK_SUBMISSIONS_KEY = intPreferencesKey("feedback_submissions_count")

        private const val WATCHLIST_THRESHOLD = 10
        private const val REVIEW_COOLDOWN_DAYS = 90
        private const val MILLIS_PER_DAY = 86_400_000L
    }

    /**
     * Records a watchlist addition and checks if the user has reached the threshold
     * for triggering a Play Store review prompt.
     *
     * @return True if the review prompt should be shown (threshold reached and cooldown expired)
     */
    suspend fun recordWatchlistAddition(): Boolean {
        val currentCount = incrementWatchlistCount()

        // Check if we've reached the threshold
        if (currentCount >= WATCHLIST_THRESHOLD) {
            return shouldShowReviewPrompt()
        }

        return false
    }

    /**
     * Increments the watchlist addition count and returns the new count.
     */
    private suspend fun incrementWatchlistCount(): Int {
        var newCount = 0
        context.feedbackDataStore.edit { preferences ->
            val currentCount = preferences[WATCHLIST_ADDITIONS_KEY] ?: 0
            newCount = currentCount + 1
            preferences[WATCHLIST_ADDITIONS_KEY] = newCount
        }
        return newCount
    }

    /**
     * Checks if the review prompt should be shown based on cooldown period.
     */
    private suspend fun shouldShowReviewPrompt(): Boolean {
        val lastPromptTime = context.feedbackDataStore.data
            .map { preferences -> preferences[LAST_REVIEW_PROMPT_KEY] ?: 0L }
            .first()

        val currentTime = System.currentTimeMillis()
        val cooldownMillis = REVIEW_COOLDOWN_DAYS * MILLIS_PER_DAY

        return (currentTime - lastPromptTime) >= cooldownMillis
    }

    /**
     * Requests a Play Store review from the user.
     * Updates the last review prompt timestamp regardless of whether the user completes the review.
     *
     * @param activity The activity context for showing the review flow
     */
    suspend fun requestReview(activity: android.app.Activity) {
        // Update the last review prompt timestamp
        context.feedbackDataStore.edit { preferences ->
            preferences[LAST_REVIEW_PROMPT_KEY] = System.currentTimeMillis()
        }

        // Request the review info
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Launch the in-app review flow
                val reviewInfo = task.result
                reviewManager.launchReviewFlow(activity, reviewInfo)
            }
        }
    }

    /**
     * Records a feedback submission for analytics tracking.
     * Returns the total number of feedback submissions.
     */
    suspend fun recordFeedbackSubmission(): Int {
        var newCount = 0
        context.feedbackDataStore.edit { preferences ->
            val currentCount = preferences[FEEDBACK_SUBMISSIONS_KEY] ?: 0
            newCount = currentCount + 1
            preferences[FEEDBACK_SUBMISSIONS_KEY] = newCount
        }
        return newCount
    }

    /**
     * Gets the current watchlist addition count.
     */
    suspend fun getWatchlistAdditionCount(): Int {
        return context.feedbackDataStore.data
            .map { preferences -> preferences[WATCHLIST_ADDITIONS_KEY] ?: 0 }
            .first()
    }

    /**
     * Gets the total number of feedback submissions.
     */
    suspend fun getFeedbackSubmissionCount(): Int {
        return context.feedbackDataStore.data
            .map { preferences -> preferences[FEEDBACK_SUBMISSIONS_KEY] ?: 0 }
            .first()
    }

    /**
     * Resets all feedback tracking data (for testing purposes).
     */
    suspend fun resetFeedbackData() {
        context.feedbackDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
