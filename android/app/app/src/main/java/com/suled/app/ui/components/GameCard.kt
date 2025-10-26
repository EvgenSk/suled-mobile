package com.suled.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Round and Court
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsTennis,
                        contentDescription = null,
                        tint = CourtGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Round ${game.round}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = CourtGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Court ${game.courtNumber}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = CourtGreen
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Pairs
            Text(
                text = game.pair1,
                style = MaterialTheme.typography.bodyLarge,
                color = if (game.isOurGame) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "vs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = game.pair2,
                style = MaterialTheme.typography.bodyLarge,
                color = if (game.isOurGame) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (game.status.lowercase()) {
                    "scheduled" -> GameScheduled.copy(alpha = 0.2f)
                    "inprogress" -> GameInProgress.copy(alpha = 0.2f)
                    "completed" -> GameCompleted.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = game.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (game.status.lowercase()) {
                        "scheduled" -> GameScheduled
                        "inprogress" -> GameInProgress
                        "completed" -> GameCompleted
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
