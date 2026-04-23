package com.suled.wear

import com.suled.app.data.models.TournamentDetail

/**
 * Interface for syncing data from phone to watch via the Wear OS Data Layer.
 * Abstracting over the concrete implementation makes ViewModels testable.
 */
interface WearSyncService {
    fun syncTrackedPairs()
    fun syncTournamentDetail(tournament: TournamentDetail)
}
