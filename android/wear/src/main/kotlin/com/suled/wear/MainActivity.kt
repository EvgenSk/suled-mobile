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
import com.suled.data.createWatchLocalStorageService
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
    val context = androidx.compose.ui.platform.LocalContext.current

    // Reactive: updates whenever WearDataListenerService receives new data from the phone
    val nextGame by WearDataListenerService.nextGame.collectAsState()

    // Initial load from SharedPrefs on first composition
    LaunchedEffect(Unit) {
        val storage = createWatchLocalStorageService(context)
        WearDataListenerService.nextGame.value = withContext(Dispatchers.IO) {
            storage.getNextGame()
        }
    }
    
    Scaffold(
        timeText = { TimeText() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
                val game = nextGame
            if (game == null) {
                Text(
                    text = "No upcoming games",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            } else {
                val scheduledInstant = remember(game.scheduledTime) {
                    if (game.scheduledTime.isEmpty()) null
                    else try { JavaInstant.parse(game.scheduledTime) } catch (e: Exception) { null }
                }
                val zone = ZoneId.systemDefault()
                val gameLocalDate = scheduledInstant?.atZone(zone)?.toLocalDate()
                val todayLocalDate = LocalDate.now(zone)
                val gameLocalTime = scheduledInstant?.atZone(zone)?.toLocalTime()

                when {
                    // Tournament is on a future date
                    gameLocalDate != null && gameLocalDate.isAfter(todayLocalDate) -> {
                        val dateStr = DateTimeFormatter.ofPattern("MMM d").format(gameLocalDate)
                        Text(
                            text = "Tournament is on $dateStr",
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                    }
                    // Today, but more than 15 minutes away
                    game.minutesUntilStart > 15 -> {
                        val timeStr = if (gameLocalTime != null)
                            DateTimeFormatter.ofPattern("HH:mm").format(gameLocalTime)
                        else "${game.minutesUntilStart / 60}h ${game.minutesUntilStart % 60}m"
                        Text(
                            text = "Tournament starts at $timeStr",
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                    }
                    // Within 15 min before start or 10 min after — show game detail
                    else -> {
                        Text(
                            text = "Court ${game.courtNumber}",
                            style = MaterialTheme.typography.title1,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when {
                                game.minutesUntilStart <= 0 -> "Now"
                                game.minutesUntilStart == 1 -> "In 1 min"
                                else -> "In ${game.minutesUntilStart} min"
                            },
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Round ${game.round}",
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
