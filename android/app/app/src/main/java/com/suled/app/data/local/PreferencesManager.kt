package com.suled.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension to create DataStore instance
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "suled_preferences")

/**
 * DataStore-based preferences manager
 * Replaces SharedPreferences with modern, type-safe, coroutine-based storage
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        private val FILTER_OUR_GAMES = booleanPreferencesKey("filter_our_games")
        private val SELECTED_TOURNAMENT_ID = stringPreferencesKey("selected_tournament_id")
        private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }
    
    /**
     * Filter "Our Games" preference
     */
    val filterOurGamesFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FILTER_OUR_GAMES] ?: false
        }
    
    suspend fun setFilterOurGames(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FILTER_OUR_GAMES] = enabled
        }
    }
    
    /**
     * Selected tournament ID
     */
    val selectedTournamentIdFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SELECTED_TOURNAMENT_ID]
        }
    
    suspend fun setSelectedTournamentId(tournamentId: String?) {
        dataStore.edit { preferences ->
            if (tournamentId != null) {
                preferences[SELECTED_TOURNAMENT_ID] = tournamentId
            } else {
                preferences.remove(SELECTED_TOURNAMENT_ID)
            }
        }
    }
    
    /**
     * Last sync timestamp
     */
    val lastSyncTimestampFlow: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] ?: 0L
        }
    
    suspend fun updateLastSyncTimestamp() {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
        }
    }
    
    /**
     * Clear all preferences
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
