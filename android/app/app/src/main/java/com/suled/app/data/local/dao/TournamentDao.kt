package com.suled.app.data.local.dao

import androidx.room.*
import com.suled.app.data.local.entity.TournamentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tournaments
 */
@Dao
interface TournamentDao {
    
    @Query("SELECT * FROM tournaments ORDER BY startDate DESC")
    fun observeAllTournaments(): Flow<List<TournamentEntity>>
    
    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournamentById(tournamentId: String): TournamentEntity?
    
    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    fun observeTournamentById(tournamentId: String): Flow<TournamentEntity?>
    
    @Query("SELECT * FROM tournaments WHERE status = :status ORDER BY startDate DESC")
    fun observeTournamentsByStatus(status: String): Flow<List<TournamentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournaments(tournaments: List<TournamentEntity>)
    
    @Update
    suspend fun updateTournament(tournament: TournamentEntity)
    
    @Delete
    suspend fun deleteTournament(tournament: TournamentEntity)
    
    @Query("DELETE FROM tournaments")
    suspend fun deleteAllTournaments()
    
    @Query("DELETE FROM tournaments WHERE lastUpdated < :timestamp")
    suspend fun deleteOldTournaments(timestamp: Long)
}
