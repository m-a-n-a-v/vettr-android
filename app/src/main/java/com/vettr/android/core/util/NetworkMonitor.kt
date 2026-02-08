package com.vettr.android.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network connectivity monitor service.
 * Tracks network availability and provides observable connectivity state.
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        registerNetworkCallback()
    }

    /**
     * Check initial connectivity state.
     */
    private fun checkInitialConnectivity(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Register network callback to monitor connectivity changes.
     */
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                _isOnline.value = hasInternet && isValidated
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    }

    /**
     * Unregister network callback.
     * Should be called when the monitor is no longer needed.
     */
    fun unregister() {
        networkCallback?.let { callback ->
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore if already unregistered
            }
        }
        networkCallback = null
    }

    /**
     * Get a Flow of connectivity state changes.
     * This is a convenience method for observing connectivity changes.
     */
    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(hasInternet && isValidated)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Send initial state
        trySend(checkInitialConnectivity())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    /**
     * Check if currently connected to the internet.
     * This is a one-time check, prefer using isOnline StateFlow for continuous monitoring.
     */
    fun isConnected(): Boolean {
        return checkInitialConnectivity()
    }
}
