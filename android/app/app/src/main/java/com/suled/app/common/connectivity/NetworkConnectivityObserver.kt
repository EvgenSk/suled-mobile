package com.suled.app.common.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of [ConnectivityObserver] using Android's ConnectivityManager.
 * Monitors network connectivity changes and exposes them as a Flow.
 * 
 * @property context Application context for accessing system services
 */
class NetworkConnectivityObserver @Inject constructor(
    private val context: Context
) : ConnectivityObserver {
    
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Observes network connectivity changes.
     * Uses NetworkCallback to listen for network state changes.
     * 
     * @return Flow emitting [ConnectivityStatus] on each network state change
     */
    override fun observe(): Flow<ConnectivityStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Timber.d("Network available: $network")
                trySend(ConnectivityStatus.AVAILABLE)
            }
            
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                Timber.d("Network losing: $network, maxMsToLive: $maxMsToLive")
                trySend(ConnectivityStatus.LOSING)
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                Timber.d("Network lost: $network")
                trySend(ConnectivityStatus.UNAVAILABLE)
            }
            
            override fun onUnavailable() {
                super.onUnavailable()
                Timber.d("Network unavailable")
                trySend(ConnectivityStatus.UNAVAILABLE)
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val hasInternet = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val validated = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                if (hasInternet && validated) {
                    Timber.d("Network capabilities changed: has internet and validated")
                    trySend(ConnectivityStatus.AVAILABLE)
                }
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Send initial state
        trySend(getCurrentStatus())
        
        awaitClose {
            Timber.d("Unregistering network callback")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * Gets the current connectivity status by checking active network capabilities.
     * 
     * @return Current [ConnectivityStatus]
     */
    override fun getCurrentStatus(): ConnectivityStatus {
        val network = connectivityManager.activeNetwork
        if (network == null) {
            Timber.d("No active network")
            return ConnectivityStatus.UNAVAILABLE
        }
        
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities == null) {
            Timber.d("No network capabilities")
            return ConnectivityStatus.UNAVAILABLE
        }
        
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        
        return if (hasInternet && validated) {
            Timber.d("Network available with internet and validated")
            ConnectivityStatus.AVAILABLE
        } else {
            Timber.d("Network unavailable: hasInternet=$hasInternet, validated=$validated")
            ConnectivityStatus.UNAVAILABLE
        }
    }
}
