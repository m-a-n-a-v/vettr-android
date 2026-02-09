package com.vettr.android.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.local.StockDao
import com.vettr.android.core.data.local.SyncHistoryDao
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.data.repository.StockRepository
import com.vettr.android.core.model.User
import com.vettr.android.core.model.VettrTier
import com.vettr.android.core.sync.SyncManager
import com.vettr.android.core.util.VersionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Profile screen.
 * Manages user data, tier information, stock counts, sync state, and authentication.
 * All data is sourced from live repositories -- no hardcoded values.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val stockRepository: StockRepository,
    private val stockDao: StockDao,
    private val syncManager: SyncManager,
    private val syncHistoryDao: SyncHistoryDao,
    private val versionManager: VersionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _tier = MutableStateFlow(VettrTier.FREE)
    val tier: StateFlow<VettrTier> = _tier.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _stockCount = MutableStateFlow(0)
    val stockCount: StateFlow<Int> = _stockCount.asStateFlow()

    private val _favoritesCount = MutableStateFlow(0)
    val favoritesCount: StateFlow<Int> = _favoritesCount.asStateFlow()

    private val _cacheCleared = MutableStateFlow(false)
    val cacheCleared: StateFlow<Boolean> = _cacheCleared.asStateFlow()

    init {
        loadUserData()
        loadSyncHistory()
        loadStockCounts()
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
     * Load sync history to display last sync time.
     */
    private fun loadSyncHistory() {
        viewModelScope.launch {
            syncHistoryDao.getRecent(1).collect { syncHistory ->
                if (syncHistory.isNotEmpty()) {
                    val lastSync = syncHistory.first()
                    // Check if sync is in progress
                    _isSyncing.update { lastSync.status == "in_progress" }
                    // Set last completed sync time
                    if (lastSync.status == "success" && lastSync.completedAt != null) {
                        _lastSyncTime.update { lastSync.completedAt }
                    }
                }
            }
        }
    }

    /**
     * Load live stock counts from the Room database.
     * Observes stocks flow for total count, and favorites flow for favorites count.
     */
    private fun loadStockCounts() {
        // Observe total stock count from Room
        viewModelScope.launch {
            stockRepository.getStocks().collectLatest { stocks ->
                _stockCount.update { stocks.size }
            }
        }

        // Observe favorites count from Room
        viewModelScope.launch {
            stockRepository.getFavorites().collectLatest { favorites ->
                _favoritesCount.update { favorites.size }
            }
        }
    }

    /**
     * Trigger an immediate manual sync.
     * Updates sync status and refreshes stock data from the live API.
     */
    fun triggerManualSync() {
        viewModelScope.launch {
            _isSyncing.update { true }
            syncManager.triggerImmediateSync()
            // The sync worker will update sync history,
            // and our loadSyncHistory observer will pick it up.
            // Give a small delay then reload
            kotlinx.coroutines.delay(2000)
            _isSyncing.update { false }
            _lastSyncTime.update { System.currentTimeMillis() }
        }
    }

    /**
     * Clear the app's cache directory.
     * Removes image cache and temporary files. Does NOT clear Room database.
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                context.cacheDir.deleteRecursively()
                _cacheCleared.update { true }
                // Reset flag after showing confirmation
                kotlinx.coroutines.delay(2000)
                _cacheCleared.update { false }
            } catch (e: Exception) {
                // Silently fail -- cache clear is non-critical
            }
        }
    }

    /**
     * Calculate next sync ETA based on user tier and last sync time.
     * Returns null if no sync history exists.
     */
    fun getNextSyncEta(): Long? {
        val lastSync = _lastSyncTime.value ?: return null
        val syncIntervalMillis = _tier.value.syncIntervalHours * 60 * 60 * 1000L
        return lastSync + syncIntervalMillis
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

    /**
     * Get formatted app version (e.g., "1.0.0 (1)").
     */
    fun getAppVersion(): String {
        return versionManager.getFormattedVersion()
    }

    /**
     * Get build type (Debug or Release).
     */
    fun getBuildType(): String {
        return versionManager.getBuildType()
    }
}
