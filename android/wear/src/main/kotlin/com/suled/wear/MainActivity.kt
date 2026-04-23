package com.suled.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.compose.ui.platform.LocalContext
import com.suled.data.createWatchLocalStorageService
import com.suled.models.NextGameInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant as JavaInstant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                NextGameScreen()
            }
        }
    }
}

@Composable
fun NextGameScreen() {
    val context = LocalContext.current
    val nextGame by WearDataListenerService.nextGame.collectAsState()

    LaunchedEffect(Unit) {
        val storage = createWatchLocalStorageService(context)
        WearDataListenerService.nextGame.value = withContext(Dispatchers.IO) {
            storage.getNextGame()
        }
    }

    Scaffold(timeText = { TimeText() }) {
        val game = nextGame
        if (game == null) NoGameContent() else GameContent(game)
    }
}

@Composable
private fun NoGameContent() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No upcoming games",
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GameContent(game: NextGameInfo) {
    val zone = ZoneId.systemDefault()
    val scheduledInstant = remember(game.scheduledTime) {
        game.scheduledTime.takeIf { it.isNotEmpty() }
            ?.let { runCatching { JavaInstant.parse(it) }.getOrNull() }
    }
    val gameLocalDate = scheduledInstant?.atZone(zone)?.toLocalDate()
    val gameLocalTime = scheduledInstant?.atZone(zone)?.toLocalTime()
    val todayLocalDate = LocalDate.now(zone)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            gameLocalDate != null && gameLocalDate.isAfter(todayLocalDate) ->
                Text(
                    text = "Tournament is on ${DateTimeFormatter.ofPattern("MMM d").format(gameLocalDate)}",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            game.minutesUntilStart > 15 -> {
                val timeStr = gameLocalTime
                    ?.let { DateTimeFormatter.ofPattern("HH:mm").format(it) }
                    ?: "${game.minutesUntilStart / 60}h ${game.minutesUntilStart % 60}m"
                Text(
                    text = "Tournament starts at $timeStr",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                Text("Court ${game.courtNumber}", style = MaterialTheme.typography.title1, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (game.minutesUntilStart >= -3) "Now" else "Next",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Round ${game.round}", style = MaterialTheme.typography.body2, textAlign = TextAlign.Center)
            }
        }
    }
}
