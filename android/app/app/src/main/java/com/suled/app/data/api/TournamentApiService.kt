package com.suled.app.data.api

import com.suled.app.data.models.GamesResponse
import com.suled.app.data.models.PairsResponse
import com.suled.app.data.models.TournamentsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TournamentApiService {
    @GET(ApiConstants.TOURNAMENTS_ENDPOINT)
    suspend fun getTournaments(
        @Query("startDateFrom") startDateFrom: String? = null,
        @Query("startDateTo") startDateTo: String? = null,
        @Query("location") location: String? = null,
        @Query("division") division: String? = null,
        @Query("status") status: String? = null,
        @Query("maxResults") maxResults: Int? = 100
    ): Response<TournamentsResponse>
    
    @GET(ApiConstants.PAIRS_ENDPOINT)
    suspend fun getPairs(): Response<PairsResponse>
    
    @GET(ApiConstants.GAMES_FOR_PAIR_ENDPOINT)
    suspend fun getGamesForPair(
        @Path("pairId") pairId: String
    ): Response<GamesResponse>
}
