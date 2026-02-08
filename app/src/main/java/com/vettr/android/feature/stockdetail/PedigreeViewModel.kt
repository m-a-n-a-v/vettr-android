package com.vettr.android.feature.stockdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.ExecutiveRepository
import com.vettr.android.core.model.Executive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Pedigree screen.
 * Manages UI state for executive team data with search, sort, and filter functionality.
 */
@HiltViewModel
class PedigreeViewModel @Inject constructor(
    private val executiveRepository: ExecutiveRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _executives = MutableStateFlow<List<Executive>>(emptyList())
    val executives: StateFlow<List<Executive>> = _executives.asStateFlow()

    private val _filteredExecutives = MutableStateFlow<List<Executive>>(emptyList())
    val filteredExecutives: StateFlow<List<Executive>> = _filteredExecutives.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NAME)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filterTitle = MutableStateFlow(FilterTitle.ALL)
    val filterTitle: StateFlow<FilterTitle> = _filterTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Get stockId from SavedStateHandle (passed via navigation)
    private val stockId: String = savedStateHandle.get<String>("stockId") ?: ""

    init {
        if (stockId.isNotEmpty()) {
            loadExecutives()
        }
    }

    /**
     * Load executives for the current stock from repository.
     */
    fun loadExecutives() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                executiveRepository.getExecutivesForStock(stockId)
                    .catch { error ->
                        _errorMessage.value = "Failed to load executives: ${error.message}"
                    }
                    .collect { executiveList ->
                        _executives.value = executiveList
                        applyFiltersAndSort()
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update search query and filter results.
     * @param query Search query string
     */
    fun search(query: String) {
        _searchQuery.value = query
        applyFiltersAndSort()
    }

    /**
     * Update sort option and re-sort results.
     * @param option Sort option (Name, Tenure, or Score)
     */
    fun sort(option: SortOption) {
        _sortOption.value = option
        applyFiltersAndSort()
    }

    /**
     * Update title filter and re-filter results.
     * @param title Filter by executive title (All, CEO, CFO, COO)
     */
    fun filter(title: FilterTitle) {
        _filterTitle.value = title
        applyFiltersAndSort()
    }

    /**
     * Apply current search, filter, and sort settings to executive list.
     */
    private fun applyFiltersAndSort() {
        var filtered = _executives.value

        // Apply title filter
        filtered = when (_filterTitle.value) {
            FilterTitle.ALL -> filtered
            FilterTitle.CEO -> filtered.filter { it.title.contains("CEO", ignoreCase = true) }
            FilterTitle.CFO -> filtered.filter { it.title.contains("CFO", ignoreCase = true) }
            FilterTitle.COO -> filtered.filter { it.title.contains("COO", ignoreCase = true) }
        }

        // Apply search query
        if (_searchQuery.value.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(_searchQuery.value, ignoreCase = true) ||
                it.title.contains(_searchQuery.value, ignoreCase = true) ||
                it.specialization.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        // Apply sort
        filtered = when (_sortOption.value) {
            SortOption.NAME -> filtered.sortedBy { it.name }
            SortOption.TENURE -> filtered.sortedByDescending { it.yearsAtCompany }
            SortOption.SCORE -> {
                // Score based on tenure and experience - higher tenure = higher score
                // Executives with < 1 year are considered risky (lower score)
                filtered.sortedByDescending { executive ->
                    when {
                        executive.yearsAtCompany < 1.0 -> -1.0 // Risky - show last
                        else -> executive.yearsAtCompany
                    }
                }
            }
        }

        _filteredExecutives.value = filtered
    }

    /**
     * Refresh executive data by reloading from repository.
     */
    fun refresh() {
        loadExecutives()
    }
}

/**
 * Sort options for executive list.
 */
enum class SortOption {
    NAME,
    TENURE,
    SCORE
}

/**
 * Filter options for executive titles.
 */
enum class FilterTitle {
    ALL,
    CEO,
    CFO,
    COO
}
