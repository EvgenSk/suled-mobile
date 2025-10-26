package com.suled.app.data.api

object ApiConstants {
    // TODO: Update this URL based on your deployment
    // For local development with emulator: http://10.0.2.2:7071/api/
    // For local development with physical device: http://YOUR_IP:7071/api/
    // For Azure deployment: https://your-function-app.azurewebsites.net/api/
    const val BASE_URL = "http://10.0.2.2:7071/api/"
    
    const val PAIRS_ENDPOINT = "pairs"
    const val GAMES_FOR_PAIR_ENDPOINT = "games/pair/{pairId}"
}
