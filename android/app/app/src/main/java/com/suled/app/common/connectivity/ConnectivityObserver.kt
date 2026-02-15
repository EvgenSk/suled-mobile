package com.suled.app.common.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Interface for observing network connectivity state.
 * Provides a reactive stream of connectivity status changes.
 */
interface ConnectivityObserver {
    /**
     * Observes network connectivity state.
     * Emits [ConnectivityStatus] whenever the network state changes.
     * 
     * @return Flow of connectivity status updates
     */
    fun observe(): Flow<ConnectivityStatus>
    
    /**
     * Gets the current connectivity status synchronously.
     * 
     * @return Current connectivity status
     */
    fun getCurrentStatus(): ConnectivityStatus
}

/**
 * Represents the current network connectivity state.
 */
enum class ConnectivityStatus {
    /** Device has active internet connection */
    AVAILABLE,
    
    /** Device lost internet connection */
    UNAVAILABLE,
    
    /** Currently losing connection */
    LOSING,
    
    /** Connection state is unknown or not yet determined */
    UNKNOWN
}
