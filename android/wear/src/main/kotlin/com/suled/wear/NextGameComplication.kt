package com.suled.wear

import android.content.Context
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.suled.data.createWatchLocalStorageService
import com.suled.R

/**
 * Wear OS Complication showing next upcoming game
 * Displays court number and time until game
 */
class NextGameComplication : SuspendingComplicationDataSourceService() {
    
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder("Court 5").build(),
                contentDescription = PlainComplicationText.Builder("Next game on Court 5").build()
            )
            .setTitle(PlainComplicationText.Builder("12m").build())
            .build()
            
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("Court 5 in 12 min").build(),
                contentDescription = PlainComplicationText.Builder("Next game").build()
            )
            .setTitle(PlainComplicationText.Builder("Round 3").build())
            .build()
            
            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 12f,
                min = 0f,
                max = 60f,
                contentDescription = PlainComplicationText.Builder("12 minutes until game").build()
            )
            .setText(PlainComplicationText.Builder("12m").build())
            .setTitle(PlainComplicationText.Builder("Court 5").build())
            .build()
            
            else -> null
        }
    }
    
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val localStorage = createWatchLocalStorageService(this)
        val nextGame = localStorage.getNextGame()
        
        // If no upcoming game, show "no games" message
        if (nextGame == null) {
            return when (request.complicationType) {
                ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("--").build(),
                    contentDescription = PlainComplicationText.Builder("No upcoming games").build()
                )
                .setTitle(PlainComplicationText.Builder("No games").build())
                .build()
                
                ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("No upcoming games").build(),
                    contentDescription = PlainComplicationText.Builder("No upcoming games").build()
                )
                .build()
                
                else -> NoDataComplicationData()
            }
        }
        
        // Build complication based on type
        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> buildShortText(nextGame)
            ComplicationType.LONG_TEXT -> buildLongText(nextGame)
            ComplicationType.RANGED_VALUE -> buildRangedValue(nextGame)
            else -> null
        }
    }
    
    private fun buildShortText(game: com.suled.models.NextGameInfo): ShortTextComplicationData {
        val timeText = when {
            game.minutesUntilStart < 0 -> "Now!"
            game.minutesUntilStart == 0 -> "Now"
            game.minutesUntilStart < 60 -> "${game.minutesUntilStart}m"
            else -> {
                val hours = game.minutesUntilStart / 60
                val mins = game.minutesUntilStart % 60
                "${hours}h${mins}m"
            }
        }
        
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("Court ${game.courtNumber}").build(),
            contentDescription = PlainComplicationText.Builder(
                "Next game on Court ${game.courtNumber} in $timeText"
            ).build()
        )
        .setTitle(PlainComplicationText.Builder(timeText).build())
        .setMonochromaticImage(
            MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.ic_court)
            ).build()
        )
        .build()
    }
    
    private fun buildLongText(game: com.suled.models.NextGameInfo): LongTextComplicationData {
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
        
        return LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(
                "Court ${game.courtNumber} $timeText"
            ).build(),
            contentDescription = PlainComplicationText.Builder(
                "Next game on Court ${game.courtNumber} $timeText"
            ).build()
        )
        .setTitle(PlainComplicationText.Builder("Round ${game.round}").build())
        .setMonochromaticImage(
            MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.ic_court)
            ).build()
        )
        .build()
    }
    
    private fun buildRangedValue(game: com.suled.models.NextGameInfo): RangedValueComplicationData {
        // Show time as progress (0-60 minutes range)
        val value = game.minutesUntilStart.coerceIn(0, 60).toFloat()
        
        val timeText = when {
            game.minutesUntilStart < 0 -> "Now"
            game.minutesUntilStart == 0 -> "0m"
            else -> "${game.minutesUntilStart}m"
        }
        
        return RangedValueComplicationData.Builder(
            value = value,
            min = 0f,
            max = 60f,
            contentDescription = PlainComplicationText.Builder(
                "${game.minutesUntilStart} minutes until game on Court ${game.courtNumber}"
            ).build()
        )
        .setText(PlainComplicationText.Builder(timeText).build())
        .setTitle(PlainComplicationText.Builder("Court ${game.courtNumber}").build())
        .setMonochromaticImage(
            MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.ic_court)
            ).build()
        )
        .build()
    }
}
