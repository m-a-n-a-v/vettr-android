package com.vettr.android.feature.stockdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.DetectedFlag
import com.vettr.android.core.data.RedFlagDetector
import com.vettr.android.core.data.RedFlagScore
import com.vettr.android.core.data.repository.RedFlagRepository
import com.vettr.android.core.data.repository.RedFlagTrend
import com.vettr.android.core.model.RedFlagHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Red Flag screen showing detected red flags and history.
 * Manages red flag detection, history, and user acknowledgements.
 */
@HiltViewModel
class RedFlagViewModel @Inject constructor(
    private val redFlagRepository: RedFlagRepository,
    private val redFlagDetector: RedFlagDetector,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ticker: String = savedStateHandle.get<String>("ticker") ?: ""

    private val _uiState = MutableStateFlow(RedFlagUiState())
    val uiState: StateFlow<RedFlagUiState> = _uiState.asStateFlow()

    init {
        loadRedFlags()
        loadFlagHistory()
        loadFlagTrend()
    }

    /**
     * Loads current red flags for the stock.
     */
    private fun loadRedFlags() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val flags = redFlagRepository.detectFlagsForStock(ticker)
                val score = redFlagDetector.calculateCompositeScore(flags)
                _uiState.update {
                    it.copy(
                        currentFlags = flags,
                        compositeScore = score,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load red flags"
                    )
                }
            }
        }
    }

    /**
     * Loads red flag history for the stock.
     */
    private fun loadFlagHistory() {
        viewModelScope.launch {
            try {
                redFlagRepository.getFlagHistory(ticker).collect { history ->
                    _uiState.update { it.copy(flagHistory = history.take(10)) }
                }
            } catch (e: Exception) {
                // Silently fail for history - not critical
            }
        }
    }

    /**
     * Loads red flag trend analysis.
     */
    private fun loadFlagTrend() {
        viewModelScope.launch {
            try {
                val trend = redFlagRepository.getFlagTrend(ticker)
                _uiState.update { it.copy(trend = trend) }
            } catch (e: Exception) {
                // Silently fail for trend - not critical
            }
        }
    }

    /**
     * Acknowledges all red flags for the current stock.
     */
    fun acknowledgeAllFlags() {
        viewModelScope.launch {
            try {
                redFlagRepository.acknowledgeAllForStock(ticker)
                // Reload history to reflect acknowledged status
                loadFlagHistory()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to acknowledge flags: ${e.message}")
                }
            }
        }
    }

    /**
     * Acknowledges a specific red flag.
     */
    fun acknowledgeFlag(flagId: String) {
        viewModelScope.launch {
            try {
                redFlagRepository.acknowledgeFlag(flagId)
                // Reload history to reflect acknowledged status
                loadFlagHistory()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to acknowledge flag: ${e.message}")
                }
            }
        }
    }

    /**
     * Toggles the methodology info sheet visibility.
     */
    fun toggleMethodologySheet() {
        _uiState.update { it.copy(showMethodologySheet = !it.showMethodologySheet) }
    }

    /**
     * Toggles expanded state for a specific flag.
     */
    fun toggleFlagExpanded(flagType: String) {
        _uiState.update {
            val expanded = it.expandedFlags.toMutableSet()
            if (expanded.contains(flagType)) {
                expanded.remove(flagType)
            } else {
                expanded.add(flagType)
            }
            it.copy(expandedFlags = expanded)
        }
    }
}

/**
 * UI state for Red Flag screen.
 */
@androidx.compose.runtime.Immutable
data class RedFlagUiState(
    val currentFlags: List<DetectedFlag> = emptyList(),
    val compositeScore: RedFlagScore? = null,
    val flagHistory: List<RedFlagHistory> = emptyList(),
    val trend: RedFlagTrend? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showMethodologySheet: Boolean = false,
    val expandedFlags: Set<String> = emptySet()
)
