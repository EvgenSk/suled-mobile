package com.suled.wear

import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.suled.data.createWatchLocalStorageService

/**
 * Wear OS Tile showing next upcoming game information
 * Quick glance tile for watch face
 */
@OptIn(ExperimentalHorologistApi::class)
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
            .setFreshnessIntervalMillis(5 * 60 * 1000)
            .build()
    }

    private fun createLayout(
        game: com.suled.models.NextGameInfo?
    ): LayoutElementBuilders.LayoutElement {
        return if (game == null) createNoGameLayout() else createGameLayout(game)
    }

    private fun createNoGameLayout(): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("No upcoming\ngames")
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setColor(ColorBuilders.argb(0xFF888888.toInt()))
                            .setSize(DimensionBuilders.sp(16f))
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createGameLayout(
        game: com.suled.models.NextGameInfo
    ): LayoutElementBuilders.LayoutElement {

        val timeText = when {
            game.minutesUntilStart <= 0 -> "Starting now!"
            game.minutesUntilStart < 60 -> "in ${game.minutesUntilStart} min"
            else -> {
                val h = game.minutesUntilStart / 60
                val m = game.minutesUntilStart % 60
                "in ${h}h ${m}m"
            }
        }

        val urgentColor = when {
            game.minutesUntilStart <= 5  -> 0xFFFF5252.toInt() // Red
            game.minutesUntilStart <= 15 -> 0xFFFFC107.toInt() // Amber
            else                          -> 0xFF4CAF50.toInt() // Green
        }

        return LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(
                LayoutElementBuilders.Column.Builder()
                    .setWidth(DimensionBuilders.expand())
                    .setHeight(DimensionBuilders.wrap())
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("Court ${game.courtNumber}")
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                                    .setSize(DimensionBuilders.sp(22f))
                                    .build()
                            )
                            .build()
                    )
                    .addContent(spacer(6f))
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText(timeText)
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setColor(ColorBuilders.argb(urgentColor))
                                    .setSize(DimensionBuilders.sp(16f))
                                    .build()
                            )
                            .build()
                    )
                    .addContent(spacer(4f))
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("Round ${game.round}")
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setColor(ColorBuilders.argb(0xFFAAAAAA.toInt()))
                                    .setSize(DimensionBuilders.sp(13f))
                                    .build()
                            )
                            .build()
                    )
                    .addContent(spacer(4f))
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("vs ${game.opponentPairName}")
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setColor(ColorBuilders.argb(0xFFAAAAAA.toInt()))
                                    .setSize(DimensionBuilders.sp(13f))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun spacer(dp: Float): LayoutElementBuilders.Spacer =
        LayoutElementBuilders.Spacer.Builder()
            .setHeight(DimensionBuilders.dp(dp))
            .build()
}
