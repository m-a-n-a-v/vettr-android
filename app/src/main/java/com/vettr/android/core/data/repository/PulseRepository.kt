package com.vettr.android.core.data.repository

import com.vettr.android.core.model.PulseSummary

/**
 * Repository interface for Pulse summary data operations.
 * Provides abstraction over the pulse summary API endpoint.
 */
interface PulseRepository {
    /**
     * Fetch the pulse summary from the backend.
     * @return PulseSummary or null if the request fails
     */
    suspend fun getPulseSummary(): PulseSummary?
}
