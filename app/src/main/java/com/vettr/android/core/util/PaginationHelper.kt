package com.vettr.android.core.util

/**
 * Utility class for handling pagination in list views.
 * Provides constants and helper functions for implementing lazy loading with pagination.
 */
object PaginationHelper {
    /**
     * Default page size for paginated queries.
     * Balances between too many small requests and loading too much data at once.
     */
    const val PAGE_SIZE = 50

    /**
     * Threshold for triggering the next page load.
     * When the user scrolls to this many items from the end, start loading more.
     */
    const val LOAD_MORE_THRESHOLD = 10

    /**
     * Calculate the offset for a given page number.
     * @param page The page number (0-indexed)
     * @param pageSize The number of items per page (defaults to PAGE_SIZE)
     * @return The offset for the Room OFFSET query
     */
    fun calculateOffset(page: Int, pageSize: Int = PAGE_SIZE): Int {
        return page * pageSize
    }

    /**
     * Determine if more items should be loaded based on the current scroll position.
     * @param lastVisibleItemIndex The index of the last visible item in the list
     * @param totalItemsCount The total number of items currently loaded
     * @param threshold The number of items from the end to trigger loading (defaults to LOAD_MORE_THRESHOLD)
     * @return True if more items should be loaded, false otherwise
     */
    fun shouldLoadMore(
        lastVisibleItemIndex: Int,
        totalItemsCount: Int,
        threshold: Int = LOAD_MORE_THRESHOLD
    ): Boolean {
        return lastVisibleItemIndex >= totalItemsCount - threshold
    }
}

/**
 * Data class representing the state of paginated data.
 */
data class PaginatedState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    val error: String? = null
)
