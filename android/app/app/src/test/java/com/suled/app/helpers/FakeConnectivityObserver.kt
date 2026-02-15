package com.suled.app.helpers

import com.suled.app.common.connectivity.ConnectivityObserver
import com.suled.app.common.connectivity.ConnectivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of [ConnectivityObserver] for testing.
 * Returns a fixed connectivity status.
 */
class FakeConnectivityObserver(
    private val status: ConnectivityStatus = ConnectivityStatus.AVAILABLE
) : ConnectivityObserver {
    
    override fun observe(): Flow<ConnectivityStatus> {
        return flowOf(status)
    }
    
    override fun getCurrentStatus(): ConnectivityStatus {
        return status
    }
}
