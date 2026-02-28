package com.vettr.android.feature.filings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.FilingRepository
import com.vettr.android.core.model.Filing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilingCalendarViewModel @Inject constructor(
    private val filingRepository: FilingRepository
) : ViewModel() {

    private val _filings = MutableStateFlow<List<Filing>>(emptyList())
    val filings: StateFlow<List<Filing>> = _filings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFilings()
    }

    private fun loadFilings() {
        viewModelScope.launch {
            filingRepository.getLatestFilings(limit = 100)
                .catch { _isLoading.value = false }
                .collect { list ->
                    _filings.value = list
                    _isLoading.value = false
                }
        }
    }
}
