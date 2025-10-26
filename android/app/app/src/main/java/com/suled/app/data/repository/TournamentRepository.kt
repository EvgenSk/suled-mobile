package com.suled.app.data.repository

import com.suled.app.data.api.ApiConstants
import com.suled.app.data.api.TournamentApiService
import com.suled.app.data.models.Game
import com.suled.app.data.models.Pair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TournamentRepository {
    private val apiService: TournamentApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(TournamentApiService::class.java)
    }

    suspend fun getPairs(): Result<List<Pair>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPairs()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.pairs)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGamesForPair(pairId: String): Result<List<Game>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGamesForPair(pairId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.games)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
