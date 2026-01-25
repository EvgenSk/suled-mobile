package com.suled.wear

import android.content.Context
import com.google.android.gms.wearable.*
import com.suled.data.createWatchLocalStorageService
import com.suled.models.TrackedPair
import com.suled.models.TournamentData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Wear OS service to receive data from phone
 * Listens for tracked pairs and tournament data updates
 */
class WearDataListenerService : WearableListenerService() {
    
    private val localStorage by lazy {
        createWatchLocalStorageService(this)
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
    }
    
    companion object {
        private const val TRACKED_PAIRS_PATH = "/suled/tracked_pairs"
        private const val TOURNAMENT_PATH_PREFIX = "/suled/tournament/"
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                val path = uri.path
                
                when {
                    path == TRACKED_PAIRS_PATH -> {
                        handleTrackedPairsUpdate(event.dataItem)
                    }
                    path?.startsWith(TOURNAMENT_PATH_PREFIX) == true -> {
                        handleTournamentUpdate(event.dataItem)
                    }
                }
            }
        }
    }
    
    /**
     * Handle tracked pairs update from phone
     */
    private fun handleTrackedPairsUpdate(dataItem: DataItem) {
        try {
            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
            val pairsJson = dataMap.getString("pairs") ?: return
            
            val pairs: List<TrackedPair> = json.decodeFromString(pairsJson)
            
            // Clear existing tracked pairs
            localStorage.clearAllTrackedPairs()
            
            // Save new tracked pairs
            pairs.forEach { pair ->
                localStorage.addTrackedPair(pair)
            }
            
            // Update complications to show new data
            updateComplications()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Handle tournament data update from phone
     */
    private fun handleTournamentUpdate(dataItem: DataItem) {
        try {
            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
            val tournamentJson = dataMap.getString("tournament") ?: return
            
            val tournament: TournamentData = json.decodeFromString(tournamentJson)
            
            // Cache tournament data on watch
            localStorage.cacheTournament(tournament)
            
            // Update complications to reflect new data
            updateComplications()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Request update of all complications showing Suled data
     */
    private fun updateComplications() {
        try {
            // Request update of NextGameComplication
            val componentName = android.content.ComponentName(
                this,
                NextGameComplication::class.java
            )
            
            androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
                .create(this, componentName)
                .requestUpdateAll()
                
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
