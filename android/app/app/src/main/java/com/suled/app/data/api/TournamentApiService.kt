package com.suled.app.data.api

import com.suled.app.data.models.GamesResponse
import com.suled.app.data.models.PairsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TournamentApiService {
    @GET(ApiConstants.PAIRS_ENDPOINT)
    suspend fun getPairs(): Response<PairsResponse>
    
    @GET(ApiConstants.GAMES_FOR_PAIR_ENDPOINT)
    suspend fun getGamesForPair(
        @Path("pairId") pairId: String
    ): Response<GamesResponse>
}
