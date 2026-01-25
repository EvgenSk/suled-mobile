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
    val localStorage = remember { createWatchLocalStorageService(context) }
    val nextGame by produceState<com.suled.models.NextGameInfo?>(initialValue = null) {
        value = localStorage.getNextGame()
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
            if (nextGame != null) {
                Text(
                    text = "Court ${nextGame!!.courtNumber}",
                    style = MaterialTheme.typography.title1,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nextGame!!.timeUntil,
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Round ${nextGame!!.round}",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "No upcoming games",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
