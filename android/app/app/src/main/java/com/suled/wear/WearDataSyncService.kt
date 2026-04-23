package com.suled.wear

import android.content.Context
import com.google.android.gms.tasks.Task
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service to sync tracked pairs and tournament data from phone to watch.
 * Uses Wear OS Data Layer API.
 *
 * @param scope Application-lifetime scope; callers must NOT pass a locally-created scope.
 */
class WearDataSyncService(
    private val context: Context,
    private val trackedPairDao: TrackedPairDao,
    private val scope: CoroutineScope
) : WearSyncService {

    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

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

    /** Suspending wrapper around a GMS [Task] — avoids blocking thread with Tasks.await(). */
    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> cont.resume(result) }
        addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    /**
     * Sync all tracked pairs (read from Room DB) to the watch.
     * Call this whenever the tracked pairs list changes.
     */
    override fun syncTrackedPairs() {
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

                dataClient.putDataItem(putDataReq).await()
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync tracked pairs to watch")
            }
        }
    }

    /**
     * Map a [TournamentDetail] (phone API model) to [TournamentData] (watch model)
     * and sync it to the watch via the Data Layer.
     */
    override fun syncTournamentDetail(tournament: TournamentDetail) {
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
                syncTournamentData(tournamentData)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync tournament detail for ${tournament.id}")
            }
        }
    }

    /**
     * Sync a pre-built [TournamentData] to the watch.
     */
    private suspend fun syncTournamentData(tournament: TournamentData) {
        val tournamentJson = json.encodeToString(tournament)
        val path = "$TOURNAMENT_PATH_PREFIX${tournament.id}"

        val putDataReq = PutDataMapRequest.create(path).apply {
            dataMap.putString("tournament", tournamentJson)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        dataClient.putDataItem(putDataReq).await()
    }

    /**
     * Check if a watch node is connected.
     */
    suspend fun isWatchConnected(): Boolean {
        return try {
            Wearable.getNodeClient(context).connectedNodes.await().isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check watch connectivity")
            false
        }
    }
}
