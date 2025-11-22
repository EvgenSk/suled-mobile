package com.suled.app.data.api

import com.suled.app.BuildConfig

object ApiConstants {
    // Automatically switches between debug (local) and release (Azure) URLs
    const val BASE_URL = BuildConfig.API_BASE_URL
    
    const val TOURNAMENTS_ENDPOINT = "tournaments"
    const val TOURNAMENT_DETAIL_ENDPOINT = "tournament/{id}"
    const val PAIRS_ENDPOINT = "pairs"
    const val GAMES_FOR_PAIR_ENDPOINT = "games/pair/{pairId}"
}
