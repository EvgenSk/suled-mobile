package com.suled.wear

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.suled.app.data.local.dao.TrackedPairDao
import com.suled.app.data.models.TournamentDetail
import com.suled.models.GameData
import com.suled.models.PairData
import com.suled.models.RoundData
import com.suled.models.TrackedPair
import com.suled.models.TournamentData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * Service to sync tracked pairs and tournament data from phone to watch.
 * Uses Wear OS Data Layer API.
 */
class WearDataSyncService(
    private val context: Context,
    private val trackedPairDao: TrackedPairDao
) {

    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TRACKED_PAIRS_PATH = "/suled/tracked_pairs"
        private const val TOURNAMENT_PATH_PREFIX = "/suled/tournament/"

        private val STATUS_MAP = mapOf(
            "scheduled" to 0,
            "inprogress" to 1,
            "completed" to 2,
            "cancelled" to 3
        )
    }

    /**
     * Sync all tracked pairs (read from Room DB) to the watch.
     * Call this whenever the tracked pairs list changes.
     */
    fun syncTrackedPairs() {
        scope.launch {
            try {
                val entities = trackedPairDao.getAllTrackedPairs()
                val pairs = entities.map { entity ->
                    TrackedPair(
                        tournamentId = entity.tournamentId,
                        tournamentName = entity.tournamentName,
                        pairId = entity.pairId,
                        pairDisplayName = entity.pairDisplayName,
                        addedDate = Instant.ofEpochMilli(entity.addedDate).toString()
                    )
                }

                val pairsJson = json.encodeToString(pairs)
                val putDataReq = PutDataMapRequest.create(TRACKED_PAIRS_PATH).apply {
                    dataMap.putString("pairs", pairsJson)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }.asPutDataRequest().setUrgent()

                Tasks.await(dataClient.putDataItem(putDataReq))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Map a [TournamentDetail] (phone API model) to [TournamentData] (watch model)
     * and sync it to the watch via the Data Layer.
     */
    fun syncTournamentDetail(tournament: TournamentDetail) {
        scope.launch {
            try {
                val tournamentData = TournamentData(
                    id = tournament.id,
                    name = tournament.name,
                    startDate = tournament.startDate,
                    rounds = tournament.rounds.map { round ->
                        RoundData(
                            roundNumber = round.roundNumber,
                            startTime = round.startTime,
                            endTime = round.endTime,
                            gameCount = round.gameCount
                        )
                    },
                    pairs = tournament.pairs.map { pair ->
                        val pairIdInt = pair.id.toIntOrNull() ?: pair.id.hashCode()
                        PairData(
                            id = pairIdInt,
                            displayName = pair.displayName,
                            player1Name = pair.displayName,
                            player2Name = "",
                            games = pair.games.map { game ->
                                val opponentIdInt = game.opponentPair.id.toIntOrNull()
                                    ?: game.opponentPair.id.hashCode()
                                val statusInt = STATUS_MAP[game.status.lowercase()] ?: 0
                                GameData(
                                    round = game.round,
                                    courtNumber = game.courtNumber,
                                    opponentId = opponentIdInt,
                                    opponentName = game.opponentPair.displayName,
                                    status = statusInt
                                )
                            }
                        )
                    }
                )
                syncTournament(tournamentData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Sync a pre-built [TournamentData] to the watch.
     */
    fun syncTournament(tournament: TournamentData) {
        scope.launch {
            try {
                val tournamentJson = json.encodeToString(tournament)
                val path = "$TOURNAMENT_PATH_PREFIX${tournament.id}"

                val putDataReq = PutDataMapRequest.create(path).apply {
                    dataMap.putString("tournament", tournamentJson)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }.asPutDataRequest().setUrgent()

                Tasks.await(dataClient.putDataItem(putDataReq))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Check if a watch node is connected.
     */
    suspend fun isWatchConnected(): Boolean {
        return try {
            val nodes = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
            nodes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
