package com.suled.app.data.api

import com.suled.app.BuildConfig

object ApiConstants {
    // Automatically switches between debug (local) and release (Azure) URLs
    const val BASE_URL = BuildConfig.API_BASE_URL
    
    const val PAIRS_ENDPOINT = "pairs"
    const val GAMES_FOR_PAIR_ENDPOINT = "games/pair/{pairId}"
}
