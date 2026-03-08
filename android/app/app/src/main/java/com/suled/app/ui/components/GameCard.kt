package com.suled.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suled.app.data.models.Game
import com.suled.app.ui.theme.CourtGreen
import com.suled.app.ui.theme.GameCompleted
import com.suled.app.ui.theme.GameInProgress
import com.suled.app.ui.theme.GameScheduled

@Composable
fun GameCard(
    game: Game,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (game.isOurGame)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header: Round and Court
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏸",
                        fontSize = 16.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Round ${game.round}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = CourtGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Court ${game.courtNumber}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = CourtGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Pairs
            Text(
                text = game.pair1,
                style = MaterialTheme.typography.bodyMedium,
                color = if (game.isOurGame)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "vs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = game.pair2,
                style = MaterialTheme.typography.bodyMedium,
                color = if (game.isOurGame)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: status badge + scheduled time
            val statusKey = when (game.status.lowercase()) {
                "0", "scheduled" -> "scheduled"
                "1", "inprogress" -> "inprogress"
                "2", "completed" -> "completed"
                "3", "cancelled" -> "cancelled"
                else -> game.status.lowercase()
            }
            val statusLabel = when (statusKey) {
                "scheduled" -> "Scheduled"
                "inprogress" -> "In Progress"
                "completed" -> "Completed"
                "cancelled" -> "Cancelled"
                else -> game.status
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (statusKey) {
                        "scheduled" -> GameScheduled.copy(alpha = 0.2f)
                        "inprogress" -> GameInProgress.copy(alpha = 0.2f)
                        "completed" -> GameCompleted.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (statusKey) {
                            "scheduled" -> GameScheduled
                            "inprogress" -> GameInProgress
                            "completed" -> GameCompleted
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                if (!game.scheduledTime.isNullOrBlank()) {
                    val displayTime = game.scheduledTime.let { raw ->
                        // Extract HH:mm from ISO string or plain time string
                        val timeRegex = Regex("""T?(\d{2}:\d{2})""")
                        timeRegex.find(raw)?.groupValues?.get(1) ?: raw
                    }
                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
