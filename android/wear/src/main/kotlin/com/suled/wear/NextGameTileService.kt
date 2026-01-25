package com.suled.wear

import androidx.wear.protolayout.*
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders
import com.google.android.horologist.tiles.SuspendingTileService
import com.suled.data.createWatchLocalStorageService

/**
 * Wear OS Tile showing next upcoming game information
 * Quick glance tile for watch face
 */
class NextGameTileService : SuspendingTileService() {
    
    private val localStorage by lazy {
        createWatchLocalStorageService(this)
    }
    
    companion object {
        private const val RESOURCES_VERSION = "1"
    }
    
    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
    }
    
    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val nextGame = localStorage.getNextGame()
        
        val timeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(createLayout(nextGame))
                            .build()
                    )
                    .build()
            )
            .build()
        
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTimeline(timeline)
            // Request update when data might change (every 5 minutes)
            .setFreshnessIntervalMillis(5 * 60 * 1000)
            .build()
    }
    
    private fun createLayout(
        game: com.suled.models.NextGameInfo?
    ): LayoutElementBuilders.LayoutElement {
        
        return if (game == null) {
            // No upcoming game layout
            createNoGameLayout()
        } else {
            // Next game layout
            createGameLayout(game)
        }
    }
    
    private fun createNoGameLayout(): LayoutElementBuilders.LayoutElement {
        return PrimaryLayout.Builder(DeviceParametersBuilders.DeviceParameters.Builder()
            .setScreenWidthDp(192)
            .setScreenHeightDp(192)
            .build()
        )
        .setContent(
            Text.Builder(this, "No upcoming games")
                .setTypography(Typography.TYPOGRAPHY_BODY1)
                .setColor(androidx.wear.tiles.ColorBuilders.argb(0xFF888888.toInt()))
                .build()
        )
        .build()
    }
    
    private fun createGameLayout(
        game: com.suled.models.NextGameInfo
    ): LayoutElementBuilders.LayoutElement {
        
        val timeText = when {
            game.minutesUntilStart < 0 -> "Starting now!"
            game.minutesUntilStart == 0 -> "Now"
            game.minutesUntilStart < 60 -> "in ${game.minutesUntilStart} min"
            else -> {
                val hours = game.minutesUntilStart / 60
                val mins = game.minutesUntilStart % 60
                "in ${hours}h ${mins}m"
            }
        }
        
        val urgentColor = when {
            game.minutesUntilStart <= 5 -> 0xFFFF5252.toInt() // Red
            game.minutesUntilStart <= 15 -> 0xFFFFC107.toInt() // Amber
            else -> 0xFF4CAF50.toInt() // Green
        }
        
        return PrimaryLayout.Builder(DeviceParametersBuilders.DeviceParameters.Builder()
            .setScreenWidthDp(192)
            .setScreenHeightDp(192)
            .build()
        )
        .setContent(
            LayoutElementBuilders.Column.Builder()
                // Court number (large)
                .addContent(
                    Text.Builder(this, "Court ${game.courtNumber}")
                        .setTypography(Typography.TYPOGRAPHY_TITLE1)
                        .setColor(androidx.wear.tiles.ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build()
                )
                // Time until game
                .addContent(
                    LayoutElementBuilders.Spacer.Builder()
                        .setHeight(DimensionBuilders.dp(8f))
                        .build()
                )
                .addContent(
                    Text.Builder(this, timeText)
                        .setTypography(Typography.TYPOGRAPHY_BODY1)
                        .setColor(androidx.wear.tiles.ColorBuilders.argb(urgentColor))
                        .build()
                )
                // Round info
                .addContent(
                    LayoutElementBuilders.Spacer.Builder()
                        .setHeight(DimensionBuilders.dp(4f))
                        .build()
                )
                .addContent(
                    Text.Builder(this, "Round ${game.round}")
                        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                        .setColor(androidx.wear.tiles.ColorBuilders.argb(0xFFAAAAAA.toInt()))
                        .build()
                )
                // Opponent
                .addContent(
                    LayoutElementBuilders.Spacer.Builder()
                        .setHeight(DimensionBuilders.dp(4f))
                        .build()
                )
                .addContent(
                    Text.Builder(this, "vs ${game.opponentPairName}")
                        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                        .setColor(androidx.wear.tiles.ColorBuilders.argb(0xFFAAAAAA.toInt()))
                        .build()
                )
                .build()
        )
        .build()
    }
}
