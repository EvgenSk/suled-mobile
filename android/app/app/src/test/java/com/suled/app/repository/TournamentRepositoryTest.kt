package com.suled.app.repository

import com.suled.app.data.api.TournamentApiService
import com.suled.app.data.repository.TournamentRepository
import com.suled.app.helpers.CoroutineTestRule
import com.suled.app.helpers.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class TournamentRepositoryTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: TournamentRepository
    private lateinit var apiService: TournamentApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TournamentApiService::class.java)

        repository = TournamentRepository(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // ========== getPairs() Tests ==========

    @Test
    fun `getPairs returns parsed pairs from API on success`() = runTest {
        // Given
        val mockPairs = TestData.createPairs(3)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.pairsResponse(mockPairs))
        )

        // When
        val result = repository.getPairs()

        // Then
        assertTrue(result.isSuccess)
        val pairs = result.getOrNull()
        assertNotNull(pairs)
        assertEquals(3, pairs!!.size)
        assertEquals(mockPairs[0].id, pairs[0].id)
        assertEquals(mockPairs[0].displayName, pairs[0].displayName)
    }

    @Test
    fun `getPairs returns empty list when API returns empty pairs`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.pairsResponse(emptyList()))
        )

        // When
        val result = repository.getPairs()

        // Then
        assertTrue(result.isSuccess)
        val pairs = result.getOrNull()
        assertNotNull(pairs)
        assertTrue(pairs!!.isEmpty())
    }

    @Test
    fun `getPairs returns failure on 404 error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody(TestData.Json.errorResponse)
        )

        // When
        val result = repository.getPairs()

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("404") == true)
    }

    @Test
    fun `getPairs returns failure on 500 error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(TestData.Json.errorResponse)
        )

        // When
        val result = repository.getPairs()

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("500") == true)
    }

    @Test
    fun `getPairs returns failure on network error`() = runTest {
        // Given - Shutdown server to simulate network error
        mockWebServer.shutdown()

        // When
        val result = repository.getPairs()

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `getPairs makes correct API request`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.pairsResponse())
        )

        // When
        repository.getPairs()

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("pairs") == true)
    }

    // ========== getGamesForPair() Tests ==========

    @Test
    fun `getGamesForPair returns parsed games from API on success`() = runTest {
        // Given
        val pairId = "pair-1"
        val mockGames = TestData.createGames(5)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.gamesResponse(pairId, mockGames))
        )

        // When
        val result = repository.getGamesForPair(pairId)

        // Then
        assertTrue(result.isSuccess)
        val games = result.getOrNull()
        assertNotNull(games)
        assertEquals(5, games!!.size)
        assertEquals(mockGames[0].id, games[0].id)
        assertEquals(mockGames[0].courtNumber, games[0].courtNumber)
    }

    @Test
    fun `getGamesForPair returns empty list when no games`() = runTest {
        // Given
        val pairId = "pair-1"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.gamesResponse(pairId, emptyList()))
        )

        // When
        val result = repository.getGamesForPair(pairId)

        // Then
        assertTrue(result.isSuccess)
        val games = result.getOrNull()
        assertNotNull(games)
        assertTrue(games!!.isEmpty())
    }

    @Test
    fun `getGamesForPair returns failure on 404 error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody(TestData.Json.errorResponse)
        )

        // When
        val result = repository.getGamesForPair("pair-1")

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("404") == true)
    }

    @Test
    fun `getGamesForPair returns failure on network error`() = runTest {
        // Given
        mockWebServer.shutdown()

        // When
        val result = repository.getGamesForPair("pair-1")

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `getGamesForPair makes correct API request with pairId`() = runTest {
        // Given
        val pairId = "pair-123"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.gamesResponse(pairId))
        )

        // When
        repository.getGamesForPair(pairId)

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("games/pair/$pairId") == true)
    }

    @Test
    fun `getGamesForPair correctly parses isOurGame field`() = runTest {
        // Given
        val pairId = "pair-1"
        val mockGames = TestData.createGames(10, ourGames = 3)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.gamesResponse(pairId, mockGames))
        )

        // When
        val result = repository.getGamesForPair(pairId)

        // Then
        assertTrue(result.isSuccess)
        val games = result.getOrNull()!!
        assertEquals(3, games.count { it.isOurGame })
        assertEquals(7, games.count { !it.isOurGame })
    }

    @Test
    fun `getGamesForPair handles null scheduledTime`() = runTest {
        // Given
        val pairId = "pair-1"
        val gameWithNullTime = TestData.createGame(scheduledTime = null)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.gamesResponse(pairId, listOf(gameWithNullTime)))
        )

        // When
        val result = repository.getGamesForPair(pairId)

        // Then
        assertTrue(result.isSuccess)
        val games = result.getOrNull()!!
        assertEquals(1, games.size)
        assertEquals(null, games[0].scheduledTime)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `repository handles malformed JSON gracefully`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("{ invalid json }")
        )

        // When
        val result = repository.getPairs()

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `repository handles timeout errors`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.pairsResponse())
                .setBodyDelay(5, TimeUnit.SECONDS) // Longer than timeout
        )

        // When
        val result = repository.getPairs()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `repository handles multiple sequential requests`() = runTest {
        // Given
        val pairs = TestData.createPairs(2)
        val games = TestData.createGames(3)
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.pairsResponse(pairs))
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.gamesResponse("pair-1", games))
        )

        // When
        val pairsResult = repository.getPairs()
        val gamesResult = repository.getGamesForPair("pair-1")

        // Then
        assertTrue(pairsResult.isSuccess)
        assertTrue(gamesResult.isSuccess)
        assertEquals(2, pairsResult.getOrNull()?.size)
        assertEquals(3, gamesResult.getOrNull()?.size)
    }

    // ========== getTournaments() Tests ==========

    @Test
    fun `getTournaments returns parsed tournaments from API on success`() = runTest {
        // Given
        val mockTournaments = TestData.createTournaments(3)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.tournamentsResponse(mockTournaments))
        )

        // When
        val result = repository.getTournaments()

        // Then
        assertTrue(result.isSuccess)
        val tournaments = result.getOrNull()
        assertNotNull(tournaments)
        assertEquals(3, tournaments!!.size)
        assertEquals(mockTournaments[0].id, tournaments[0].id)
        assertEquals(mockTournaments[0].name, tournaments[0].name)
    }

    @Test
    fun `getTournaments returns empty list when API returns empty tournaments`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.tournamentsResponse(emptyList()))
        )

        // When
        val result = repository.getTournaments()

        // Then
        assertTrue(result.isSuccess)
        val tournaments = result.getOrNull()
        assertNotNull(tournaments)
        assertTrue(tournaments!!.isEmpty())
    }

    @Test
    fun `getTournaments returns failure on 404 error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody(TestData.Json.errorResponse)
        )

        // When
        val result = repository.getTournaments()

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("404") == true)
    }

    @Test
    fun `getTournaments returns failure on 500 error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(TestData.Json.errorResponse)
        )

        // When
        val result = repository.getTournaments()

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("500") == true)
    }

    @Test
    fun `getTournaments makes correct API request with query parameters`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.tournamentsResponse())
        )

        // When
        repository.getTournaments(
            startDateFrom = "2025-06-01",
            startDateTo = "2025-12-31",
            location = "Central Arena",
            division = "Division A",
            status = "Scheduled",
            maxResults = 50
        )

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("tournaments") == true)
        assertTrue(request.path?.contains("startDateFrom=2025-06-01") == true)
        assertTrue(request.path?.contains("startDateTo=2025-12-31") == true)
        assertTrue(request.path?.contains("location=Central+Arena") == true)
        assertTrue(request.path?.contains("division=Division+A") == true)
        assertTrue(request.path?.contains("status=Scheduled") == true)
        assertTrue(request.path?.contains("maxResults=50") == true)
    }

    @Test
    fun `getTournaments with default parameters makes minimal request`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(TestData.Json.tournamentsResponse())
        )

        // When
        repository.getTournaments()

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("tournaments") == true)
        // Default parameters should still be included
        assertTrue(request.path?.contains("status=Scheduled") == true)
        assertTrue(request.path?.contains("maxResults=100") == true)
    }

    @Test
    fun `getTournaments returns failure on network error`() = runTest {
        // Given - Shutdown server to simulate network error
        mockWebServer.shutdown()

        // When
        val result = repository.getTournaments()

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }
}

